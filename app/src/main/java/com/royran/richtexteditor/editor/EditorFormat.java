package com.royran.richtexteditor.editor;

public enum EditorFormat {
    UNDO,
    REDO,
    BOLD,
    ITALIC,
    UNDERLINE,
    STRIKETHROUGH,
    SUBSCRIPT,
    SUPERSCRIPT,
    INDENT,
    OUTDENT,
    ALIGN_LEFT,
    ALIGN_CENTER,
    ALIGN_RIGHT,
    BULLET_LIST,
    ORDERED_LIST,
    BLOCKQUOTE,
    HEADER,
    LINK,
    ;

    public static EditorFormat getFormat(String val) {
        switch (val) {
            case "undo":
                return UNDO;
            case "redo":
                return REDO;
            case "bold":
                return BOLD;
            case "italic":
                return ITALIC;
            case "underline":
                return UNDERLINE;
            case "strikeThrough":
                return STRIKETHROUGH;
            case "superscript":
                return SUPERSCRIPT;
            case "subscript":
                return SUBSCRIPT;
            case "indent":
                return INDENT;
            case "outdent":
                return OUTDENT;
            case "justifyLeft":
                return ALIGN_LEFT;
            case "justifyCenter":
                return ALIGN_CENTER;
            case "justifyRight":
                return ALIGN_RIGHT;
            case "insertUnorderedList":
                return BULLET_LIST;
            case "insertOrderedList":
                return ORDERED_LIST;
            case "blockquote":
                return BLOCKQUOTE;
            case "header":
                return HEADER;
            case "link":
                return LINK;
            default:
                return null;
        }
    }
}

