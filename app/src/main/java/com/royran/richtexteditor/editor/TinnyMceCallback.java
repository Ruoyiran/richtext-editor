package com.royran.richtexteditor.editor;

import android.annotation.SuppressLint;
import android.util.Log;
import android.webkit.JavascriptInterface;

import androidx.annotation.NonNull;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

class TinnyMceCallback {

    private static final String TAG = "TinnyMceCallback:";

    private Gson mGson;

    private TinnyMceListener mListener;

    interface TinnyMceListener {
        void onFormatChanged(Map<EditorFormat, Object> format);

        void onCursorChanged(Map<EditorFormat, Object> enabledFormats);

        void onContentFetched(String content);

        void onLinkClicked(String url, String title);
    }

    TinnyMceCallback(TinnyMceListener listener) {
        mListener = listener;
        mGson = new Gson();
    }

    @SuppressLint("JavascriptInterface")
    @JavascriptInterface
    void onSelectionChanged(int start, int end) {
        if (mListener != null) {
        }
    }

    @JavascriptInterface
    public void onContentFetched(String content) {
        if (mListener != null) {
            mListener.onContentFetched(content);
        }
    }

    @JavascriptInterface
    public void onFormatChanged(String data) {
        Log.d(TAG, "onFormatChanged - " + data);
        if (mListener != null) {
            Map<EditorFormat, Object> format = parseFormat(data);
            mListener.onFormatChanged(format);
        }
    }

    @JavascriptInterface
    public void onLinkClicked(String url, String title) {
        if (mListener != null) {
            mListener.onLinkClicked(url, title);
        }
    }

    @JavascriptInterface
    public void onClickedImage(String url) {
        if (mListener != null) {
//            mListener.onClickedImage(url);
        }
    }

    @JavascriptInterface
    public void onCursorChanged(String data) {
        Log.d(TAG, "onCursorChanged - " + data);
        if (mListener != null) {
            Map<EditorFormat, Object> enabledFormats = parseFormat(data);
            mListener.onCursorChanged(enabledFormats);
        }
    }

    @NonNull
    private Map<EditorFormat, Object> parseFormat(String data) {
        Map<String, Object> formats;
        try {
            formats = mGson.fromJson(data, Map.class);
        } catch (Exception e) {
            Log.e(TAG, "json parse failed, error: " + e.getMessage());
            formats = new HashMap<>();
        }
        Map<EditorFormat, Object> enabledFormats = new HashMap<>();
        for (Map.Entry<String, Object> format : formats.entrySet()) {
            EditorFormat formatEnum = EditorFormat.getFormat(format.getKey());
            if (formatEnum != null) {
                enabledFormats.put(formatEnum, format.getValue());
            }
        }
        return enabledFormats;
    }
}
