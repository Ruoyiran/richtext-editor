JS_DEBUG_TAG = "EDITOR_JS";

tinyMCE.init({
    selector: 'div#content',
    remove_trailing_brs: false,
    element_format: 'html',
    allow_unsafe_link_target: true,
    plugins: 'autolink lists advlist',
    toolbar: false,
    menubar: false,
    inline: true,
});

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

RE.initialContent = "";

RE.content = document.getElementById('content');

RE.setInputEnabled = function (inputEnabled) {
    RE.content.contentEditable = String(inputEnabled);
}

RE.enable = function () {
    RE.initialContent = RE.content.innerHTML;
    RE.setInputEnabled(true);
    RE.focus();
}

RE.disable = function () {
    RE.setInputEnabled(false);
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

RE.setContent = function(content) {
    tinyMCE.activeEditor.setContent(content);
}

RE.getContent = function () {
    nativeCallbackHandler.onContentFetched(RE.content.innerHTML);
}

RE.getSelectedText = function () {
    var selection = window.getSelection();
    var selectedText = selection.toString();
    nativeCallbackHandler.onSelectedTextFetched(selectedText);
}

RE.insertLink = function (url, title) {
    if (!RE.isEditorFocusOn()) {
        RE.focus();
    }
    var sel = document.getSelection();
    if (sel.toString().length == 0) {
        RE.execCommand("insertHTML", true, "<a href='" + url + "'>" + title + "</a>")
    } else if (sel.rangeCount > 0) {
        tinyMCE.activeEditor.formatter.apply('link', {
            href: url,
            title: title
        });
    }
}

RE.insertImage = function (src, title) {
    if (!RE.isEditorFocusOn()) {
        RE.focus();
    }
    tinyMCE.activeEditor.insertContent('<p/>');
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
    return (document.activeElement === RE.content);
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
    var hasUndo = (RE.initialContent !== RE.content.innerHTML);
    if (!hasUndo) {
        return;
    }
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
        tinyMCE.activeEditor.undoManager.add();
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
    var node = tinyMCE.activeEditor.selection.getNode();
    var parents = tinyMCE.dom.DomQuery(node).parents();
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

function printObject(o) {
    var out = '';
    for (var p in o) {
        out += p + ': ' + o[p] + '\n';
    }
    consoleLog(out);
}

RE.onClickHandler = function (e) {
    var target = e.target;
    if (target.tagName === 'A') {
        var link = target.getAttribute('href');
        var title = target.innerHTML;
        e.preventDefault();
        nativeCallbackHandler.onLinkClicked(link, title);
    } else if (target.tagName === 'IMG') {
        var src = target.getAttribute('src');
        var alt = target.getAttribute('alt');
        if (RE.content.contentEditable === "false") { // if not in editable mode, remove the image selected state
            tinyMCE.dom.Event.cancel(e);
        }
        nativeCallbackHandler.onImageClicked(src, alt);
    }
}

RE.onKeyDownHandler = function(e) {
    if ((e.keyCode == 8 || e.keyCode == 46) && tinyMCE.activeEditor.selection) { // delete & backspace keys
        var selectedNode = tinyMCE.activeEditor.selection.getNode(); // get the selected node (element) in the editor
        if (selectedNode && selectedNode.nodeName === 'IMG') {
            var src = selectedNode.getAttribute('src');
            nativeCallbackHandler.onImageRemoved(src);
        }
    }
}

document.addEventListener("selectionchange", RE.selectionChangeHandler, false);
RE.content.addEventListener("keydown", RE.onKeyDownHandler, false);
RE.content.addEventListener('click', RE.onClickHandler, false);
