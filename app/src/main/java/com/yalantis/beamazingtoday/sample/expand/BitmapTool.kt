package com.yalantis.beamazingtoday.sample.expand

/**
 * Created by lenovo on 2017/12/5.
 */

import android.app.Activity
import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import java.io.*
import java.net.URL
import android.graphics.Bitmap
import android.view.WindowManager
import android.graphics.RectF
import android.graphics.BitmapFactory
import android.text.Layout
import android.text.StaticLayout
import android.graphics.Typeface
import android.text.TextPaint
import android.widget.ImageView
import java.nio.channels.FileChannel
import kotlin.concurrent.thread

/**
 * Android根据设备屏幕尺寸和dpi的不同，给系统分配的单应用程序内存大小也不同，具体如下表
 *
 * 屏幕尺寸 DPI 应用内存
 * small / normal / large ldpi / mdpi 16MB
 * small / normal / large tvdpi / hdpi 32MB
 * small / normal / large xhdpi 64MB
 * small / normal / large 400dpi 96MB
 * small / normal / large xxhdpi 128MB
 * -------------------------------------------------------
 * xlarge mdpi 32MB
 * xlarge tvdpi / hdpi 64MB
 * xlarge xhdpi 128MB
 * xlarge 400dpi 192MB
 * xlarge xxhdpi 256MB
 */

/**
 * 图片加载及转化工具 ----------------------------------------------------------------------- 延伸：一个Bitmap到底占用多大内存？系统给每个应用程序分配多大内存？ Bitmap占用的内存为：像素总数
 * * 每个像素占用的内存。在Android中， Bitmap有四种像素类型：ARGB_8888、ARGB_4444、ARGB_565、ALPHA_8， 他们每个像素占用的字节数分别为4、2、2、1。因此，一个2000*1000的ARGB_8888
 * 类型的Bitmap占用的内存为2000*1000*4=8000000B=8MB。
 *
 *
 */

/**
 * 把batmap 转file
 * @param bitmap
 * @param filepath
 */
fun Bitmap.saveBitmapFile(filepath: String): File? {
    val file = File(filepath)//将要保存图片的路径
    try {
        val bos = BufferedOutputStream(FileOutputStream(file))
        this.compress(Bitmap.CompressFormat.JPEG, 100, bos)
        bos.flush()
        bos.close()
    } catch (e: IOException) {
        e.printStackTrace()
        return null
    }
    return file
}

/**
 * 从网上下载图片
 *
 * @param imageUrl
 * @return
 */
fun ImageView.downloadBitmap(imageUrl: String){
    var bitmap: Bitmap? = null
    thread(start = true) {
        bitmap = BitmapFactory.decodeStream(URL(imageUrl).openStream())
        (this.context as Activity).runOnUiThread {
            this.setImageBitmap(bitmap)
        }
    }
}

/**
 * drawable 转bitmap
 *
 * @param drawable
 * @return
 */
fun Drawable.drawable2Bitmap(): Bitmap {
    val bitmap = Bitmap.createBitmap(intrinsicWidth, intrinsicHeight,
            if (opacity != PixelFormat.OPAQUE) Bitmap.Config.ARGB_8888 else Bitmap.Config.RGB_565)
    val canvas = Canvas(bitmap)
    // canvas.setBitmap(bitmap);
    setBounds(0, 0, intrinsicWidth, intrinsicHeight)
    draw(canvas)
    return bitmap
}

/**
 * bitmap 转 drawable
 *
 * @param bm
 * @return
 */
fun Bitmap.bitmap2Drable(): Drawable {
    return BitmapDrawable(this)
}


/**
 * 把图片转换成字节数组
 * @return
 */
