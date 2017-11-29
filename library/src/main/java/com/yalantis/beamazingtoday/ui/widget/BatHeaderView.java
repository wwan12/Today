package com.yalantis.beamazingtoday.ui.widget;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.ColorInt;
import android.support.annotation.DimenRes;
import android.support.annotation.DrawableRes;
import android.support.v4.content.FileProvider;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.AppCompatCheckBox;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.jakewharton.rxbinding.widget.RxTextView;
import com.yalantis.beamazingtoday.Constant;
import com.yalantis.beamazingtoday.R;
import com.yalantis.beamazingtoday.listeners.AnimationListener;
import com.yalantis.beamazingtoday.listeners.BatListener;
import com.yalantis.beamazingtoday.ui.callback.EditListener;
import com.yalantis.beamazingtoday.util.AnimationUtil;
import com.yalantis.beamazingtoday.util.TypefaceUtil;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Calendar;

import rx.functions.Action1;

/**
 * Created by galata on 15.07.16.
 */
public class BatHeaderView extends FrameLayout implements EditListener {

    AddView mAddView;
    BatEditText mEditText;
    Button mButtonAdd;
    Button mButtonAddImg;
    Button mButtontack;
//    LinearLayout llAdd;
    View mRoot;
    AppCompatCheckBox mRadioButton;
    View mDivider;
    private String path;

    private Context context;
     String tackUri;

    private AnimationListener mAnimationListener;
    private BatListener mAddItemListener;

    public BatHeaderView(Context context) {
        this(context, null);
    }

    public BatHeaderView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BatHeaderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        LayoutInflater.from(context).inflate(R.layout.bat_header_view, this, true);
        mAddView = (AddView) findViewById(R.id.add_view);
        mEditText = (BatEditText) findViewById(R.id.bat_edit_text);
        mButtonAdd = (Button) findViewById(R.id.button_add);
//        llAdd = (LinearLayout) findViewById(R.id.ll_addimg);
        mButtonAddImg = (Button) findViewById(R.id.button_addimg);
        mButtontack = (Button) findViewById(R.id.button_tackimg);
        mRoot = findViewById(R.id.root);
        mRadioButton = (AppCompatCheckBox) findViewById(R.id.radio_button);
        mDivider = findViewById(R.id.divider);
        mRoot.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onClick2();
            }
        });
        mButtonAdd.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                addItem();
            }
        });
        mButtonAddImg.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                addImg();
            }
        });
        mButtontack.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                tackImg();
            }
        });
        mAddView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onClick2();
            }
        });
        mEditText.setEditListener(this);
        if (path == null) {
            path = context.getFilesDir().getPath();
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        RxTextView.textChanges(mEditText.getView()).subscribe(new Action1<CharSequence>() {
            @Override
            public void call(CharSequence charSequence) {
                mButtonAdd.setEnabled(!TextUtils.isEmpty(charSequence));
            }
        });
        mButtonAdd.setTypeface(TypefaceUtil.getTypeface(getContext()));
    }

    void onClick2() {
        if (mEditText.isEnabled()) {
            AnimationUtil.showKeyboard(getContext(), mEditText.getView());
        } else {
            animateIncreasing();
        }
    }

    void animateIncreasing() {
        mEditText.clear();
        mRoot.setBackgroundResource(R.drawable.header_background_rounded);
        ViewCompat.animate(this).scaleX(1.1f).translationY(getDimen(R.dimen.header_translation))
                .setDuration(Constant.ANIM_DURATION_MILLIS).start();
        AnimationUtil.hide(mDivider);
        AnimationUtil.scaleXViews(0.9f, mButtonAdd, mEditText);
        AnimationUtil.moveX(mEditText, -mAddView.getWidth() - getDimen(R.dimen.cursor_width) / 2, new Runnable() {
            @Override
            public void run() {
                AnimationUtil.showViews(mButtonAdd);
                AnimationUtil.showViews(mButtonAddImg);
                AnimationUtil.showViews(mButtontack);
                mEditText.focus();
            }
        });
        mAddView.rotate(new Runnable() {
            @Override
            public void run() {
                mEditText.showCursor();
                mAddView.hide();
                mEditText.setEnabled(true);
                AnimationUtil.showKeyboard(getContext(), mEditText.getView());
            }
        });

        if (mAnimationListener != null) {
            mAnimationListener.onIncreaseAnimationStarted();
        }
    }

    void animateDecreasing() {
        mEditText.setEnabled(false);
        mEditText.clearFocus();
        mEditText.clear();
        mAddView.show();
        AnimationUtil.moveX(mEditText, 0);
        AnimationUtil.hideViews(mButtonAdd);
        AnimationUtil.hideViews(mButtonAddImg);
        AnimationUtil.hideViews(mButtontack);
        mEditText.hideCursor();
        ViewCompat.animate(this).scaleX(1f).translationY(0).setDuration(Constant.ANIM_DURATION_MILLIS).start();
        AnimationUtil.scaleXViews(1, mButtonAdd, mEditText);

        if (mAnimationListener != null) {
            mAnimationListener.onDecreaseAnimationStarted();
        }
        mAddView.rotateBack(new Runnable() {
            @Override
            public void run() {
                mRoot.setBackgroundResource(R.drawable.header_background);
                mRoot.requestFocus();
            }
        });
    }

    void animateAppearance() {
        mButtonAdd.setVisibility(INVISIBLE);
        mButtonAddImg.setVisibility(INVISIBLE);
        mButtontack.setVisibility(INVISIBLE);
        mRoot.setBackgroundResource(R.drawable.header_background);
        mEditText.setVisibility(GONE);
        mEditText.clear();
        mEditText.clearFocus();
        mEditText.setEnabled(false);
        mEditText.hideCursor();
        mEditText.setTranslationX(0);
        setScaleY(0);
        setPivotY(0);
        setPivotY(getY() + getHeight());
        setTranslationY(getDimen(R.dimen.header_translation));
        setAlpha(1);

        ViewCompat.animate(this).scaleY(1.1f).setInterpolator(new OvershootInterpolator(2)).setDuration(Constant.ANIM_DURATION_MILLIS)
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        setTranslationY(0);
                        AnimationUtil.show(mDivider);
                        setScaleY(1);
                        mAddView.increase();
                        AnimationUtil.show(mEditText);
                    }
                }).start();
    }

    void addItem() {
        if (mAddItemListener != null) {
            mAddItemListener.add(mEditText.getText());
        }

        AnimationUtil.scaleXViews(1, mButtonAdd, mButtonAddImg, mEditText);
        bringToFront();

        AnimationUtil.hide(mButtonAdd);
        AnimationUtil.hide(mButtonAddImg);
        AnimationUtil.hide(mButtontack);
        mEditText.hideCursor();
        setTranslationY(0);
        if (mAnimationListener != null) {
            mAnimationListener.onAddAnimationStarted();
        }
        ViewCompat.animate(mEditText).translationX(-getDimen(R.dimen.edit_text_vertical_offset))
                .setDuration(Constant.ANIM_DURATION_MILLIS).start();
        ViewCompat.animate(this).scaleX(1).translationY(getHeight() - getDimen(R.dimen.edit_text_offset))
                .setInterpolator(new LinearInterpolator())
                .setDuration(Constant.ANIM_DURATION_MILLIS).withEndAction(new Runnable() {
            @Override
            public void run() {
                Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.increasing_anim);
                animation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                        AnimationUtil.scaleXViews(1, mButtonAdd, mEditText);
                        mRadioButton.setVisibility(VISIBLE);
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        setAlpha(0);
                        setTranslationY(0);
                        mRadioButton.setVisibility(INVISIBLE);
                        animateAppearance();
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                mRadioButton.startAnimation(animation);
            }
        }).start();
    }


    private void addImg() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        ((Activity) context).startActivityForResult(intent, 2);
    }

    private void tackImg() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH) + 1;
        int day = c.get(Calendar.DAY_OF_MONTH);
        String imgName = year + "_" + month + "_" + day + "(" + System.currentTimeMillis() + ").jpg";
        File f = new File(path+File.separatorChar+"img"+File.separatorChar, imgName);
        Uri contentUri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            contentUri = FileProvider.getUriForFile(context, "com.yalantis.beamazingtoday.fileProvider", f);
