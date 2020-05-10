package com.yalantis.beamazingtoday.sample.activity

import android.app.Activity
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView

import com.yalantis.beamazingtoday.sample.R
import com.yalantis.beamazingtoday.sample.activity.ExampleActivity.Companion.HASIMGS
import com.yalantis.beamazingtoday.sample.expand.ACache
import com.yalantis.beamazingtoday.sample.expand.loge
import kotlinx.android.synthetic.main.activity_des.*

import java.io.FileNotFoundException
import java.net.URISyntaxException

/**
 * Created by lenovo on 2017/11/28.
 */

class DesActivity : Activity() {
    private var position: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_des)

        position = intent.getIntExtra(NUMBER,0)
        position.toString().loge()
        try {
            init()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        updata_to_w.setOnClickListener {  }

    }

    private fun init() {
        val text = ACache.get(this).getAsString(position.toString() + "")
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

    companion object {
        val NUMBER = "NUMBER"
    }
}
