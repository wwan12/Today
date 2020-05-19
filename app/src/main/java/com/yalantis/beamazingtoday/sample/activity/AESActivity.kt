package com.yalantis.beamazingtoday.sample.activity

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.BaseAdapter
import android.widget.Toast
import com.yalantis.beamazingtoday.sample.R
import com.yalantis.beamazingtoday.sample.expand.ACache
import com.yalantis.beamazingtoday.sample.expand.generateKey
import com.yalantis.beamazingtoday.sample.expand.toast
import kotlinx.android.synthetic.main.activity_aes.*
import kotlinx.android.synthetic.main.dialog_team_name.view.*
import kotlinx.android.synthetic.main.item_key.view.*
import java.io.Serializable


class AESActivity:AppCompatActivity() {

    val keys=ArrayList<Key>()

    var mainKey=Key("主密钥",generateKey()!!)

    val rel={ i:Int->relKey(i) }
    val out={ i:Int-> outKey(i)}
    val delete={ i:Int-> deleteKey(i)}
    companion object{
        val MAIN_KEY="MAIN_KEY"
        val KEY_NAME="KEY_NAME"
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_aes)
        initKeys()
        initView()
    }

    fun initView(): Unit {
        key_list.adapter=KeyAdapter(this,keys,rel,out,delete)
        main_key.rel.visibility= View.GONE
        c_key.setOnClickListener { cKey() }
        key_inp_enter.setOnClickListener { inputKey() }
    }

    fun cKey(): Unit {
        var success: AlertDialog? = null
        val load = AlertDialog.Builder(this)
        val log = layoutInflater.inflate(R.layout.dialog_team_name, null, false)
        log.log_name_enter.setOnClickListener {
            if (log.log_input_name.text.toString().equals("")){
                "密钥名称不可为空".toast(this)
            }else{
                keys.add(Key(log.log_input_name.text.toString(),generateKey()!!))
                success?.dismiss()
                (key_list.adapter as BaseAdapter).notifyDataSetChanged()
                ACache.get(this).put(KEY_NAME,keys)
            }
        }
        load.setView(log)
        success = load.show()
    }

    fun inputKey(): Unit {
        if (key_inp.text.toString().equals("")){
            "导入密钥不可为空".toast(this)
        }else{
            var success: AlertDialog? = null
            val load = AlertDialog.Builder(this)
            val log = layoutInflater.inflate(R.layout.dialog_team_name, null, false)
            log.log_name_enter.setOnClickListener {
                if (log.log_input_name.text.toString().equals("")){
                    "密钥名称不可为空".toast(this)
                }else{
                    keys.add(Key(log.log_input_name.text.toString(),key_inp.text.toString()))
                    success?.dismiss()
                    (key_list.adapter as BaseAdapter).notifyDataSetChanged()
                    ACache.get(this).put(KEY_NAME,keys)
                }
            }
            load.setView(log)
            success = load.show()
        }
    }

    fun initKeys(): Unit {//读取所有钥匙
        val cmainKey= ACache.get(this).getAsObject(MAIN_KEY)
        if (cmainKey!=null){
            mainKey.key=(cmainKey as Key).key
            mainKey.name=(cmainKey as Key).name
        }
        keys.addAll(ACache.get(this).getAsObject(KEY_NAME) as ArrayList<Key>)
        main_key.key_name.text=mainKey.name
        main_key.key_v.text=mainKey.key
        (key_list.adapter as BaseAdapter).notifyDataSetChanged()
    }

    //替换点击回调
    fun relKey(i:Int): Unit {
        mainKey.key=keys[i].key
        mainKey.name=keys[i].name
        keys[i].name=main_key.key_name.text.toString()
        keys[i].key=main_key.key_v.text.toString()
        main_key.key_name.text=mainKey.name
        main_key.key_v.text=mainKey.key
        ACache.get(this).put(MAIN_KEY,mainKey)
        ACache.get(this).put(KEY_NAME,keys)
    }
    //导出点击回调
    fun outKey(i:Int): Unit {
        //获取剪贴板管理器
        val clipboardManager: ClipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val ocrKey= keys[i].key
        // 创建普通字符型ClipData
        val mClipData = ClipData.newPlainText("OcrText", ocrKey)
        // 将ClipData内容放到系统剪贴板里。
        clipboardManager.primaryClip = mClipData
        "已将密钥复制至剪贴板".toast(this)
    }
    //删除点击回调
    fun deleteKey(i:Int): Unit {
        keys.removeAt(i)
        (key_list.adapter as BaseAdapter).notifyDataSetChanged()
        ACache.get(this).put(KEY_NAME,keys)
        "删除成功".toast(this)
    }



    data class Key(var name:String,var key:String):Serializable
}