package com.aisino.tool.http

import android.os.Handler
import android.os.Looper
import android.util.JsonToken
import okhttp3.*
import android.util.Xml
import com.google.gson.Gson
import com.google.gson.stream.JsonReader
import org.xmlpull.v1.XmlPullParser
import java.io.*
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Cookie
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import java.util.concurrent.TimeUnit
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import com.google.gson.JsonArray
import com.yalantis.beamazingtoday.sample.BuildConfig.DEBUG
import com.yalantis.beamazingtoday.sample.expand.loge
import okio.ByteString
import org.json.JSONArray
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import okhttp3.RequestBody
import java.lang.Exception


/**
 * Created by lenovo on 2017/11/14.
 */

val cookjar: CookieJar = object : CookieJar {
    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        cookieStore.put(url.host, cookies)
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val cookies = cookieStore[url.host]
        return cookies ?: ArrayList()
    }
}
val cookieStore = HashMap<String, List<Cookie>>()//cookie缓存
//url : result
val testResult=HashMap<String,Submit.TestResult>()

private var socket: WebSocket?=null


private val socketCalls=ArrayList<Submit.SocketCall>()

private var _socketCall: (SuccessData) -> Unit = {
    for (sc in socketCalls){
        sc.call(it)
        if (!sc.save){
            socketCalls.remove(sc)
        }
    }
}

/**
 * 正式访问前缀
 */
var RELEASEAPI = ""
/**
 * debug访问前缀
 */
var DEBUGAPI = ""

class Submit {
    //可配置属性
    var url = ""
    var cacheUrl=""
    var tag = ""
    var method = Method.GET
    var returnType = ReturnType.JSON
    var downloadPath = System.currentTimeMillis().toString() + ".jpg"
    var outTime = 4L//单位为秒
    //出错是否重启请求
    var isRetry = false
    private val _params: MutableMap<String, Any> = mutableMapOf()
  //  val _fileParams: MutableMap<String, String> = mutableMapOf()
 //   val _headers: MutableMap<String, String> = mutableMapOf()
    private val _response: MutableMap<String, Any> = mutableMapOf()
    var jsonParam=""//临时参数
    lateinit private var toUI:Handler
    private var _start: () -> Unit = {}
    private var _success: (SuccessData) -> Unit = {}
    private var _socketOpen: () -> Unit = {}
    private var _fail: (FailData) -> Unit = {}

    var beat:SocketHeart?=null
    var socketSave=false//是否保留长连接回调

    //    var cookjar: CookieJar
//    val cookieStore = HashMap<String, List<Cookie>>()//cookie缓存
//
//    init {
//        cookjar = object : CookieJar {
//            override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
//                cookieStore.put(url.host(), cookies)
//            }
//
//            override fun loadForRequest(url: HttpUrl): List<Cookie> {
//                val cookies = cookieStore[url.host()]
//                return cookies ?: ArrayList()
//            }
//        }
//    }


    fun run() {
        tryInit()
    }


    private fun tryInit(): Unit { //检查配置单
        if (Looper.myLooper() != Looper.getMainLooper()) {
            Looper.getMainLooper().run { toUI =  Handler()  }
        } else {
            toUI= Handler()
        }
        when (returnType) {
            ReturnType.JSON -> {
            }
            ReturnType.XML -> {
            }
        }
        tag = method.name
        cacheUrl=url
        _start()
        if (DEBUG) {
            url = DEBUGAPI + url
            if (testResult.containsKey(url)){
                testSuccessCall(testResult[url]!!)
                return
            }
        } else {
            url = RELEASEAPI + url
        }
        when (method) {//分类请求
            Method.GET -> get()

            Method.POST -> post()

            Method.POSTJSON -> postJson()

            Method.IMAGE -> upImage()

            Method.DOWNLOAD -> download()

            Method.FILE -> upFile()

            Method.SOCKET ->  socketOpen()

            Method.SOCKETSEND -> socket()
        }

    }

