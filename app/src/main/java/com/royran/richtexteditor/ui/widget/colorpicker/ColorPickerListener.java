package com.royran.richtexteditor.ui.widget.colorpicker;

/**
 * Created by wliu on 2018/3/6.
 */

public interface ColorPickerListener {
    void onPickColor(int index, int color);

    void onDismiss();

    void onColorRemove();
}
