package com.yalantis.beamazingtoday.sample.activity

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.PopupWindow
import java.util.*
import android.os.Environment
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import com.aisino.tool.http.Http
import com.yalantis.beamazingtoday.sample.R
import com.yalantis.beamazingtoday.sample.expand.load
import com.yalantis.beamazingtoday.sample.expand.save
import com.yalantis.beamazingtoday.sample.expand.signPermissions
import com.yalantis.beamazingtoday.sample.expand.toast
import com.yalantis.beamazingtoday.sample.service.LOGIN
import kotlinx.android.synthetic.main.activity_login.*
import java.io.File
import java.io.FileOutputStream
import java.io.FileInputStream
import java.lang.Exception


class LoginActivity : AppCompatActivity() {

    companion object{
       val NOT_AUTO_LOGIN="NOT_AUTO_LOGIN"
    }

    var uid=""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        login_enter.setOnClickListener { Login() }
        login_phone_et.setText("user".load(this))
        login_pwd_et.setText("pwd".load(this))
        if (!login_phone_et.text.toString().equals("") && !login_pwd_et.text.toString().equals("") && intent.getIntExtra(NOT_AUTO_LOGIN, 0) == 0) {
            Login()
        } else {
            login_pwd_et.setText("")
        }
        signPermissions { }
    }



    fun Login(): Unit {
        startActivity(Intent(this@LoginActivity,ExampleActivity::class.java))
        if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)){
            "请给予运行权限".toast(this)
            signPermissions {  }
            return
        }
        if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)){
            "请给予运行权限".toast(this)
            signPermissions {  }
            return
        }
        if (login_phone_et.text.toString().equals("")) {
            "请输入手机号".toast(this)
            return
        }
        if (login_pwd_et.text.toString().equals("")) {
            "请输入密码".toast(this)
            return
        }
        Http.post {
            url = LOGIN
            "user"-login_phone_et.text.toString()
             "password"-login_pwd_et.text.toString()
            success {
                when (it.get<String>("status")) {
                    "0000", "0004", "0005" -> {//登录成功的情况
                        SavePwd()//记住账号密码
                        startActivity(Intent(this@LoginActivity,ExampleActivity::class.java))
                    }
                }
                it.get<String>("message").toast(this@LoginActivity)

            }
            fail { "网络错误".toast(this@LoginActivity) }
        }
    }

    fun SavePwd(): Unit {
        if (login_reb_pwd.isChecked) {
            login_phone_et.text.toString().save(this, "user")
            login_pwd_et.text.toString().save(this, "pwd")
        } else {

        }
    }

//    0000
//    登录成功
//    0001
//    登录失败
//    0002
//    停业整顿，不能登录
//    0003
//    达到最大登录人数
//    0004
//    登录成功，APP账号7天内到期
//    0005
//    登录成功，旅馆7天内到期
//    0006
//    登录失败，APP账号到期
//    0007
//    登录失败，旅馆到期
//    9998
//    报文格式错误
//    9999
//    其他错误

}