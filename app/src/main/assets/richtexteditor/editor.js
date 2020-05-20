/**
 * Copyright (C) 2017 Wasabeef
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

JS_DEBUG_TAG = "EDITOR_JS";

tinymce.init({
    selector: 'div#content',
    remove_trailing_brs: false,
    element_format: 'html',
    allow_unsafe_link_target: true,
    plugins: 'autolink image lists advlist',
    toolbar: false,
    menubar: false,
    inline: true,
});

// lang
var ms = location.href.match(/lang=(.+)/);
var lang = 'en';
if (ms) {
    lang = ms[1].toLowerCase();
}
if (lang.indexOf('zh') >= 0) {
    $('#title').attr('placeholder', '请输入标题')
} else {
    $('#title').attr('placeholder', 'Input title here')
}

// reference: https://www.tiny.cloud/docs/general-configuration-guide/filter-content/#built-informats
var Format = {}
Format.UNDO = 'undo';
Format.REDO = 'redo';
Format.BOLD = 'bold';
Format.ITALIC = 'italic';
Format.UNDERLINE = 'underline';
Format.LINK = 'link';
Format.STRIKETHROUGH = 'strikeThrough';
Format.FONT_SIZE = 'fontsize';
Format.FORE_COLOR = 'forecolor';
Format.HILITE_COLOR = 'hilitecolor';
Format.SUBSCRIPT = 'subscript';
Format.SUPERSCRIPT = 'superscript';
Format.BLOCKQUOTE = 'blockquote';
Format.INDENT = 'indent';
Format.OUTDENT = 'outdent';
Format.NUMBERS = 'insertOrderedList';
Format.BULLETS = 'insertUnorderedList';
Format.ALIGN_LEFT = 'justifyLeft';
Format.ALIGN_CENTER = 'justifyCenter';
Format.ALIGN_RIGHT = 'justifyRight';
Format.REMOVE_FORMAT = 'removeFormat';

Format.AllBooleanFormats = [
    Format.BOLD,
    Format.ITALIC,
    Format.UNDERLINE,
    Format.STRIKETHROUGH,
    Format.SUBSCRIPT,
    Format.SUPERSCRIPT,
    Format.BLOCKQUOTE,
    Format.NUMBERS,
    Format.BULLETS,
    Format.ALIGN_LEFT,
    Format.ALIGN_CENTER,
    Format.ALIGN_RIGHT,
];

consoleLog = function (msg) {
    if (msg) {
        console.log(JS_DEBUG_TAG + " " + msg);
    }
}

var RE = {};

RE.currentSelection = {
    "startContainer": 0,
    "startOffset": 0,
    "endContainer": 0,
    "endOffset": 0
};

RE.editor = document.getElementById('content');

RE.setInputEnabled = function (inputEnabled) {
    RE.editor.contentEditable = String(inputEnabled);
}

RE.enable = function () {
    RE.setInputEnabled(true);
    RE.focus();
}

RE.disable = function () {
    RE.setInputEnabled(false);
    document.removeEventListener("selectionchange", RE.selectionChangeHandler, false);
}

RE.checkEnabled = function (format) {
    var enabled = false;
    if (format === Format.BLOCKQUOTE) {
        enabled = tinyMCE.activeEditor.formatter.match(format);
    } else {
        enabled = document.queryCommandState(format);
    }
    return enabled;
}

RE.getHTML = function () {
    nativeCallbackHandler.onContentFetched(RE.editor.innerHTML);
}

RE.getSelectedText = function () {
    var selection = window.getSelection();
    var selectedText = selection.toString();
    nativeCallbackHandler.onContentFetched(selectedText);
}

RE.insertLink = function (url, title) {
    if (!RE.isEditorFocusOn()) {
        RE.focus();
    }
    var sel = document.getSelection();
    if (sel.toString().length == 0) {
        document.execCommand("insertHTML", false, "<a href='" + url + "'>" + title + "</a>");
    } else if (sel.rangeCount) {
        var el = document.createElement("a");
        el.setAttribute("href", url);
        el.setAttribute("title", title);
        var range = sel.getRangeAt(0).cloneRange();
        range.surroundContents(el);
        sel.removeAllRanges();
        sel.addRange(range);
    }
}

RE.insertImage = function (src, title) {
    if (!RE.isEditorFocusOn()) {
        RE.focus();
    }
    tinyMCE.activeEditor.insertContent('<img src="' + src + '" alt="' + title + '"/>');
    tinyMCE.activeEditor.insertContent('<p/>');
}

RE.removeLink = function () {
    tinyMCE.activeEditor.formatter.remove(Format.LINK);
}

RE.focus = function () {
    tinyMCE.activeEditor.focus();
    tinyMCE.activeEditor.selection.collapse();
}

RE.isEditorFocusOn = function () {
    return (document.activeElement === RE.editor);
}

RE.removeTextColor = function () {
    tinyMCE.activeEditor.formatter.remove(Format.FORE_COLOR);
    RE.focus();
}

RE.removeHiliteColor = function () {
    tinyMCE.activeEditor.formatter.remove(Format.HILITE_COLOR);
    RE.focus();
}

RE.undo = function () {
    tinyMCE.activeEditor.undoManager.undo();
}

RE.redo = function () {
    tinyMCE.activeEditor.undoManager.redo();
}

RE.execCommand = function (format, useDoc, args) {
    if (RE.isEditorFocusOn() === false) {
        RE.focus();
    }
    var ok = false;
    if (useDoc) {
        ok = document.execCommand(format, false, args);
    } else {
        ok = tinyMCE.activeEditor.execCommand(format, false, args);
    }
    if (ok) {
        nativeCallbackHandler.onFormatChanged(JSON.stringify({[format]: RE.checkEnabled(format)}));
    } else {
        consoleLog("execCommand failed, format: " + format);
    }
}

RE.setBold = function () {
    RE.execCommand(Format.BOLD, true);
}

RE.setItalic = function () {
    RE.execCommand(Format.ITALIC, true);
}

RE.setUnderline = function () {
    RE.execCommand(Format.UNDERLINE, true);
}

RE.setStrikethrough = function () {
    RE.execCommand(Format.STRIKETHROUGH, true);
}

RE.setSubscript = function () {
    if (RE.checkEnabled(Format.SUPERSCRIPT)) {
        RE.execCommand(Format.SUPERSCRIPT);
    }
    RE.execCommand(Format.SUBSCRIPT);
}

RE.setSuperscript = function () {
    if (RE.checkEnabled(Format.SUBSCRIPT)) {
        RE.execCommand(Format.SUBSCRIPT);
    }
    RE.execCommand(Format.SUPERSCRIPT);
}

RE.setTextAlign = function (align) {
    var alignFormats = {};
    alignFormats[Format.ALIGN_LEFT] = false;
    alignFormats[Format.ALIGN_CENTER] = false;
    alignFormats[Format.ALIGN_RIGHT] = false;
    var format = Format.ALIGN_LEFT;
    if (align == "center") {
        format = Format.ALIGN_CENTER;
    } else if (align == "right") {
        format = Format.ALIGN_RIGHT;
    }
    if (!RE.checkEnabled(format)) {
        RE.execCommand(format);
    }
    alignFormats[format] = true;
    nativeCallbackHandler.onFormatChanged(JSON.stringify(alignFormats));
}

RE.isChildOfBody = function (elm) {
    return tinyMCE.activeEditor.$.contains(tinyMCE.activeEditor.getBody(), elm);
}

RE.isNodeList = function (node) {
    return node && (/^(OL|UL|DL)$/).test(node.nodeName) && RE.isChildOfBody(node);
}

RE.getListState = function () {
    var node = tinyMCE.activeEditor.selection.getNode()
    var parents = tinymce.dom.DomQuery(node).parents();
    var lists = tinyMCE.util.Tools.grep(parents, RE.isNodeList);
    if (lists.length > 0) {
        return lists[0].nodeName.toLowerCase();
    } else {
        return '';
    }
}

RE.setNumbers = function () {
    var ok = tinyMCE.activeEditor.execCommand(Format.NUMBERS, false);
    if (ok) {
        var listState = RE.getListState();
        var enabled = listState.toLowerCase() === 'ol';
        var format = Format.NUMBERS;
        nativeCallbackHandler.onFormatChanged(JSON.stringify({format: enabled}));
    }
}

RE.setBullets = function () {
    var ok = tinyMCE.activeEditor.execCommand(Format.BULLETS, false);
    if (ok) {
        var listState = RE.getListState();
        var enabled = listState.toLowerCase() === 'ul';
        var format = Format.BULLETS;
        nativeCallbackHandler.onFormatChanged(JSON.stringify({format: enabled}));
    }
}

RE.setBlockquote = function () {
    var format = Format.BLOCKQUOTE;
    tinyMCE.activeEditor.formatter.toggle(format);
    var enabled = tinyMCE.activeEditor.formatter.match(format);
    nativeCallbackHandler.onFormatChanged(JSON.stringify({[format]: enabled}));
}

RE.setFontSize = function (fontSize) {
    RE.execCommand(Format.FONT_SIZE, false, fontSize);
}

RE.setTextColor = function (color) {
    RE.execCommand(Format.FORE_COLOR, false, color);
    RE.focus();
}

RE.setHiliteColor = function (color) {
    RE.execCommand(Format.HILITE_COLOR, false, color);
    RE.focus();
}

RE.setIndent = function () {
    tinyMCE.activeEditor.execCommand(Format.INDENT, false);
}

RE.setOutdent = function () {
    tinyMCE.activeEditor.execCommand(Format.OUTDENT, false);
}

RE.removeFormat = function () {
    tinyMCE.activeEditor.execCommand(Format.REMOVE_FORMAT, false);
}

RE.getAllEnabledFormats = function () {
    var enabledFormats = {};
    for (let i = 0; i < Format.AllBooleanFormats.length; ++i) {
        var format = Format.AllBooleanFormats[i];
        if (RE.checkEnabled(format)) {
            enabledFormats[format] = true;
        }
    }
    return enabledFormats;
}

RE.selectionChangeHandler = function () {
    if (!RE.isEditorFocusOn()) {
        return;
    }
    var enabledFormats = RE.getAllEnabledFormats();
    nativeCallbackHandler.onCursorChanged(JSON.stringify(enabledFormats));

}

RE.clickHandler = function (e) {
    var target = e.target;
    if (target.tagName === 'A') {
        var link = target.getAttribute('href');
        var title = target.innerHTML;
        e.preventDefault();
        nativeCallbackHandler.onLinkClicked(link, title);
    } else if (target.tagName === 'IMG') {
        var src = target.getAttribute('src');
        var alt = target.getAttribute('alt');
        e.preventDefault();
        nativeCallbackHandler.onImageClicked(src, alt);
    }
}

document.addEventListener("selectionchange", RE.selectionChangeHandler, false);
RE.editor.addEventListener('click', RE.clickHandler, false);
