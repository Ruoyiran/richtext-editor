package com.royran.rteditor.editor;

import android.annotation.SuppressLint;
import android.util.Log;
import android.webkit.JavascriptInterface;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.royran.rteditor.utils.HtmlUtils;

import java.util.HashMap;
import java.util.Map;

class TinnyMceCallback {
    private final static String EMPTY_HTML_TAG = "<p><br data-mce-bogus=\"1\"></p>";

    private static final String TAG = "TinnyMceCallback:";

    private Gson mGson;

    private TinnyMceListener mListener;

    interface TinnyMceListener {
        void onFormatChanged(Map<EditorFormat, Object> format);

        void onCursorChanged(Map<EditorFormat, Object> enabledFormats);

        void onLinkClicked(String url, String title);

        void onImageClicked(String src, String alt);

        void onImageRemoved(String src);

        void onContentFetched(String content);
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
    public void onSelectedTextFetched(String selectedText) {
        if (mListener != null) {
            selectedText = HtmlUtils.unescapeHtml(selectedText);
            if (EMPTY_HTML_TAG.equals(selectedText)) {
                selectedText = "";
            }
            mListener.onContentFetched(selectedText);
        }
    }

    @JavascriptInterface
    public void onContentFetched(String content) {
        if (mListener != null) {
            content = HtmlUtils.unescapeHtml(content);
            if (EMPTY_HTML_TAG.equals(content)) {
                content = "";
            }
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
    public void onImageClicked(String src, String alt) {
        if (mListener != null) {
            mListener.onImageClicked(src, alt);
        }
    }

    @JavascriptInterface
    public void onImageRemoved(String src) {
        if (mListener != null) {
            mListener.onImageRemoved(src);
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
