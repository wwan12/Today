package com.yalantis.beamazingtoday.sample.activity

import android.content.ContentUris
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.Toolbar
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.View
import android.widget.TextView
import com.aisino.tool.http.Http

import com.yalantis.beamazingtoday.interfaces.AnimationType
import com.yalantis.beamazingtoday.interfaces.BatModel
import com.yalantis.beamazingtoday.listeners.BatListener
import com.yalantis.beamazingtoday.listeners.OnItemClickListener
import com.yalantis.beamazingtoday.listeners.OnOutsideClickedListener
import com.yalantis.beamazingtoday.sample.AES_KEY
import com.yalantis.beamazingtoday.sample.Goal
import com.yalantis.beamazingtoday.sample.R
import com.yalantis.beamazingtoday.sample.expand.ACache
import com.yalantis.beamazingtoday.sample.expand.generateKey
import com.yalantis.beamazingtoday.sample.expand.toast
import com.yalantis.beamazingtoday.sample.service.DELECT_YUN
import com.yalantis.beamazingtoday.sample.service.QUERY_YUN
import com.yalantis.beamazingtoday.sample.user
import com.yalantis.beamazingtoday.ui.adapter.BatAdapter
import com.yalantis.beamazingtoday.ui.animator.BatItemAnimator
import com.yalantis.beamazingtoday.ui.callback.BatCallback
import com.yalantis.beamazingtoday.ui.widget.BatRecyclerView
import com.yalantis.beamazingtoday.util.TypefaceUtil
import kotlinx.android.synthetic.main.activity_example.*

import java.io.File
import java.util.ArrayList
import java.util.Calendar

import pub.devrel.easypermissions.EasyPermissions

/**
 * Created by lenovo on 2017/11/28.
 */

class ExampleActivity : AppCompatActivity(), BatListener, OnItemClickListener, OnOutsideClickedListener, EasyPermissions.PermissionCallbacks {

