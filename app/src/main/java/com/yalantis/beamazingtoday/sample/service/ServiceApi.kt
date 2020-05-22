package com.yalantis.beamazingtoday.sample.service


val BASEURL="https://192.168.0.109:44328/"

val LOGIN=BASEURL+"UserInfoes/Login"

val UP_YUN=BASEURL+"UserDatas/uploadData"

val DELECT_YUN=BASEURL+"UserDatas/deleteData"

val QUERY_YUN=BASEURL+"UserDatas/getUserDataList"


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
