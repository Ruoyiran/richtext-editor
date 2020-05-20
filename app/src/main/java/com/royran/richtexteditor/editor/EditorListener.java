package com.royran.richtexteditor.editor;

import java.util.Map;

public interface EditorListener {
    void onPageLoaded();

    void onFormatChanged(Map<EditorFormat, Object> format);

    void onCursorChanged(Map<EditorFormat, Object> enabledFormats);

    void onLinkClicked(String url, String title);
}

