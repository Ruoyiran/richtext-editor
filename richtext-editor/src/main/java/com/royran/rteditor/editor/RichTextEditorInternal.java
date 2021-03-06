package com.royran.rteditor.editor;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.ValueCallback;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.royran.rteditor.utils.HtmlUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static android.view.View.SCROLLBARS_OUTSIDE_OVERLAY;

public class RichTextEditorInternal implements TinnyMceCallback.TinnyMceListener {
    private final static String TAG = "RichTextEditor";
    private final static String JS_CALLBACK_HANDLER = "nativeCallbackHandler";

    private WebView mWebView;
    private List<ContentFetchListener> mContentFetchListeners;
    private EditorListener mEditorListener;
    private boolean mIsReady;

    public void focus() {
        if (mWebView != null) {
            mWebView.requestFocus();
        }
    }

    public interface ContentFetchListener {
        void onContentFetched(String content);
    }

    public RichTextEditorInternal(WebView webView) {
        mContentFetchListeners = new ArrayList<>();
        initWebView(webView);
    }

    @SuppressLint({"SetJavaScriptEnabled", "AddJavascriptInterface"})
    private void initWebView(WebView webView) {
        mWebView = webView;
        mWebView.setScrollBarStyle(SCROLLBARS_OUTSIDE_OVERLAY);
        WebSettings settings = mWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        mWebView.setWebViewClient(new EditorWebViewClient());
        mWebView.addJavascriptInterface(new TinnyMceCallback(this), JS_CALLBACK_HANDLER);
        mWebView.loadUrl("file:///android_asset/richtexteditor/editor.html?lang=" + Locale.getDefault().getLanguage());
    }

    public void setEditorListener(EditorListener listener) {
        mEditorListener = listener;
    }

    private void execJs(final String script) {
        execJs(script, null);
    }

    private void execJs(final String script, final ValueCallback<String> callback) {
        if (mIsReady) {
            mWebView.post(() -> mWebView.evaluateJavascript(script, callback));
        } else {
            mWebView.postDelayed(() -> mWebView.evaluateJavascript(script, callback), 1000);
        }
    }

    public void setEditorEnabled(boolean enabled) {
        if (enabled) {
            execJs("javascript:RE.enable();");
        } else {
            execJs("javascript:RE.disable();");
        }
    }

    public void undo() {
        execJs("javascript:RE.undo();");
    }

    public void redo() {
        execJs("javascript:RE.redo();");
    }

    public void setBold() {
        execJs("javascript:RE.setBold();");
    }

    public void setItalic() {
        execJs("javascript:RE.setItalic();");
    }

    public void setStrikethrough() {
        execJs("javascript:RE.setStrikethrough();");
    }

    public void setUnderline() {
        execJs("javascript:RE.setUnderline();");
    }

    public void setSubscript() {
        execJs("javascript:RE.setSubscript();");
    }

    public void setSuperscript() {
        execJs("javascript:RE.setSuperscript();");
    }

    public void setBlockquote() {
        execJs("javascript:RE.setBlockquote();");
    }

    public void setNumbers() {
        execJs("javascript:RE.setNumbers();");
    }

    public void setBullets() {
        execJs("javascript:RE.setBullets();");
    }

    public void setIndent() {
        execJs("javascript:RE.setIndent();");
    }

    public void setOutdent() {
        execJs("javascript:RE.setOutdent();");
    }

    public void setTextAlignLeft() {
        execJs("javascript:RE.setTextAlign(\"left\");");
    }

    public void setTextAlignCenter() {
        execJs("javascript:RE.setTextAlign(\"center\");");
    }

    public void setTextAlignRight() {
        execJs("javascript:RE.setTextAlign(\"right\");");
    }

    public void setFontSize(int fontSize) {
        execJs("javascript:RE.setFontSize('" + fontSize + "px');");
    }

    public void setTextColor(int color) {
        String hex = convertHexColorString(color);
        execJs("javascript:RE.setTextColor('" + hex + "');");
    }

    public void setTextBGColor(int color) {
        String hex = convertHexColorString(color);
        execJs("javascript:RE.setHiliteColor('" + hex + "');");
    }