fun Bitmap.bitmap2Byte(): ByteArray? {
    val outBitmap = Bitmap.createScaledBitmap(this, 150, this.height * 150 / this.width, true)
    if (this != outBitmap) {
        this.recycle()
    }
    var compressData: ByteArray? = null
    val baos = ByteArrayOutputStream()
    try {
        try {
            outBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        compressData = baos.toByteArray()
        baos.close()
    } catch (e: IOException) {
        e.printStackTrace()
    }

    return compressData
}

/**
 * 缩放图片
 *
 * @param bitmap
 * 原图片
 * @param newWidth
 * @param newHeight
 * @return
 */
fun Bitmap.setBitmapSize( newWidth: Int, newHeight: Int): Bitmap {
    val width = this.width
    val height = this.height
    val scaleWidth = newWidth * 1.0f / width
    val scaleHeight = newHeight * 1.0f / height
    val matrix = Matrix()
    matrix.postScale(scaleWidth, scaleHeight)
    return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
}

/**
 * 保持比例缩放图片
 *
 * @param bitmap
 * req 长边长度
 * @return
 */
fun Bitmap.setBitmapSize(req: Int): Bitmap {
    if (this.width==0||this.height==0){
        return this
    }
    var h=0
    var w=0
    if (this.width>this.height){
        h= (this.height / (this.width.toDouble()/req)).toInt()
        w=req
    }else{
        w= (this.width / (this.height.toDouble()/req)).toInt()
        h=req
    }
    w=w-w%4
    h=h-h%4
   return this.setBitmapSize(w,h)
}


/**
 * 计算图片的缩放大小 如果==1，表示没变化，==2，表示宽高都缩小一倍 ----------------------------------------------------------------------------
 * inSampleSize是BitmapFactory.Options类的一个参数，该参数为int型， 他的值指示了在解析图片为Bitmap时在长宽两个方向上像素缩小的倍数。inSampleSize的默认值和最小值为1（当小于1时，解码器将该值当做1来处理），
 * 且在大于1时，该值只能为2的幂（当不为2的幂时，解码器会取与该值最接近的2的幂）。 例如，当inSampleSize为2时，一个2000*1000的图片，将被缩小为1000*500，相应地， 它的像素数和内存占用都被缩小为了原来的1/4：
 *
 * @param options
 * @param reqWidth
 * @param reqHeight
 * @return
 */
private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
    // 原始图片的宽高
    val height = options.outHeight
    val width = options.outWidth
    var inSampleSize = 1
    if (height > reqHeight || width > reqWidth) {
        val halfHeight = height / 2
        val halfWidth = width / 2
        // 在保证解析出的bitmap宽高分别大于目标尺寸宽高的前提下，取可能的inSampleSize的最大值
        while (halfHeight / inSampleSize > reqHeight && halfWidth / inSampleSize > reqWidth) {
            inSampleSize *= 2
        }
    }
    return inSampleSize
}

/**
 * 根据计算出的inSampleSize生成Bitmap(此时的bitmap是经过缩放的图片)
 *
 * @param res
 * @param resId
 * @param reqWidth
 * @param reqHeight
 * @return
 */
fun Activity.decodeSampledBitmapFromResource(resId: Int, reqWidth: Int, reqHeight: Int): Bitmap {
    // 首先设置 inJustDecodeBounds=true 来获取图片尺寸
    val options = BitmapFactory.Options()
    /**
     * inJustDecodeBounds属性设置为true，decodeResource()方法就不会生成Bitmap对象，而仅仅是读取该图片的尺寸和类型信息：
     */
    options.inJustDecodeBounds = true
    BitmapFactory.decodeResource(this.resources, resId, options)

    // 计算 inSampleSize 的值
    options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)

    // 根据计算出的 inSampleSize 来解码图片生成Bitmap
    options.inJustDecodeBounds = false
    return BitmapFactory.decodeResource(this.resources, resId, options)
}

/**
 * 将图片保存到本地时进行压缩, 即将图片从Bitmap形式变为File形式时进行压缩,
 * 特点是: File形式的图片确实被压缩了, 但是当你重新读取压缩后的file为 Bitmap是,它占用的内存并没有改变
 *
 * @param bmp
 * @param file
 */
fun Bitmap.compressBmpToFile(file: File) {
    val baos = ByteArrayOutputStream()
    var options = 80// 个人喜欢从80开始,
    this.compress(Bitmap.CompressFormat.JPEG, options, baos)
    while (baos.toByteArray().size / 1024 > 100) {
        baos.reset()
        options -= 10
        this.compress(Bitmap.CompressFormat.JPEG, options, baos)
    }
    try {
        val fos = FileOutputStream(file)
        fos.write(baos.toByteArray())
        fos.flush()
        fos.close()
    } catch (e: Exception) {
        e.printStackTrace()
    }

}

