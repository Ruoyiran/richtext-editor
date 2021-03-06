package com.royran.rteditor.editor;

import java.util.Map;

public interface EditorListener {
    void onPageLoaded();

    void onFormatChanged(Map<EditorFormat, Object> format);

    void onCursorChanged(Map<EditorFormat, Object> enabledFormats);

    void onLinkClicked(String url, String title);

    void onImageClicked(String src, String alt);

    void onImageRemoved(String src);
}

