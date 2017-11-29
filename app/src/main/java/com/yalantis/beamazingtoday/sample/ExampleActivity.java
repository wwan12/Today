package com.yalantis.beamazingtoday.sample;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.yalantis.beamazingtoday.interfaces.AnimationType;
import com.yalantis.beamazingtoday.interfaces.BatModel;
import com.yalantis.beamazingtoday.listeners.BatListener;
import com.yalantis.beamazingtoday.listeners.OnItemClickListener;
import com.yalantis.beamazingtoday.listeners.OnOutsideClickedListener;
import com.yalantis.beamazingtoday.sample.cache.ACache;
import com.yalantis.beamazingtoday.ui.adapter.BatAdapter;
import com.yalantis.beamazingtoday.ui.animator.BatItemAnimator;
import com.yalantis.beamazingtoday.ui.callback.BatCallback;
import com.yalantis.beamazingtoday.ui.widget.BatRecyclerView;
import com.yalantis.beamazingtoday.util.TypefaceUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;

import static com.yalantis.beamazingtoday.sample.DesActivity.NUMBER;

/**
 * Created by lenovo on 2017/11/28.
 */

public class ExampleActivity extends AppCompatActivity implements BatListener, OnItemClickListener, OnOutsideClickedListener , EasyPermissions.PermissionCallbacks{

