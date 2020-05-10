package com.yalantis.beamazingtoday.sample.service

import android.app.Activity
import com.aisino.tool.http.Http
import com.aisino.tool.http.SuccessData
import com.yalantis.beamazingtoday.sample.expand.toast

val BASEURL=""
val LOGIN=BASEURL+""


//fun Activity.login(user:String, pwd: String, scall:(s:SuccessData)->Unit): Unit {
//    Http.post {
//        url = LOGIN
//                "phone"-user
//                "password"-pwd
//        success {
//            when (it.get<String>("status")) {
//                "0000", "0004", "0005" -> {//登录成功的情况
//
//                }
//            }
//            it.get<String>("message").toast(this@LoginActivity)
//
//        }
//        fail { "网络错误".toast(this@LoginActivity) }
//    }
//}
