package com.yalantis.beamazingtoday.sample.activity

import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.yalantis.beamazingtoday.sample.R
import kotlinx.android.synthetic.main.item_key.view.*

class KeyAdapter(val activity: AppCompatActivity, val keyList: ArrayList<AESActivity.Key>):BaseAdapter() {
    override fun getView(p0: Int, p1: View?, p2: ViewGroup?): View {
        val view= activity.layoutInflater.inflate(R.layout.item_key, p2,false)
        view.key_name.text=keyList[p0].name
        view.key_v.text=keyList[p0].key
        return view
    }

    override fun getItem(p0: Int): Any {
        return keyList[p0]
    }

    override fun getItemId(p0: Int): Long {
        return p0.toLong()
    }

    override fun getCount(): Int {
        return keyList.size
    }

}