    fun start(start: () -> Unit): Unit {//检查参数
        _start = start

    }

    fun success(success: (SuccessData) -> Unit): Unit {
        _success = success
    }

    fun fail(fail: (FailData) -> Unit): Unit {
        _fail = fail
    }

    fun socketOpen(open: () -> Unit): Unit {
        _socketOpen=open
    }

//    private fun test(): Unit {//测试方法
//        toUI.postDelayed({
////            if (){
//                pullJson()
////            }else{
////                _fail(RuntimeException().toString())
////            }
//        },200)
//    }

    private fun get(): Unit {
        val okHttpClient = OkHttpClient.Builder().cookieJar(cookjar).connectTimeout(outTime, TimeUnit.SECONDS)
        if (cacheUrl.length > 0 && !cacheUrl.substring(cacheUrl.length - 1, cacheUrl.length).equals("?")&&_params.size>0) {
            cacheUrl = cacheUrl + "?"
        }
        for (p in _params) {
            cacheUrl = cacheUrl + p.key + "=" + p.value + "&"
        }
        if (_params.size > 0) {
            cacheUrl = cacheUrl.substring(0, cacheUrl.length - 1)
        }
        "DEBUGAPI->$cacheUrl".loge("api")
        val request = Request.Builder().url(cacheUrl).build()
        val call = okHttpClient.build().newCall(request)
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                failCall(e.toString())
            }