/**
 * 将图片从本地读到内存时,进行压缩 ,即图片从File形式变为Bitmap形式
 * 特点: 通过设置采样率, 减少图片的像素, 达到对内存中的Bitmap进行压缩
 * @param srcPath
 * @return
 */
fun File.compressImageFromFile(pixWidth: Float, pixHeight: Float): Bitmap {
    val options = BitmapFactory.Options()
    options.inJustDecodeBounds = true// 只读边,不读内容
    var bitmap:Bitmap

    options.inJustDecodeBounds = false
    val w = options.outWidth
    val h = options.outHeight
    var scale = 1
    if (w > h && w > pixWidth) {
        scale = (options.outWidth / pixWidth).toInt()
    } else if (w < h && h > pixHeight) {
        scale = (options.outHeight / pixHeight).toInt()
    }
    if (scale <= 0)
        scale = 1
    options.inSampleSize = scale// 设置采样率

    options.inPreferredConfig = Bitmap.Config.ARGB_8888// 该模式是默认的,可不设
    options.inPurgeable = true// 同时设置才会有效
    options.inInputShareable = true// 。当系统内存不够时候图片自动被回收

    bitmap = BitmapFactory.decodeFile(this.absolutePath, options)
    // return compressBmpFromBmp(bitmap);//原来的方法调用了这个方法企图进行二次压缩
    // 其实是无效的,大家尽管尝试
    return bitmap
}

/**将图片改为圆角类型
 * @param bitmap
 * @param pixels
 * @return
 */
