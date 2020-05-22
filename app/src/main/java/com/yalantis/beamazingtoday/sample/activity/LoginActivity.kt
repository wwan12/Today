package com.yalantis.beamazingtoday.sample.activity

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import com.aisino.tool.http.Http
import com.yalantis.beamazingtoday.sample.R
import com.yalantis.beamazingtoday.sample.expand.load
import com.yalantis.beamazingtoday.sample.expand.save
import com.yalantis.beamazingtoday.sample.expand.signPermissions
import com.yalantis.beamazingtoday.sample.expand.toast
import com.yalantis.beamazingtoday.sample.service.LOGIN
import com.yalantis.beamazingtoday.sample.user
import kotlinx.android.synthetic.main.activity_login.*
import java.lang.Exception
import android.annotation.SuppressLint
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.*


class LoginActivity : AppCompatActivity() {

    companion object{
       val NOT_AUTO_LOGIN="NOT_AUTO_LOGIN"
    }

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
            "请输入账号".toast(this)
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
                when (it.get<String>("code")) {
                    "0" -> {//登录成功的情况
                        user.user=login_phone_et.text.toString()
                        user.id=it.get<String>("id")
                        SavePwd()//记住账号密码
                        startActivity(Intent(this@LoginActivity,ExampleActivity::class.java))
                        finish()
                    }
                }
                it.get<String>("msg").toast(this@LoginActivity)

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



}