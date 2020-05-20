package com.yalantis.beamazingtoday.sample

import com.yalantis.beamazingtoday.sample.activity.AESActivity


val user=User("","")

val mainKey= AESActivity.Key("","")
//正在使用的密钥
var AES_KEY=""

data class User(var user:String,var id:String)