fun Bitmap.toRoundCorner(pixels: Int): Bitmap {
    val output = Bitmap.createBitmap(this.width, this.height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(output)
    val color = -0xbdbdbe
    val paint = Paint()
    val rect = Rect(0, 0, this.width, this.height)
    val rectF = RectF(rect)
    val roundPx = pixels.toFloat()
    paint.isAntiAlias = true
    canvas.drawARGB(0, 0, 0, 0)
    paint.color = color
    canvas.drawRoundRect(rectF, roundPx, roundPx, paint)
    paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
    canvas.drawBitmap(this, rect, rect, paint)
    return output
}


/**
 * 给图片加水印，网上
 * 右下角
 * @param src       原图
 * @param watermark 水印
 * @return 加水印的原图
 */
fun Bitmap.WaterMask(context: Context, watermark: Bitmap): Bitmap {
    var src = this
    var watermark = watermark
    val w = this.width
    val h = this.height

    // 设置原图想要的大小
    val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    val newWidth = wm.defaultDisplay.width
//    val newWidth = ScreenUtils.getScreenWidth()
    val newHeight = h * (newWidth / w)
    // 计算缩放比例
    val scaleWidth = newWidth / w
    val scaleHeight = newHeight / h
    val matrix = Matrix()
    matrix.postScale(scaleWidth.toFloat(), scaleHeight.toFloat())
    src = Bitmap.createBitmap(src, 0, 0, w, h, matrix, true)

    //根据bitmap缩放水印图片
    val w1 = (w / 5).toFloat()
    val h1 = w1 / 5
    //获取原始水印图片的宽、高
    var w2 = watermark.width
    var h2 = watermark.height

    //计算缩放的比例
    val scalewidth = w1 / w2
    val scaleheight = h1 / h2

    val matrix1 = Matrix()
    matrix1.postScale(0.4.toFloat(), 0.4.toFloat())

    watermark = Bitmap.createBitmap(watermark, 0, 0, w2, h2, matrix1, true)
    //获取新的水印图片的宽、高
    w2 = watermark.width
    h2 = watermark.height

    val result = Bitmap.createBitmap(src.width, src.height, Bitmap.Config.ARGB_8888)// 创建一个新的和SRC长度宽度一样的位图
    val cv = Canvas(result)
    //在canvas上绘制原图和新的水印图
    cv.drawBitmap(src, 0f, 0f, null)
    //水印图绘制在画布的右下角，距离右边和底部都为20
    cv.drawBitmap(watermark, src.width - w2 - 20f, src.height - h2 - 20f, null)
    cv.save()
    cv.restore()

    return result
}

/**
 * 左下角添加水印（多行，图标 + 文字）
 * 参考资料：
 * Android 对Canvas的translate方法总结    https://blog.csdn.net/u013681739/article/details/49588549
 * @param photo
 */
fun Bitmap.addWaterMark(context: Context, textList: List<String>, iconIdList: List<Int>, isShowIcon: Boolean): Bitmap? {
    var newBitmap:Bitmap? = null
    var photo=this
    try {
        val srcWidth = photo.width
        val srcHeight = photo.height
        val unitHeight = if (srcHeight > srcWidth) srcWidth / 30 else srcHeight / 25
        var marginBottom = unitHeight
        //创建一个bitmap
        if (!newBitmap!!.isMutable) {
            newBitmap = this.copy(context.cacheDir.path)
        }
        //将该图片作为画布
        val canvas = Canvas(newBitmap)

        // 设置画笔
        val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG or Paint.DEV_KERN_TEXT_FLAG)
        textPaint.textSize = unitHeight.toFloat()// 字体大小
        textPaint.typeface = Typeface.DEFAULT// 采用默认的宽度
        textPaint.color = Color.WHITE// 采用的颜色v

        val bounds = Rect()
        val gText = "hello world!"
        textPaint.getTextBounds(gText, 0, gText.length, bounds)

        val iconWidth = bounds.height()//图片宽度
        val maxTextWidth = srcWidth - unitHeight * 3 - iconWidth//最大文字宽度

        for (i in textList.indices.reversed()) {
            val text = textList[i]
            val iconId = iconIdList[i]

            canvas.save()//锁画布(为了保存之前的画布状态)

            //文字处理
            val layout = StaticLayout(text, textPaint, maxTextWidth, Layout.Alignment.ALIGN_NORMAL,
                    1.0f, 0.0f, true) // 确定换行
            //在画布上绘制水印图片
            if (isShowIcon) {
                val watermark = BitmapFactory.decodeResource(context.resources, iconId)
                val iconHeight = iconWidth * (watermark.height * 1000 / watermark.width) / 1000//维持图片宽高比例，也可以简单粗暴 iconHeight = iconWidth;
                //图片相对文字位置居中
                val rectF = RectF(unitHeight.toFloat(), (srcHeight - marginBottom - layout.height / 2 - iconHeight / 2).toFloat(), (unitHeight + iconWidth).toFloat(), (srcHeight - marginBottom - layout.height / 2 + iconHeight / 2).toFloat())
                canvas.drawBitmap(watermark, null, rectF, null)//限定图片显示范围
            }

            //绘制文字
            canvas.translate(if (isShowIcon) unitHeight + iconWidth + unitHeight.toFloat() else unitHeight.toFloat(), srcHeight - layout.height - marginBottom.toFloat()) // 设定画布位置
            layout.draw(canvas) // 绘制水印

            //marginBottom 更新
            marginBottom = marginBottom + (unitHeight + layout.height)
            canvas.restore()//把当前画布返回（调整）到上一个save()状态之前
        }
        // 保存
        canvas.save()
        // 存储
        canvas.restore()

    } catch (e: Exception) {
        e.printStackTrace()
        return newBitmap
    }

    return newBitmap
}

/**
 * 根据原位图生成一个新的位图，并将原位图所占空间释放
 *
 * @param srcBmp 原位图
 * @return 新位图
 */
fun Bitmap.copy(path:String): Bitmap {
    var destBmp: Bitmap? = null
    try {
        // 创建一个临时文件
        val file = File(path + "temppic/tmp.txt")
        if (file.exists()) {// 临时文件 ， 用一次删一次
            file.delete()
        }
        file.getParentFile().mkdirs()
        val randomAccessFile = RandomAccessFile(file, "rw")
        val width = this.width
        val height = this.height
        val channel = randomAccessFile.getChannel()
        val map = channel.map(FileChannel.MapMode.READ_WRITE, 0, width * height * 4L)
        // 将位图信息写进buffer
        this.copyPixelsToBuffer(map)
        // 释放原位图占用的空间
        this.recycle()
        // 创建一个新的位图
        destBmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        map.position(0)
        // 从临时缓冲中拷贝位图信息
        destBmp!!.copyPixelsFromBuffer(map)
        channel.close()
        randomAccessFile.close()
        file.delete()
    } catch (ex: Exception) {
        ex.printStackTrace()
        destBmp = null
        return this
    }

    return destBmp
}



