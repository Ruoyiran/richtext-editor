package com.royran.rteditor.widget.colorpicker;

import android.content.Context;
import android.view.Gravity;
import android.widget.LinearLayout;

import androidx.appcompat.widget.AppCompatImageView;

import com.royran.rteditor.R;


/**
 * Created by wliu on 2018/3/19.
 */

public class ColorCheckedViewCheckmark extends AppCompatImageView {

    private int mSize;

    public ColorCheckedViewCheckmark(Context context, int size) {
        super(context);
        this.mSize = size;
        initView();
    }

    private void initView() {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(mSize, mSize);
        layoutParams.gravity = Gravity.CENTER;
        this.setLayoutParams(layoutParams);
        this.setImageResource(R.drawable.check_mark);
    }
}
