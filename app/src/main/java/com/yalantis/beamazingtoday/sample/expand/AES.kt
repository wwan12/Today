package com.yalantis.beamazingtoday.sample.expand

import java.security.SecureRandom
import javax.crypto.KeyGenerator
import android.text.TextUtils
import android.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec


private val HEX = "0123456789ABCDEF"
private val CBC_PKCS5_PADDING = "AES/CBC/PKCS5Padding"//AES是加密方式 CBC是工作模式 PKCS5Padding是填充模式
private val AES = "AES"//AES 加密
private val SHA1PRNG = "SHA1PRNG"//// SHA1PRNG 强随机种子算法, 要区别4.2以上版本的调用方法

/*
     * 生成随机数，可以当做动态的密钥 加密和解密的密钥必须一致，不然将不能解密
     */
fun generateKey(): String? {
    try {
        val localSecureRandom = SecureRandom.getInstance(SHA1PRNG)
        val bytes_key = ByteArray(20)
        localSecureRandom.nextBytes(bytes_key)
        return toHex(bytes_key)
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return null
}

// 对密钥进行处理
private fun getRawKey(seed: ByteArray): ByteArray {
    val kgen = KeyGenerator.getInstance(AES)
    //for android
    var sr: SecureRandom? = null
    // 在4.2以上版本中，SecureRandom获取方式发生了改变
    if (android.os.Build.VERSION.SDK_INT >= 24){
        sr = SecureRandom.getInstance(SHA1PRNG)
    }
    else if (android.os.Build.VERSION.SDK_INT >= 19) {
        sr = SecureRandom.getInstance(SHA1PRNG, "Crypto")
    } else {
        sr = SecureRandom.getInstance(SHA1PRNG)
    }
    // for Java
    // secureRandom = SecureRandom.getInstance(SHA1PRNG);
    sr!!.setSeed(seed)
    kgen.init(128, sr) //256 bits or 128 bits,192bits
    //AES中128位密钥版本有10个加密循环，192比特密钥版本有12个加密循环，256比特密钥版本则有14个加密循环。
    val skey = kgen.generateKey()
    return skey.getEncoded()
}

/*
     * 加密
     */
fun String.encrypt(key: String): String? {
    if (TextUtils.isEmpty(this)) {
        return this
    }
    try {
        val result = encrypt(key, this.toByteArray())
        return toHex(Base64.encode(result,Base64.DEFAULT))
    } catch (e: Exception) {
        e.printStackTrace()
    }

    return null
}

/*
    * 加密
    */
private fun encrypt(key: String, clear: ByteArray): ByteArray {
    val raw = getRawKey(key.toByteArray())
    val skeySpec = SecretKeySpec(raw, AES)
    val cipher = Cipher.getInstance(CBC_PKCS5_PADDING)
    cipher.init(Cipher.ENCRYPT_MODE, skeySpec, IvParameterSpec(ByteArray(cipher.getBlockSize())))
    return cipher.doFinal(clear)
}

/*
     * 解密
     */
fun String.decrypt(key: String): String? {
    if (TextUtils.isEmpty(this)) {
        return this
    }
    try {
        val enc = Base64.decode(this,Base64.DEFAULT)
        val result = decrypt(key, enc)
        return String(result)
    } catch (e: Exception) {
        e.printStackTrace()
    }

    return null
}

/*
     * 解密
     */
private fun decrypt(key: String, encrypted: ByteArray): ByteArray {
    val raw = getRawKey(key.toByteArray())
    val skeySpec = SecretKeySpec(raw, AES)
    val cipher = Cipher.getInstance(CBC_PKCS5_PADDING)
    cipher.init(Cipher.DECRYPT_MODE, skeySpec, IvParameterSpec(ByteArray(cipher.blockSize)))
    return cipher.doFinal(encrypted)
}

//二进制转字符
fun toHex(buf: ByteArray?): String {
    if (buf == null)
        return ""
    val result = StringBuffer(2 * buf.size)
    for (i in buf.indices) {
        appendHex(result, buf[i])
    }
    return result.toString()
}

private fun appendHex(sb: StringBuffer, b: Byte) {
    sb.append(HEX[b.toInt() shr 4 and 0x0f]).append(HEX[b.toInt() and 0x0f])
}