            override fun onResponse(call: Call, response: Response) {
                successCall(response)
            }
        })
    }

    private fun post(): Unit {
        val okHttpClient = OkHttpClient.Builder().cookieJar(cookjar).connectTimeout(outTime, TimeUnit.SECONDS)
        val build = FormBody.Builder()
        url.loge("post")
        for (p in _params) {
            build.add(p.key, p.value.toString())
            (p.key + "-" + p.value.toString()).loge("post")
        }
        val body = build.build()
        val request = Request.Builder().url(cacheUrl).post(body).build()
        val call = okHttpClient.build().newCall(request)
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                failCall(e.toString())

            }

            override fun onResponse(call: Call, response: Response) {
                successCall(response)
            }
        })
    }

    data class Param(var token:String ,var content:Any)//提交数据的外壳

    private fun postJson(): Unit {//临时改造
        val okHttpClient = OkHttpClient.Builder().cookieJar(cookjar).connectTimeout(outTime, TimeUnit.SECONDS)
//        val gson= Gson()
//        val param=gson.fromJson<Param>(jsonParam,Param::class.java)
//        val body =  jsonParam.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
        val build = FormBody.Builder()
        url.loge("postjson")
        jsonParam.loge("postjson")
        build.add("data",jsonParam)
//        build.add("content",gson.toJson( param.content))
        val body = build.build()
        val request = Request.Builder().url(cacheUrl).post(body).build()
        val call = okHttpClient.build().newCall(request)
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                failCall(e.toString())

            }

            override fun onResponse(call: Call, response: Response) {
                successCall(response)
            }
        })
    }

    private fun upImage() {
        val mOkHttpClient = OkHttpClient.Builder().cookieJar(cookjar).connectTimeout(outTime, TimeUnit.SECONDS)
        val build = MultipartBody.Builder().setType(MultipartBody.FORM)
        for (p in _params) {
            if (p.value is File) {
                build.addFormDataPart(p.key, (p.value as File).name,( p.value as File).asRequestBody("image/png".toMediaTypeOrNull() ))//RequestBody.create("image/png".toMediaTypeOrNull(), p.value as File)
            } else {
                build.addFormDataPart(p.key, p.value.toString())
            }

        }
        val requestBody = build.build()

        val request = Request.Builder()
//                .header("Authorization", "Client-ID " + "...")
                .url(cacheUrl)
                .post(requestBody)
                .build()

        mOkHttpClient.build().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                failCall(e.toString())
            }

            override fun onResponse(call: Call, response: Response) {
                successCall(response)
            }
        })
    }

    private fun upFile(){
        val mOkHttpClient = OkHttpClient.Builder().cookieJar(cookjar).connectTimeout(outTime, TimeUnit.SECONDS)
        val build = MultipartBody.Builder().setType(MultipartBody.FORM)
        for (p in _params) {
            if (p.value is File) {
                build.addFormDataPart(p.key, (p.value as File).name,( p.value as File).asRequestBody("file/*".toMediaTypeOrNull()) )//MediaType.parse("file/*")
            } else {
                build.addFormDataPart(p.key, p.value.toString())
            }

        }
        val requestBody = build.build()

        val request = Request.Builder()
//                .header("Authorization", "Client-ID " + "...")
                .url(cacheUrl)
                .post(requestBody)
                .build()

        mOkHttpClient.build().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                failCall(e.toString())
            }

            override fun onResponse(call: Call, response: Response) {
                successCall(response)
            }
        })
    }


    private fun socketOpen(): Unit {
        val mOkHttpClient = OkHttpClient.Builder()
                .readTimeout(5, TimeUnit.SECONDS)//设置读取超时时间
                .writeTimeout(5, TimeUnit.SECONDS)//设置写的超时时间
                .connectTimeout(5, TimeUnit.SECONDS)//设置连接超时时间
                .build()
        val request = Request.Builder().url(url).build()
        mOkHttpClient.newWebSocket(request, object: WebSocketListener(){
            override fun onOpen(webSocket: WebSocket, response: Response) {
                super.onOpen(webSocket, response)
                socket = webSocket
                ("连接成功").loge("onOpen")
                if (beat!=null){
                    val timer = Timer()
                    timer.schedule(object : TimerTask() {
                        override fun run() {
                            socket?.send(beat!!.data)
                        }
                    }, beat!!.beat,beat!!.beat)
                }
                _socketOpen()
            }

            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                super.onMessage(webSocket, bytes)
                ("receive bytes:" + bytes.hex()).loge("onMessage")
                successCall(bytes.hex())
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                super.onMessage(webSocket, text)
                ("receive text:" + text ).loge("onMessage")
                successCall(text)

            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                super.onClosed(webSocket, code, reason)
                ("closed:" + reason).loge("onClosed")
                socket=null
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                super.onClosing(webSocket, code, reason)
                ("closing:" + reason).loge("onClosing")
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                super.onFailure(webSocket, t, response)
                ("closing:" + t.message).loge("onFailure")
                failCall(t.message!!)
            }
        })

        mOkHttpClient.dispatcher.executorService.shutdown()
    }

    private fun socket(): Unit {
        socket?.send(_params["socket"].toString())
        socketCalls.add(SocketCall(socketSave,_success))
    }

    private fun download(): Unit {
        val mOkHttpClient = OkHttpClient.Builder().cookieJar(cookjar).connectTimeout(outTime, TimeUnit.SECONDS)
        val request = Request.Builder().url(cacheUrl).build()
        mOkHttpClient.build().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                failCall(e.toString())
            }

            override fun onResponse(call: Call, response: Response) {
                val inputStream = response.body?.byteStream()
                val fileOutputStream = FileOutputStream(File(downloadPath))
                val buffer = ByteArray(2048)
                var len = 0
                while (len != -1) {
                    fileOutputStream.write(buffer, 0, len)
                    len = inputStream!!.read(buffer)
                }
                fileOutputStream.flush()
                fileOutputStream.close()
                successCall(response)
            }
        })
    }



    private fun failCall(failMsg: String): Unit {
        toUI.post {
            failMsg.loge("failCall")
            _fail(FailData(url,failMsg).apply { })
            retrySubmit()
        }
    }

    private fun successCall(response: Response): Unit {
        toUI.post {
            if (response.code != 200) {
                response.request.url.toString().loge("failCall"+"code:"+response.code)
                failCall("请求失败:" + response.code)
             //   _fail(FailData(url,"请求失败:" + response.code).apply { this.submitTime=DateAndTime.nowDateTime })
                return@post
            }
        }
        response.request.url.toString().loge("successCall")
        jsonParam?.loge("successCall")
        when (returnType) {
            ReturnType.JSON -> {
                var jsonString = response.body?.string()
                jsonString?.loge("successCall")
                pullJson(jsonString!!)
            }
            ReturnType.XML -> {
//                    val s = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<ROOT><RESULT><CODE>9999</CODE><POS><PO>1111</PO><PO>2222</PO></POS><CONTENT>java.lang.NullPointerException\ncom.aisino.heb.xlg.web.servlet.XlgServlet.doPost(XlgServlet.java:135)</CONTENT></RESULT></ROOT>".byteInputStream()
                pullXML(response.body!!.byteStream())
//                    pullXML(s)
            }
            ReturnType.STRING -> {
                _response.put(ReturnType.STRING.name, response.body!!.string())
            }
        }
        toUI.post {
            _success(SuccessData(url,_response).apply {
                this.params.putAll(params)
                 })
        }
    }
