package com.royran.rteditor.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.webkit.ValueCallback;
import android.webkit.WebView;


public class EditorWebView extends WebView {
    private final static String TAG = "EditorWebView";

    public EditorWebView(Context context) {
        super(context);
    }

    public EditorWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void evaluateJavascript(String script, ValueCallback<String> resultCallback) {
        Log.i(TAG, "execute=" + script);
        super.evaluateJavascript(script, resultCallback);
    }
}
