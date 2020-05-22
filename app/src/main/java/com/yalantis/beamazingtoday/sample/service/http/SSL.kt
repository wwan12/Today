package com.yalantis.beamazingtoday.sample.service.http

import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.*

object SSL {     //获取这个SSLSocketFactory
    val sslSocketFactory: SSLSocketFactory
        get() {
            try {
                val sslContext = SSLContext.getInstance("SSL")
                sslContext.init(null, trustManager, SecureRandom())
                return sslContext.getSocketFactory()
            } catch (e: Exception) {
                throw RuntimeException(e)
            }

        }     //获取TrustManager
    private val trustManager: Array<TrustManager>
        get() = arrayOf<TrustManager>(object : X509TrustManager {
            override fun getAcceptedIssuers(): Array<X509Certificate> {
                return arrayOf<X509Certificate>()
            }

            override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
            override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
        })
    //获取HostnameVerifier
    val hostnameVerifier: HostnameVerifier
        get() {
            return object : HostnameVerifier {
                override fun verify(s: String, sslSession: SSLSession): Boolean {
                    return true
                }
            }
        }
}