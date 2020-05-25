package com.royran.rteditor.widget.fontsizepicker;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.TextView;

import com.royran.rteditor.R;
import com.royran.rteditor.utils.Helper;

public class FontSizePickerWindow extends PopupWindow implements PopupWindow.OnDismissListener {
    private static final int MIN_FONT_SIZE = 12;
    private static final int MAX_FONT_SIZE = 36;

    private Context mContext;

    private View mRootView;

    private TextView mPreview;

    private SeekBar mSeekbar;

    private int mFontSize;
    private FontSizeChangedListener mListener;

    public static void show(Context context, View view, int fontSize, FontSizeChangedListener listener) {
        FontSizePickerWindow window = new FontSizePickerWindow(context, fontSize, listener);
        window.showAsDropDown(view, 0, 0);
    }

    public FontSizePickerWindow(Context context, int fontSize, FontSizeChangedListener listener) {
        mContext = context;
        this.mFontSize = fontSize;
        this.mRootView = inflateContentView();
        this.setFontSizeChangedListener(listener);
        this.setContentView(mRootView);
        int[] wh = Helper.getScreenWidthAndHeight(context);
        this.setWidth(wh[0]);
        int h = Helper.getPixelByDp(context, 100);
        this.setHeight(h);
        this.setBackgroundDrawable(new BitmapDrawable());
        this.setOutsideTouchable(true);
        this.initView();
        this.setupListeners();
    }

    private void setFontSizeChangedListener(FontSizeChangedListener listener) {
        mListener = listener;
    }

    private View inflateContentView() {
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        View view = layoutInflater.inflate(R.layout.fontsize_picker, null);
        return view;
    }

    private <T extends View> T findViewById(int id) {
        return mRootView.findViewById(id);
    }

    private void initView() {
        this.mPreview = findViewById(R.id.fontsize_preview);
        this.mSeekbar = findViewById(R.id.fontsize_seekbar);
        this.mSeekbar.setMax(MAX_FONT_SIZE - MIN_FONT_SIZE);
        this.mSeekbar.setProgress(mFontSize - MIN_FONT_SIZE);
        setPreviewText(mFontSize);
    }

    private void setupListeners() {
        this.mSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mFontSize = progress + MIN_FONT_SIZE;
                setPreviewText(mFontSize);
                changePreviewText(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        setOnDismissListener(this);
    }

    private void changePreviewText(int progress) {
        int size = MIN_FONT_SIZE + progress;
        if (mListener != null) {
            mListener.onFontSizeChanged(size);
        }
    }

    private void setPreviewText(int size) {
        mPreview.setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
        mPreview.setText(size + "px: Preview"); // TODO: read from resource
    }

    @Override
    public void onDismiss() {
        if (mListener != null) {
            mListener.onDismiss();
        }
    }
}