///socket 专用
    private fun successCall(vback: String){
        (url+":"+vback).loge("successCall")
        when (returnType) {
            ReturnType.JSON -> {
                var jsonString = vback
                jsonString?.loge("successCall")
                pullJson(jsonString!!)
            }
            ReturnType.XML -> {
//                    val s = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<ROOT><RESULT><CODE>9999</CODE><POS><PO>1111</PO><PO>2222</PO></POS><CONTENT>java.lang.NullPointerException\ncom.aisino.heb.xlg.web.servlet.XlgServlet.doPost(XlgServlet.java:135)</CONTENT></RESULT></ROOT>".byteInputStream()
                pullXML(vback.byteInputStream())
//                    pullXML(s)
            }
            ReturnType.STRING -> {
                _response.put(ReturnType.STRING.name,vback)
            }
        }
        toUI.post {
            _socketCall(SuccessData(url,_response).apply {
                this.params.putAll(params)
                 })
        }
    }


    /**
     * 测试回调
     */
    private fun testSuccessCall(response: TestResult): Unit {
        if (response.code != 200) {
            response.url.toString().loge("failCall" + "code:" + response.code)
            failCall("请求失败:" + response.code)
            //   _fail(FailData(url,"请求失败:" + response.code).apply { this.submitTime=DateAndTime.nowDateTime })
            return
        }
        response.url.loge("successCall")
        when (returnType) {
            ReturnType.JSON -> {
                var jsonString = response.result
                jsonString?.loge("successCall")
                pullJson(jsonString!!)
            }
            ReturnType.XML -> {
                pullXML(response.result.byteInputStream())
            }
            ReturnType.STRING -> {
                _response.put(ReturnType.STRING.name, response.result)
            }
        }
        _success(SuccessData(url, _response).apply {
            this.params.putAll(params)
        })
    }


    //- 入参
    operator fun String.minus(value: String?) {
        if (value != null) {
            _params.put(this, value)
        }

    }

    //- 入参
    operator fun String.minus(value: File?) {
        if (value!=null){
            _params.put(this, value)
        }
    }

    // ！ 简单取参 单key
    operator fun String.not(): String {
        return _response[this] as String
    }

    // .. 复杂取参
    operator fun <E> String.rangeTo(tag: String): E {
        val c = _response[this] as MutableMap<*, *>
        return c[tag] as E
    }


    private fun pullJson(jsonData: String): Unit {
//        if (jsonData.startsWith("[")){
//            _response.put(JsonToken.BEGIN_ARRAY.name, JSONArray(jsonData))
//            return
//        }
        val reader = JsonReader(StringReader(jsonData))
        if (jsonData.startsWith("[")){
            loopJson(JsonToken.BEGIN_ARRAY.name, reader, _response)
        }else{
            reader.beginObject()
            while (reader.hasNext()) {
                val jName = reader.nextName()
                loopJson(jName, reader, _response)
            }
            reader.endObject()
        }
    }


    private fun loopJson(loopName: String, reader: JsonReader, target: MutableMap<String, Any>): Unit {
        when (reader.peek().name) {
            JsonToken.BEGIN_OBJECT.name -> {
                reader.beginObject()
                val bo: MutableMap<String, Any> = mutableMapOf()
                while (reader.hasNext()) {
                    val jName = reader.nextName()
                    loopJson(jName, reader, bo)
                }
                target.put(loopName, bo)
                reader.endObject()
            }
            JsonToken.BEGIN_ARRAY.name -> {
                reader.beginArray()
                val al = ArrayList<MutableMap<String, Any>>()
                val als = ArrayList<String>()
                while (reader.hasNext()) {
                    if (reader.peek().name.equals(JsonToken.STRING.name)) {
                        als.add(reader.nextString())
                    } else {
                        reader.beginObject()
                        val ba: MutableMap<String, Any> = mutableMapOf()
                        while (reader.hasNext()) {
                            loopJson(reader.nextName(), reader, ba)
                        }
                        al.add(ba)
                        reader.endObject()
                    }
                }
                reader.endArray()
                if (als.size > 0) {
                    target.put(loopName, als)
                } else {
                    target.put(loopName, al)
                }
            }
            JsonToken.BOOLEAN.name -> {
                target.put(loopName, reader.nextBoolean())
            }
            JsonToken.STRING.name -> {
                target.put(loopName, reader.nextString())
            }
            JsonToken.NULL.name -> {
                target.put(loopName, "")
                reader.skipValue()
            }
            JsonToken.NUMBER.name -> {
                try {
                    target.put(loopName, reader.nextLong().toString())
                }catch (e:Exception){
                    target.put(loopName, reader.nextDouble().toString())
                }
            }
            else -> {
                target.put(loopName, "")
            }
        }
    }


    private fun pullXML(byteStream: InputStream): Unit {
        val parser = Xml.newPullParser()
        parser.setInput(byteStream, "UTF-8")
        var eventCode = parser.eventType
        var tagName = ""
        var tagText = ""
        while (eventCode != XmlPullParser.END_DOCUMENT) {
            when (eventCode) {
                XmlPullParser.START_DOCUMENT -> {

                }

                XmlPullParser.START_TAG -> {
                    tagName = parser.name
                }

                XmlPullParser.TEXT -> {
                    tagText = parser.text
                }
                XmlPullParser.END_TAG -> {// 结束标签，判断一个对象是否结束，结束后添加到集合中
                    if (_response.containsKey(tagName)) {
                        if (_response[tagName] is ArrayList<*>) {
                            (_response[tagName] as ArrayList<String>).add(tagText)
                        } else {
                            val al = ArrayList<String>()
                            al.add(_response[tagName].toString())
                            al.add(tagText)
                            _response.put(tagName, al)
                        }
                    } else {
                        _response.put(tagName, tagText)
                    }
                }
            }
            eventCode = parser.next() // 取下个标签
        }
    }

    fun <E> getAny(string: String): E {
        return loopAny<E>(string, _response) as E
    }


    private fun <E> loopAny(key: String, target: MutableMap<String, Any>): E? {
        var result: E?
        if (target.containsKey(key)) {
            return target[key] as E
        }//找到了就直接返回
        for (res in target) {
            if (res.value is MutableMap<*, *>) {
                result = loopAny<E>(key, res.value as MutableMap<String, Any>)
                if (result != null) {
                    return result
                }
            }
        }

        return null
    }

    /**
     * 重启请求
     */
    fun retrySubmit(): Unit {
        toUI.postDelayed({
            if(isRetry){
                tryInit()
            }else{

            }
        },5000)

    }

    data class TestResult(val code:Int ,val url:String, val result:String,val waitTime:Long)
    data class SocketHeart(val beat:Long,val data:String)
    data class SocketCall(val save:Boolean,val call:(SuccessData) -> Unit)
}