    private var mAdapter: BatAdapter? = null
    private var mGoals: MutableList<BatModel>? = null
    private var mAnimator: BatItemAnimator? = null
    private var imgCache: ArrayList<Bitmap>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_example)
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH) + 1
        val day = c.get(Calendar.DAY_OF_MONTH)
        text_title.text = "$year-$month-$day"

        setSupportActionBar(toolbar)
        supportActionBar?.setTitle("")
        toolbar.getChildAt(0).setOnClickListener { startActivity(Intent(this,AESActivity::class.java)) }
        text_title.typeface = TypefaceUtil.getAvenirTypeface(this)

        mAnimator = BatItemAnimator()

        bat_recycler_view.view.layoutManager = LinearLayoutManager(this)
        mGoals = ArrayList()

        val itemTouchHelper = ItemTouchHelper(BatCallback(this))
        itemTouchHelper.attachToRecyclerView(bat_recycler_view.view)
        bat_recycler_view.view.itemAnimator = mAnimator
        bat_recycler_view.setAddItemListener(this)

        root.setOnClickListener { bat_recycler_view.revertAnimation() }

        mAdapter=BatAdapter(mGoals, this, mAnimator).setOnItemClickListener(this).setOnOutsideClickListener(this)
        bat_recycler_view.view.setAdapter(mAdapter)

        AES_KEY=generateKey()!!

        init()
        initYUN()
    }

    private fun init() {
        for (i in 0 until Integer.MAX_VALUE) {
            val text = ACache.get(this).getAsString(i.toString())
            if (text != null) {
                val goal = Goal(text)
                goal.hasImg = ACache.get(this).getAsObject(HASIMGS + i) as Boolean
                mGoals!!.add(goal)
            } else {
                return
            }
        }
    }

    fun initYUN(): Unit {
        Http.post{
           url= QUERY_YUN
            "id"- user.id
            success {
                if (it.get<String>("code").equals("0")){
                    for (yun in it.get<ArrayList<MutableMap<String,Any>>>("data")){
                        val goal = Goal(yun["text"].toString())
                        goal.uid=yun["Uid"].toString()
                        goal.hasImg =yun["hasImg"].toString().equals("1")
//                                (yun["images"] as ArrayList<String>).
                        mGoals!!.add(goal)
                    }
                    mAdapter?.notifyDataSetChanged()
                }
            }
            fail { "网络错误".toast(this@ExampleActivity)  }
        }
    }

    override fun add(string: String) {
        val goal = Goal(string)
        if (imgCache != null && imgCache!!.size != 0) {
            goal.imgs=imgCache!!
        }
        addRefresh(string, goal.hasImg)//维护缓存表
        mGoals!!.add(0, goal)
        mAdapter!!.notify(AnimationType.ADD, 0)
        imgCache = null
    }

    override fun delete(position: Int) {
        val g= mGoals!![position]
        if (!g.uid.equals("")){
            deleteYUN(g.uid)
        }
        deleteRefresh(position)
        mGoals!!.removeAt(position)
        mAdapter!!.notify(AnimationType.REMOVE, position)
    }

    override fun move(from: Int, to: Int) {
        if (from > to) {
            moveRefresh(to, from)
        } else {
            moveRefresh(from, to)
        }

        if (from >= 0 && to >= 0) {
            mAnimator!!.setPosition(to)
            val model = mGoals!![from]
            mGoals!!.remove(model)
            mGoals!!.add(to, model)
            mAdapter!!.notify(AnimationType.MOVE, from, to)
            if (from == 0 || to == 0) {
                bat_recycler_view!!.view.scrollToPosition(Math.min(from, to))
            }
        }
    }

    override fun onClick(item: BatModel, position: Int) {
        val intent = Intent(this, DesActivity::class.java)
        intent.putExtra(DesActivity.NUMBER, position)
        intent.putExtra(DesActivity.UID, mGoals!![position].uid)
        startActivity(intent)
    }

    override fun onOutsideClicked() {
        bat_recycler_view!!.revertAnimation()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (imgCache == null) {
            imgCache = ArrayList()
        }
        if (requestCode == 1) {
            if (data != null && data.hasExtra("data")) {
                val thumbnail = data.getParcelableExtra<Bitmap>("data")
                imgCache!!.add(thumbnail)
            }
        } else if (requestCode == 2) {
            if (data != null && data.data != null) {
                val f = File(handleImageOnKitKat(data.data!!.toString())!!)
                imgCache!!.add(BitmapFactory.decodeFile(f.path))
            }
        }

    }

    private fun addRefresh(string: String, hasImg: Boolean) {
        for (i in mGoals!!.indices.reversed()) {
            val text = ACache.get(this).getAsString(i.toString())
            ACache.get(this).put((i + 1).toString(), text)
            val hImg = ACache.get(this).getAsObject(HASIMGS + i) as Boolean
            ACache.get(this).put(HASIMGS + (i + 1), hImg)
            if (hImg) {
                var img: Bitmap?
                var j = 0
                while (j < 99) {
                    img = ACache.get(this).getAsBitmap("${i}_$j")
                    if (img != null) {
                        ACache.get(this).put("${i + 1}_$j", img)
                    } else {
                        j = 99
                    }
                    j++
                }
            }
        }
        ACache.get(this).put(0.toString() + "", string)
        ACache.get(this).put(HASIMGS + 0, hasImg)
        if (hasImg) {
            for (i in imgCache!!.indices) {
                ACache.get(this).put("0_$i", imgCache!![i])
            }
        }


    }


    private fun deleteRefresh(index: Int) {//删除位之后数据前移
        ACache.get(this).remove(index.toString() + "")
        ACache.get(this).remove(HASIMGS + index)
        val goal = mGoals!![index] as Goal
        if (goal.hasImg) {
            for (i in 0..98) {
                if (!ACache.get(this).remove(index.toString() + "_" + i)) {
                    break
                }
            }
        }
        for (i in index + 1 until mGoals!!.size) {
            val text = ACache.get(this).getAsString(i.toString())
            ACache.get(this).put((i - 1).toString(), text)
            val hImg = ACache.get(this).getAsObject(HASIMGS + i) as Boolean
            ACache.get(this).put(HASIMGS + (i - 1), hImg)
            if (hImg) {
                var img: Bitmap?
                var j = 0
                while (j < 99) {
                    img = ACache.get(this).getAsBitmap("${i}_$j")
                    if (img != null) {
                        ACache.get(this).put("${i- 1}_$j", img)
                    } else {
                        j = 99
                    }
                    j++
                }
            }
        }
    }

    fun deleteYUN(uid:String){
        Http.post{
            url= DELECT_YUN
            "id"- user.id
            "uid"-uid
            success {
                if (it.get<String>("code").equals("0")){

                }
                it.get<String>("msg").toast(this@ExampleActivity)
            }
            fail { "网络错误".toast(this@ExampleActivity)  }
        }
    }


    private fun moveRefresh(form: Int, to: Int) {
        val goal = Goal(ACache.get(this).getAsString(form.toString() + ""))
        goal.hasImg = ACache.get(this).getAsObject(HASIMGS + form) as Boolean
        if (goal.hasImg) {
            goal.imgs = ArrayList()
            var img: Bitmap?
            var j = 0
            while (j < 99) {
                img = ACache.get(this).getAsBitmap(form.toString() + "_" + j)
                if (img != null) {
                    goal.imgs.add(img)
                } else {
                    j = 99
                }
                j++
            }
        }
        if (goal.hasImg) {//删除form全部图片
            for (i in 0..98) {
                if (!ACache.get(this).remove(form.toString() + "_" + i)) {
                    break
                }
            }
        }//取出from位数据

        for (i in form + 1 until mGoals!!.size) {
            val text = ACache.get(this).getAsString(i .toString())
            ACache.get(this).put((i - 1).toString(), text)
            val hImg = ACache.get(this).getAsObject(HASIMGS + i) as Boolean
            ACache.get(this).put(HASIMGS + (i - 1), hImg)
            if (hImg) {
                var img: Bitmap?
                var j = 0
                while (j < 99) {
                    img = ACache.get(this).getAsBitmap("${i}_$j")
                    if (img != null) {
                        ACache.get(this).put("${i- 1}_$j", img)
                    } else {
                        j = 99
                    }
                    j++
                }
            }
        }//from位之后数据整体前移

        for (i in mGoals!!.size - 1 downTo to + 1) {
            val text = ACache.get(this).getAsString(i .toString())
            ACache.get(this).put((i + 1).toString(), text)
            val hImg = ACache.get(this).getAsObject(HASIMGS + i) as Boolean
            ACache.get(this).put(HASIMGS + (i + 1), hImg)
            if (hImg) {
                var img: Bitmap?
                var j = 0
                while (j < 99) {
                    img = ACache.get(this).getAsBitmap("${i}_$j")
                    if (img != null) {
                        ACache.get(this).put("${i+1}_$j", img)
                    } else {
                        j = 99
                    }
                    j++
                }
            }
        }//to位之后数据整体后移

        ACache.get(this).put(to.toString() + "", goal.text)
        ACache.get(this).put(HASIMGS + to, goal.hasImg)
        if (goal.imgs != null) {
            for (i in goal.imgs.indices) {
                ACache.get(this).put(to.toString() + "_" + i, goal.imgs[i])
            }//form数据插入to位
        }
    }

    //读取uri
    private fun handleImageOnKitKat(uriString: String): String? {
        var imagePath: String? = null
        val uri = Uri.parse(uriString)
        if (DocumentsContract.isDocumentUri(this, uri)) {
            val docId = DocumentsContract.getDocumentId(uri)
            if ("com.android.providers.media.documents" == uri.authority) {
                val id = docId.split(":".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()[1]
                val selection = MediaStore.Images.Media._ID + "=" + id
                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection)
            } else if ("com.android.providers.downloads.documents" == uri.authority) {
                val contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"),
                        java.lang.Long.valueOf(docId))
                imagePath = getImagePath(contentUri, null)
            }
        } else if ("content".equals(uri.scheme, ignoreCase = true)) {
            imagePath = getImagePath(uri, null)
        }
        return imagePath
    }

    private fun getImagePath(uri: Uri, selection: String?): String? {
        var path: String? = null
        val cursor = contentResolver.query(uri, null, selection, null, null)
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA))
            }

            cursor.close()
        }
        return path
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }


    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {

    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {

    }

    companion object {
        val HASIMGS = "hasImg_"
    }
}
