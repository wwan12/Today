package com.yalantis.beamazingtoday.sample;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.yalantis.beamazingtoday.sample.cache.ACache;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

import static com.yalantis.beamazingtoday.sample.ExampleActivity.HASIMGS;

/**
 * Created by lenovo on 2017/11/28.
 */

public class DesActivity extends Activity {
    private LinearLayout list;
    private int position;
    public final static String NUMBER = "NUMBER";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_des);
        list = (LinearLayout) findViewById(R.id.des_list);

        position = getIntent().getExtras().getInt(NUMBER);
        try {
            init();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void init() throws URISyntaxException, FileNotFoundException {
        String text = ACache.get(this).getAsString(position + "");
        if ((boolean) ACache.get(this).getAsObject(HASIMGS + position)) {
            for (int i = 0; i < 99; i++) {
                Bitmap img = ACache.get(this).getAsBitmap(position + "_" + i);
                if (img != null) {
                    ImageView imageView = new ImageView(this);
                    imageView.setImageBitmap(img);
//                    imageView.setMaxWidth(640);
//                    imageView.setMaxHeight(320);
                    imageView.setPadding(16,16,16,16);
                    imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                    list.addView(imageView);
                } else {
                    i = 99;
                }
            }
        }
        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setTextSize(20);
        textView.setAllCaps(true);
        textView.setGravity(Gravity.CENTER_HORIZONTAL);
        list.addView(textView);
    }
}