    private BatRecyclerView mRecyclerView;
    private BatAdapter mAdapter;
    private List<BatModel> mGoals;
    private BatItemAnimator mAnimator;
    private TextView title;
    private ArrayList<Bitmap> imgCache;
    public final static String HASIMGS = "hasImg_";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_example);
        title = (TextView) findViewById(R.id.text_title);
        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH) + 1;
        int day = c.get(Calendar.DAY_OF_MONTH);
        title.setText(year + "-" + month + "-" + day);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");

        ((TextView) findViewById(R.id.text_title)).setTypeface(TypefaceUtil.getAvenirTypeface(this));

        mRecyclerView = (BatRecyclerView) findViewById(R.id.bat_recycler_view);
        mAnimator = new BatItemAnimator();

        mRecyclerView.getView().setLayoutManager(new LinearLayoutManager(this));
        mGoals = new ArrayList<BatModel>();

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new BatCallback(this));
        itemTouchHelper.attachToRecyclerView(mRecyclerView.getView());
        mRecyclerView.getView().setItemAnimator(mAnimator);
        mRecyclerView.setAddItemListener(this);

        findViewById(R.id.root).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRecyclerView.revertAnimation();
            }
        });

        init();
        mRecyclerView.getView().setAdapter(mAdapter = new BatAdapter(mGoals, this, mAnimator).setOnItemClickListener(this).setOnOutsideClickListener(this));
        PermissionsManager.getPermissionsManager().signManager(this);
    }

    private void init() {
        for (int i = 0; i < Integer.MAX_VALUE; i++) {
            String text = ACache.get(this).getAsString(i + "");
            if (text != null) {
                Goal goal = new Goal(text);
                goal.hasImg = (boolean) ACache.get(this).getAsObject(HASIMGS + i);
                mGoals.add(goal);
            } else {
                return;
            }
        }
    }

    @Override
    public void add(String string) {
        Goal goal = new Goal(string);
        if (imgCache != null && imgCache.size() != 0) {
            goal.setImgs(imgCache);
        }
        addRefresh(string, goal.hasImg);//维护缓存表
        mGoals.add(0, goal);
        mAdapter.notify(AnimationType.ADD, 0);
        imgCache = null;
    }

    @Override
    public void delete(int position) {
        deleteRefresh(position);
        mGoals.remove(position);
        mAdapter.notify(AnimationType.REMOVE, position);
    }

    @Override
    public void move(int from, int to) {
        if (from > to) {
            moveRefresh(to, from);
        } else {
            moveRefresh(from, to);
        }

        if (from >= 0 && to >= 0) {
            mAnimator.setPosition(to);
            BatModel model = mGoals.get(from);
            mGoals.remove(model);
            mGoals.add(to, model);
            mAdapter.notify(AnimationType.MOVE, from, to);
            if (from == 0 || to == 0) {
                mRecyclerView.getView().scrollToPosition(Math.min(from, to));
            }
        }
    }

    @Override
    public void onClick(BatModel item, int position) {
        Intent intent = new Intent(this, DesActivity.class);
        intent.putExtra(NUMBER, position);
        startActivity(intent);
    }

    @Override
    public void onOutsideClicked() {
        mRecyclerView.revertAnimation();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (imgCache == null) {
            imgCache = new ArrayList<>();
        }
        if (requestCode == 1) {
            if (data.hasExtra("data")) {
                Bitmap thumbnail = data.getParcelableExtra("data");
                imgCache.add(thumbnail);
            }
        } else if (requestCode == 2) {
            if (data.getData() != null) {
                File f = new File(handleImageOnKitKat(data.getData().toString()));
                imgCache.add(BitmapFactory.decodeFile(f.getPath()));
            }
        }

    }

    private void addRefresh(String string, boolean hasImg) {
        for (int i = mGoals.size() - 1; i >= 0; i--) {
            String text = ACache.get(this).getAsString(i + "");
            ACache.get(this).put(i + 1 + "", text);
            boolean hImg = (boolean) ACache.get(this).getAsObject(HASIMGS + i);
            ACache.get(this).put(HASIMGS + (i + 1), hImg);
            if (hImg) {
                Bitmap img;
                for (int j = 0; j < 99; j++) {
                    img = ACache.get(this).getAsBitmap(i + "_" + j);
                    if (img != null) {
                        ACache.get(this).put(i + 1 + "_" + j, img);
                    } else {
                        j = 99;
                    }
                }
            }
        }
        ACache.get(this).put(0 + "", string);
        ACache.get(this).put(HASIMGS + 0, hasImg);
        if (hasImg) {
            for (int i = 0; i < imgCache.size(); i++) {
                ACache.get(this).put(0 + "_" + i, imgCache.get(i));
            }
        }


    }


    private void deleteRefresh(int index) {//删除位之后数据前移
        ACache.get(this).remove(index+"");
        ACache.get(this).remove(HASIMGS+index);
        Goal goal = (Goal) mGoals.get(index);
        if (goal.hasImg) {
            for (int i = 0; i < 99; i++) {
                if (!ACache.get(this).remove(index + "_" + i)) {
                    break;
                }
            }
        }
        for (int i = index + 1; i < mGoals.size(); i++) {
            String text = ACache.get(this).getAsString(i + "");
            ACache.get(this).put(i - 1 + "", text);
            boolean hImg = (boolean) ACache.get(this).getAsObject(HASIMGS + i);
            ACache.get(this).put(HASIMGS + (i - 1), hImg);
            if (hImg) {
                Bitmap img;
                for (int j = 0; j < 99; j++) {
                    img = ACache.get(this).getAsBitmap(i + "_" + j);
                    if (img != null) {
                        ACache.get(this).put(i - 1 + "_" + j, img);
                    } else {
                        j = 99;
                    }
                }
            }
        }
    }


    private void moveRefresh(int form, int to) {
        Goal goal = new Goal(ACache.get(this).getAsString(form + ""));
        goal.hasImg = (boolean) ACache.get(this).getAsObject(HASIMGS + form);
        if (goal.hasImg) {
            goal.imgs = new ArrayList<>();
            Bitmap img;
            for (int j = 0; j < 99; j++) {
                img = ACache.get(this).getAsBitmap(form + "_" + j);
                if (img != null) {
                    goal.imgs.add(img);
                } else {
                    j = 99;
                }
            }
        }
        if (goal.hasImg) {//删除form全部图片
            for (int i = 0; i < 99; i++) {
                if (!ACache.get(this).remove(form + "_" + i)) {
                    break;
                }
            }
        }//取出from位数据

        for (int i = form + 1; i < mGoals.size(); i++) {
            String text = ACache.get(this).getAsString(i + "");
            ACache.get(this).put(i - 1 + "", text);
            boolean hImg = (boolean) ACache.get(this).getAsObject(HASIMGS + i);
            ACache.get(this).put(HASIMGS + (i - 1), hImg);
            if (hImg) {
                Bitmap img;
                for (int j = 0; j < 99; j++) {
                    img = ACache.get(this).getAsBitmap(i + "_" + j);
                    if (img != null) {
                        ACache.get(this).put(i - 1 + "_" + j, img);
                    } else {
                        j = 99;
                    }
                }
            }
        }//from位之后数据整体前移

        for (int i = mGoals.size() - 1; i > to; i--) {
            String text = ACache.get(this).getAsString(i + "");
            ACache.get(this).put(i + 1 + "", text);
            boolean hImg = (boolean) ACache.get(this).getAsObject(HASIMGS + i);
            ACache.get(this).put(HASIMGS + (i + 1), hImg);
            if (hImg) {
                Bitmap img;
                for (int j = 0; j < 99; j++) {
                    img = ACache.get(this).getAsBitmap(i + "_" + j);
                    if (img != null) {
                        ACache.get(this).put(i + 1 + "_" + j, img);
                    } else {
                        j = 99;
                    }
                }
            }
        }//to位之后数据整体后移

        ACache.get(this).put(to + "", goal.getText());
        ACache.get(this).put(HASIMGS + to, goal.hasImg);
        if (goal.imgs != null) {
            for (int i = 0; i < goal.imgs.size(); i++) {
                ACache.get(this).put(to + "_" + i, goal.imgs.get(i));
            }//form数据插入to位
        }
    }

    //读取uri
    private String handleImageOnKitKat(String uriString) {
        String imagePath = null;
        Uri uri = Uri.parse(uriString);
        if (DocumentsContract.isDocumentUri(this, uri)) {
            String docId = DocumentsContract.getDocumentId(uri);
            if ("com.android.providers.media.documents".equals(uri.getAuthority())) {
                String id = docId.split(":")[1];
                String selection = MediaStore.Images.Media._ID + "=" + id;
                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);
            } else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"),
                        Long.valueOf(docId));
                imagePath = getImagePath(contentUri, null);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            imagePath = getImagePath(uri, null);
        }
        return imagePath;
    }

    private String getImagePath(Uri uri, String selection) {
        String path = null;
        Cursor cursor = getContentResolver().query(uri, null, selection, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }

            cursor.close();
        }
        return path;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }


    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {

    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {

    }
}
