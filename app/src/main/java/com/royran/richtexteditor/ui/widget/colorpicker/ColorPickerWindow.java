package com.royran.richtexteditor.ui.widget.colorpicker;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.PopupWindow;

import com.royran.richtexteditor.R;
import com.royran.richtexteditor.utils.Helper;

public class ColorPickerWindow extends PopupWindow implements PopupWindow.OnDismissListener, ColorPickerView.ColorPickerListener {
    Context mContext;
    ColorPickerView mColorPickerView;
    ColorPickerListener mListener;

    public static void show(Context context, View view, int colorIndex, ColorPickerListener listener) {
        ColorPickerWindow colorPickerWindow = new ColorPickerWindow(context, colorIndex, listener);
        colorPickerWindow.showAsDropDown(view, 0, 0);
    }

    public ColorPickerWindow(Context context, int colorIndex, ColorPickerListener listener) {
        mContext = context;
        mListener = listener;
        View view = inflateContentView();
        setContentView(view);
        int[] wh = Helper.getScreenWidthAndHeight(context);
        this.setWidth(wh[0]);
        int h = Helper.getPixelByDp(context, 50);
        this.setHeight(h);
        this.setOutsideTouchable(true);
        mColorPickerView = view.findViewById(R.id.color_picker);
        mColorPickerView.setCheckedColor(colorIndex);
        mColorPickerView.setColorPickerListener(this);
        setOnDismissListener(this);
    }


    private View inflateContentView() {
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        View view = layoutInflater.inflate(R.layout.color_picker, null);
        return view;
    }

    @Override
    public void onDismiss() {
        if (mListener != null) {
            mListener.onDismiss();
        }
    }

    @Override
    public void onPickColor(int index, int color) {
        if (mListener != null) {
            mListener.onPickColor(index, color);
        }
    }

    @Override
    public void onColorRemove() {
        if (mListener != null) {
            mListener.onColorRemove();
        }
        dismiss();
    }
}
