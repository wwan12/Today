package com.yalantis.beamazingtoday.sample.expand

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast

fun String.save(activity: AppCompatActivity, key:String): Unit {
    activity.getSharedPreferences("activity", AppCompatActivity.MODE_PRIVATE).edit().putString(key,this).apply()
}

fun String.load(activity: AppCompatActivity): String {
    return activity.getSharedPreferences("activity", AppCompatActivity.MODE_PRIVATE).getString(this,"").toString()
}

fun String.loge(tag:String="tag"): Unit {
    Log.e(tag, this)
}

fun String.toast(context: Context): Unit {
    Toast.makeText(context, this, Toast.LENGTH_SHORT).show()
}



