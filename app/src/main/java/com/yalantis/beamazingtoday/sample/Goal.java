package com.yalantis.beamazingtoday.sample;

import android.graphics.Bitmap;

import com.yalantis.beamazingtoday.interfaces.BatModel;

import java.util.ArrayList;

/**
 * Created by lenovo on 22.08.16.
 */
public class Goal implements BatModel {

    private String name;

    private boolean isChecked;

    public boolean hasImg;

    public ArrayList<Bitmap> imgs;

    public Goal(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    public void setImgs(ArrayList<Bitmap> imgs){
        this.imgs=imgs;
        hasImg=true;
    }

    @Override
    public boolean hasImg() {
        return hasImg;
    }

    @Override
    public boolean isChecked() {
        return isChecked;
    }

    @Override
    public String getText() {
        return getName();
    }

}
