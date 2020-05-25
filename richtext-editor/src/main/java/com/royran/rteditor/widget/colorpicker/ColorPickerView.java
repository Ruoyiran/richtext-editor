package com.royran.rteditor.widget.colorpicker;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

import com.royran.rteditor.R;

/**
 * Created by wliu on 2018/3/6.
 */

public class ColorPickerView extends HorizontalScrollView {
    interface ColorPickerListener {
        void onPickColor(int index, int color);

        void onColorRemove();
    }

    private Context mContext;

    private LinearLayout mColorsContainer;

    private ColorPickerListener mColorPickerListener;

    private Bundle mAttributeBundle = new Bundle();

    private int[] mColors;

    public ColorPickerView(Context context) {
        this(context, null);
    }

    public ColorPickerView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public ColorPickerView(Context context, AttributeSet attributeSet, int defStyleAttr) {
        super(context, attributeSet, defStyleAttr);
        this.mContext = context;
        TypedArray ta = context.obtainStyledAttributes(attributeSet, R.styleable.ColorPickerView);
        int colorViewWidth = ta.getDimensionPixelSize(R.styleable.ColorPickerView_colorViewWidth, 40);
        int colorViewHeight = ta.getDimensionPixelSize(R.styleable.ColorPickerView_colorViewHeight, 40);
        int colorViewMarginLeft = ta.getDimensionPixelSize(R.styleable.ColorPickerView_colorViewMarginLeft, 5);
        int colorViewMarginRight = ta.getDimensionPixelSize(R.styleable.ColorPickerView_colorViewMarginRight, 5);
        int colorCheckedViewType = ta.getInt(R.styleable.ColorPickerView_colorViewCheckedType, 0);
        int colorsId = ta.getResourceId(R.styleable.ColorPickerView_colors, R.array.colors);
        mColors = ta.getResources().getIntArray(colorsId);
        ta.recycle();

        mAttributeBundle.putInt(ColorView.ATTR_VIEW_WIDTH, colorViewWidth);
        mAttributeBundle.putInt(ColorView.ATTR_VIEW_HEIGHT, colorViewHeight);
        mAttributeBundle.putInt(ColorView.ATTR_MARGIN_LEFT, colorViewMarginLeft);
        mAttributeBundle.putInt(ColorView.ATTR_MARGIN_RIGHT, colorViewMarginRight);
        mAttributeBundle.putInt(ColorView.ATTR_CHECKED_TYPE, colorCheckedViewType);

        initView();
    }

    private void initView() {
        mColorsContainer = new LinearLayout(mContext);
        LinearLayout.LayoutParams containerLayoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        mColorsContainer.setLayoutParams(containerLayoutParams);
        final ColorView colorRemove = new ColorView(mContext, Color.TRANSPARENT, mAttributeBundle);
        colorRemove.setCheckMarkerDrawable(R.drawable.ic_color_remove);
        colorRemove.setChecked(true);
        colorRemove.setOnClickListener(v -> {
            if (mColorPickerListener != null) {
                mColorPickerListener.onColorRemove();
            }
        });
        mColorsContainer.addView(colorRemove);

        for (int colorIndex = 0; colorIndex < mColors.length; ++colorIndex) {
            final int index = colorIndex;
            final int color = mColors[colorIndex];
            final ColorView colorView = new ColorView(mContext, color, mAttributeBundle);
            mColorsContainer.addView(colorView);

            colorView.setOnClickListener(v -> {
                boolean isCheckedNow = colorView.getChecked();
                if (isCheckedNow) {
                    if (mColorPickerListener != null) {
                        mColorPickerListener.onPickColor(index, colorView.getColor());
                    }
                    return;
                }

                setCheckedColor(index);
                if (mColorPickerListener != null) {
                    mColorPickerListener.onPickColor(index, colorView.getColor());
                }
            });
        }

        this.addView(mColorsContainer);
    }

    public void setCheckedColor(int index) {
        index += 1;
        if (index < 0 || index >= mColorsContainer.getChildCount()) {
            return;
        }
        int childCount = mColorsContainer.getChildCount();
        for (int i = 1; i < childCount; i++) {
            View childView = mColorsContainer.getChildAt(i);
            if (childView instanceof ColorView) {
                ColorView colorView = (ColorView) mColorsContainer.getChildAt(i);
                boolean enabled = index == i;
                if (enabled != colorView.getChecked()) {
                    colorView.setChecked(enabled);
                }
            }
        }
    }

    public void setColorPickerListener(ColorPickerListener listener) {
        this.mColorPickerListener = listener;
    }

    public boolean isVisible() {
        return getVisibility() == VISIBLE;
    }

    public void show() {
        setVisibility(VISIBLE);
    }

    public void hide() {
        setVisibility(GONE);
    }
}