    public void removeTextColor() {
        execJs("javascript:RE.removeTextColor();");
    }

    public void removeTextBGColor() {
        execJs("javascript:RE.removeHiliteColor();");
    }

    public void setContent(String content, OnCompleteListener listener) {
        content = HtmlUtils.escapeHtml(content);
        String script = String.format(Locale.US,
                "javascript:RE.setContent('%s');", content);
        execJs(script, value -> {
            if (listener != null) {
                listener.onCompleted();
            }
        });
    }

    public void getContent(ContentFetchListener listener) {
        synchronized (this) {
            if (listener != null) {
                mContentFetchListeners.add(listener);
            }
        }
        execJs("javascript:RE.getContent();");
    }

    public void getSelectedText(ContentFetchListener listener) {
        synchronized (this) {
            if (listener != null) {
                mContentFetchListeners.add(listener);
            }
        }
        execJs("javascript:RE.getSelectedText();");
    }

    public void insertLink(String url, String title) {
        if (TextUtils.isEmpty(url)) {
            return;
        }
        if (TextUtils.isEmpty(title)) {
            title = url;
        }
        execJs("javascript:RE.insertLink('" + url + "', '" + title + "');");
    }

    public void insertImage(String url, String title) {
        if (TextUtils.isEmpty(url)) {
            return;
        }
        if (TextUtils.isEmpty(title)) {
            title = "untitled";
        }
        execJs("javascript:RE.insertImage('" + url + "', '" + title + "');");
    }


    public void removeFormat() {
        execJs("javascript:RE.removeFormat();");
    }

    public void removeLink() {
        execJs("javascript:RE.removeLink();");
    }

    @Override
    public void onLinkClicked(String url, String title) {
        if (mEditorListener != null) {
            mEditorListener.onLinkClicked(url, title);
        }
    }

    @Override
    public void onImageClicked(String src, String alt) {
        if (mEditorListener != null) {
            mEditorListener.onImageClicked(src, alt);
        }
    }

    @Override
    public void onImageRemoved(String src) {
        if (mEditorListener != null) {
            mEditorListener.onImageRemoved(src);
        }
    }

    @Override
    public void onContentFetched(String content) {
        synchronized (this) {
            for (ContentFetchListener listener : mContentFetchListeners) {
                listener.onContentFetched(content);
            }
            mContentFetchListeners.clear();
        }
    }

    @Override
    public void onFormatChanged(Map<EditorFormat, Object> format) {
        if (mEditorListener != null) {
            mEditorListener.onFormatChanged(format);
        }
    }

    @Override
    public void onCursorChanged(Map<EditorFormat, Object> enabledFormats) {
        if (mEditorListener != null) {
            mEditorListener.onCursorChanged(enabledFormats);
        }
    }

    private String convertHexColorString(int color) {
        return String.format("#%06X", (0xFFFFFF & color));
    }

    public class EditorWebViewClient extends WebViewClient {

        private static final String TAG = "EditorClient:";

        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
            Uri uri = Uri.parse(url);
            Log.i(TAG, "shouldInterceptRequest, request=" + url + ", scheme=" + uri.getScheme() + ", authority=" + uri.getAuthority());
//        if (NoteFileService.isLocalImageUri(uri)) {
//            Log.i(TAG, "get image");
//            WebResourceResponse resourceResponse = new WebResourceResponse("image/png", "utf-8", NoteFileService.getImage(uri.getQueryParameter("id")));
//            return resourceResponse;
//        } else {
            return super.shouldInterceptRequest(view, url);
//        }
        }

        @Override
        public void onLoadResource(WebView view, String url) {
            super.onLoadResource(view, url);
            Log.i(TAG, "onLoadResource, url=" + url);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Log.i(TAG, "shouldOverrideUrlLoading, url=" + url);
            return super.shouldOverrideUrlLoading(view, url);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            mIsReady = true;
            Log.i(TAG, "onPageFinished - url=" + url);
            if (mEditorListener != null) {
                mEditorListener.onPageLoaded();
            }
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
            Log.i(TAG, "onReceivedError, code=" + errorCode + ", desc=" + description + ", url=" + failingUrl);
        }
    }
}
