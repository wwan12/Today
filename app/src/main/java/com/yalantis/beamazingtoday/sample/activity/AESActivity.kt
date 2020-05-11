package com.yalantis.beamazingtoday.sample.activity

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import com.yalantis.beamazingtoday.sample.R
import com.yalantis.beamazingtoday.sample.expand.toast
import kotlinx.android.synthetic.main.activity_aes.*
import kotlinx.android.synthetic.main.item_key.view.*


class AESActivity:AppCompatActivity() {

    val keys=ArrayList<Key>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_aes)
//        outKey(0)
        key_list.adapter=KeyAdapter(this,keys)
        main_key.rel.visibility= View.GONE
        keys.add(Key("私人密钥","9df30bd5..."))
        keys.add(Key("工作密钥","0f0365e9..."))
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

    data class Key(val name:String,val key:String)
}