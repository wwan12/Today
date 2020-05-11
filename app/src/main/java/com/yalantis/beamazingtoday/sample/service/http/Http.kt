package com.aisino.tool.http

/**
 * Created by lenovo on 2017/11/14.
 *
 * Http.get {
 *url = "login"
 *"kotlin"-"1.6"//添加参数
 *请求开始
 *start {  }
 *请求成功
 *success {
 *getAny<ArrayList<String>>("ids")//取出集合ids
 *!"id"//取出STRING参数id
 *"user".."id"//取出userJOBJ对象中STRING参数id
 *}
 *请求失败
 *fail { failMsg -> Toast.makeText(this@NetWorkActivity, failMsg, Toast.LENGTH_SHORT).show() }
 *}
 */
object Http {

    var get = fun(function: Submit.() -> Unit) {
        val sub = Submit()
        sub.method = Method.GET
        sub.function()
        sub.run()

    }

    var post = fun(function: Submit.() -> Unit) {
        val sub = Submit()
        sub.method = Method.POST
        sub.function()
        sub.run()
    }

    var postjson = fun(function: Submit.() -> Unit) {
        val sub = Submit()
        sub.method = Method.POSTJSON
        sub.function()
        sub.run()
    }

    var upimage = fun(function: Submit.() -> Unit) {
        val sub = Submit()
        sub.function()
        sub.method = Method.IMAGE
        sub.run()
    }

    var upfile = fun(function: Submit.() -> Unit) {
        val sub = Submit()
        sub.function()
        sub.method = Method.FILE
        sub.run()
    }

    var download = fun(function: Submit.() -> Unit) {
        val sub = Submit()
        sub.function()
        sub.method = Method.DOWNLOAD
        sub.run()
    }

    var test = fun(function: HashMap<String,Submit.TestResult>.() -> Unit) {
        val h = HashMap<String,Submit.TestResult>()
        h.function()
        testResult.putAll(h)
    }

    var socketOpen = fun(function: Submit.() -> Unit) {
        val sub = Submit()
        sub.function()
        sub.method = Method.SOCKET
       // sub.returnType=ReturnType.STRING
        sub.run()
    }
    var socketSend = fun(function: Submit.() -> Unit) {
        val sub = Submit()
        sub.function()
        sub.method = Method.SOCKETSEND
       // sub.returnType=ReturnType.STRING
        sub.run()
    }
}