//            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
            contentUri=Uri.fromFile(f);
        }
//        intent.putExtra(MediaStore.EXTRA_OUTPUT, contentUri);
//        intent.putExtra("return-data", true);
        ((Activity) context).startActivityForResult(intent, 1);
//        tackUri=contentUri.toString();

    }

    private int getDimen(@DimenRes int res) {
        return getContext().getResources().getDimensionPixelOffset(res);
    }

    void setAnimationListener(AnimationListener animationListener) {
        mAnimationListener = animationListener;
    }

    void setAddItemListener(BatListener addItemListener) {
        mAddItemListener = addItemListener;
    }

    void setDividerVisibility(boolean visible) {
        mDivider.setVisibility(visible ? VISIBLE : GONE);
    }

    void setDividerColor(@ColorInt int color) {
        mDivider.setBackgroundColor(color);
    }

    void setPlusColor(@ColorInt int color) {
        mAddView.setColor(color);
    }

    void setRadioButtonColor(@ColorInt int color) {
        mRadioButton.setSupportButtonTintList(ColorStateList.valueOf(color));
    }

    void setRadioButtonSelector(@DrawableRes int drawable) {
        mRadioButton.setBackgroundResource(drawable);
    }

    void setAddButtonColor(ColorStateList list) {
        mButtonAdd.setTextColor(list);
    }

    void setPath(String path) {
        this.path = path;
    }

    @Override
    public void onStartEdit() {
        animateIncreasing();
    }

    private String getErrorInfo(Throwable arg1) {
        Writer writer = new StringWriter();
        PrintWriter pw = new PrintWriter(writer);
        arg1.printStackTrace(pw);
        pw.close();
        String error = writer.toString();
        return error;
    }

}
