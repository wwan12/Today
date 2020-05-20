package com.yalantis.beamazingtoday.sample.activity

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.aisino.tool.http.Http
import com.yalantis.beamazingtoday.sample.AES_KEY
import com.yalantis.beamazingtoday.sample.Goal

import com.yalantis.beamazingtoday.sample.R
import com.yalantis.beamazingtoday.sample.activity.ExampleActivity.Companion.HASIMGS
import com.yalantis.beamazingtoday.sample.expand.*
import com.yalantis.beamazingtoday.sample.service.UP_YUN
import com.yalantis.beamazingtoday.sample.user
import kotlinx.android.synthetic.main.activity_des.*
import java.io.ByteArrayOutputStream

import java.io.FileNotFoundException
import java.io.IOException
import java.net.URISyntaxException
import java.util.ArrayList

/**
 * Created by lenovo on 2017/11/28.
 */

class DesActivity : Activity() {
    private var position: Int = 0

    private var text=""

    private val images=ArrayList<String>()

    private var uid=""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_des)

        position = intent.getIntExtra(NUMBER, 0)
        uid = intent.getStringExtra(UID)
        position.toString().loge()

        if (uid.equals("")) {
            init()
        } else {
            initYUN()
        }

        updata_to_w.setOnClickListener {
            if (uid.equals("")){
                upYUN()
            }else{
                "该信息已经上传至云".toast(this)
            }

        }

    }

    private fun init() {
        text = ACache.get(this).getAsString(position.toString() + "")
        if (ACache.get(this).getAsObject(HASIMGS + position) as Boolean) {
            var i = 0
            while (i < 99) {
                val img = ACache.get(this).getAsBitmap(position.toString() + "_" + i)
                if (img != null) {
                    val imageView = ImageView(this)
                    imageView.minimumWidth = 640
                    imageView.minimumHeight = 640
                    imageView.setPadding(16, 16, 16, 16)
                    imageView.scaleType = ImageView.ScaleType.FIT_XY
                    des_list.addView(imageView)
                    imageView.setImageBitmap(img)
                    images.add(img.bitmapToBase64()?.encrypt(AES_KEY)!! )
                } else {
                    i = 99
                }
                i++
            }
        }
        val textView = TextView(this)
        textView.text = text
        textView.textSize = 18f
        textView.setAllCaps(true)
        textView.gravity = Gravity.CENTER_HORIZONTAL
        des_list.addView(textView)
    }


    fun initYUN(): Unit {
        val goal =intent.getSerializableExtra(GOAL) as Goal
        for (img in goal.imgs){
            val imageView = ImageView(this)
            imageView.minimumWidth = 640
            imageView.minimumHeight = 640
            imageView.setPadding(16, 16, 16, 16)
            imageView.scaleType = ImageView.ScaleType.FIT_XY
            des_list.addView(imageView)
            imageView.setImageBitmap(img)
        }
        val textView = TextView(this)
        textView.text = goal.text
        textView.textSize = 18f
        textView.setAllCaps(true)
        textView.gravity = Gravity.CENTER_HORIZONTAL
        des_list.addView(textView)

    }

    fun upYUN(): Unit {
        var pImages=""
        for (image in images){
            pImages+="$image,"
        }
        Http.post{
            url= UP_YUN
            "id"-user.id
            "text"-text.encrypt(AES_KEY)
            "images"-pImages
            success {
                if (it.get<String>("code").equals("0")){
                    ACache.get(this@DesActivity).remove(position.toString() + "")
                    ACache.get(this@DesActivity).remove(HASIMGS + position)
                    finish()
                }
                it.get<String>("msg").toast(this@DesActivity)
            }
            fail { "网络错误".toast(this@DesActivity)  }
        }
    }

    companion object {
        val NUMBER = "NUMBER"
        val UID="UID"
        val GOAL="GOAL"
    }

}
