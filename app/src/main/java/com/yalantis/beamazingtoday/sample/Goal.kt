package com.yalantis.beamazingtoday.sample

import android.graphics.Bitmap

import com.yalantis.beamazingtoday.interfaces.BatModel

import java.util.ArrayList

/**
 * Created by lenovo on 22.08.16.
 */
class Goal(var name: String?) : BatModel {

    private var isChecked: Boolean = false

    var hasImg: Boolean = false

    var imgs = ArrayList<Bitmap>()
        set(imgs) {
            hasImg = true
            field = imgs
        }


    override fun setChecked(checked: Boolean) {
        isChecked = checked
    }

//    fun setImgs(imgs: ArrayList<Bitmap>) {
//        this.imgs = imgs
//        hasImg = true
//    }

    override fun hasImg(): Boolean {
        return hasImg
    }

    override fun isChecked(): Boolean {
        return isChecked
    }

    override fun getText(): String? {
        return name
    }

}
