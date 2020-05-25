package com.royran.rteditor;


import android.content.Context;
import android.content.res.TypedArray;
import android.text.Html;
import android.text.InputType;
import android.text.method.KeyListener;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.royran.rteditor.editor.EditorFormat;
import com.royran.rteditor.editor.EditorListener;
import com.royran.rteditor.editor.OnCompleteListener;
import com.royran.rteditor.editor.RichTextEditorInternal;
import com.royran.rteditor.media.ImageChooserActivity;
import com.royran.rteditor.media.ImageEvent;
import com.royran.rteditor.utils.Helper;
import com.royran.rteditor.widget.LinkDialog;
import com.royran.rteditor.widget.ToolbarImageButton;
import com.royran.rteditor.widget.colorpicker.ColorPickerListener;
import com.royran.rteditor.widget.colorpicker.ColorPickerWindow;
import com.royran.rteditor.widget.fontsizepicker.FontSizeChangedListener;
import com.royran.rteditor.widget.fontsizepicker.FontSizePickerWindow;
import com.yalantis.ucrop.util.FileUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RichTextEditor extends RelativeLayout implements View.OnClickListener, EditorListener {
    public static final int DEFAULT_FONT_SIZE = 16;

    private final static String TAG = "MainActivity";
    private static final String SCHEME = "file";
    private static final String IMAGE_PATH = "getImage";

    private WebView mWebView;
    private EditText mTitle;

    private FrameLayout mEditorToolbar;
    private Context mContext;

    private ToolbarImageButton mBoldButton;
    private ToolbarImageButton mItalicButton;
    private ToolbarImageButton mUnderlineButton;
    private ToolbarImageButton mStrikethroughButton;
    private ToolbarImageButton mFontSizeButton;
    private ToolbarImageButton mTextColorButton;
    private ToolbarImageButton mTextBGColorButton;
    private ToolbarImageButton mSubscriptButton;
    private ToolbarImageButton mSuperscriptButton;
    private ToolbarImageButton mInsertNumberButton;
    private ToolbarImageButton mInsertBulletsButton;
    private ToolbarImageButton mAlignLeftButton;
    private ToolbarImageButton mAlignCenterButton;
    private ToolbarImageButton mAlignRightButton;
    private ImageButton mBlockquoteButton;
    private RichTextEditorListener mEditorListener;

    private RichTextEditorInternal mEditor;
    private int mTextColorIndex = -1;
    private int mTextBGColorIndex = -1;
    private int mFontSize = DEFAULT_FONT_SIZE;
    private List<String> mImagePaths;
    private boolean mEditable;
    private KeyListener mTitleInputKeyListener;

    public RichTextEditor(Context context) {
        this(context, null);
    }

    public RichTextEditor(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RichTextEditor(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.RichTextEditor);
        boolean editable = attributes.getBoolean(R.styleable.RichTextEditor_editable, true);
        init(context, editable);
    }

    private void init(Context context, boolean editable) {
        mContext = context;
        mEditable = editable;
        mImagePaths = new ArrayList<>();
        setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        View view = LayoutInflater.from(context).inflate(R.layout.richtext_editor, null);
        initViews(view);
        addView(view);
        mEditor = new RichTextEditorInternal(mWebView);
        mEditor.setEditorListener(this);
        mFontSizeButton.setText(mFontSize + "");
        EventBus.getDefault().register(this);
    }

    private void initViews(View view) {
        mWebView = view.findViewById(R.id.web_editor);
        mTitle = view.findViewById(R.id.title);
        mEditorToolbar = view.findViewById(R.id.editor_toolbar);
        mBoldButton = view.findViewById(R.id.btn_bold);
        mItalicButton = view.findViewById(R.id.btn_italic);
        mUnderlineButton = view.findViewById(R.id.btn_underline);
        mStrikethroughButton = view.findViewById(R.id.btn_strikethrough);
        mFontSizeButton = view.findViewById(R.id.btn_font_size);
        mTextColorButton = view.findViewById(R.id.btn_text_color);
        mTextBGColorButton = view.findViewById(R.id.btn_text_bg_color);
        mSubscriptButton = view.findViewById(R.id.btn_subscript);
        mSuperscriptButton = view.findViewById(R.id.btn_superscript);
        mBlockquoteButton = view.findViewById(R.id.btn_blockquote);
        mInsertNumberButton = view.findViewById(R.id.btn_insert_numbers);
        mInsertBulletsButton = view.findViewById(R.id.btn_insert_bullets);
        mAlignLeftButton = view.findViewById(R.id.btn_align_left);
        mAlignCenterButton = view.findViewById(R.id.btn_align_center);
        mAlignRightButton = view.findViewById(R.id.btn_align_right);
        view.findViewById(R.id.btn_undo).setOnClickListener(this);
        view.findViewById(R.id.btn_redo).setOnClickListener(this);
        view.findViewById(R.id.btn_indent).setOnClickListener(this);
        view.findViewById(R.id.btn_outdent).setOnClickListener(this);
        view.findViewById(R.id.btn_insert_image).setOnClickListener(this);
        view.findViewById(R.id.btn_insert_link).setOnClickListener(this);
        view.findViewById(R.id.btn_remove_format).setOnClickListener(this);
        mBoldButton.setOnClickListener(this);
        mItalicButton.setOnClickListener(this);
        mUnderlineButton.setOnClickListener(this);
        mStrikethroughButton.setOnClickListener(this);
        mFontSizeButton.setOnClickListener(this);
        mTextColorButton.setOnClickListener(this);
        mTextBGColorButton.setOnClickListener(this);
        mSubscriptButton.setOnClickListener(this);
        mSuperscriptButton.setOnClickListener(this);
        mBlockquoteButton.setOnClickListener(this);
        mInsertNumberButton.setOnClickListener(this);
        mInsertBulletsButton.setOnClickListener(this);
        mAlignLeftButton.setOnClickListener(this);
        mAlignCenterButton.setOnClickListener(this);
        mAlignRightButton.setOnClickListener(this);

        mTitle.setOnKeyListener((v, keyCode, event) -> {
            if (!mEditable) {
                return true;
            }
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    return true;
                }
            }
            return false;
        });
        mTitleInputKeyListener = mTitle.getKeyListener();
    }

    public void setTitle(String title) {
        if (!mEditable) {
            mTitle.setKeyListener(null); // for ellipsize="end" effectively
        }
        mTitle.setText(title);
    }

    public String getTitle() {
        return mTitle.getText().toString();
    }

    public void setContent(String content, OnCompleteListener listener) {
        mImagePaths = getImagesFromHTML(content);
        mEditor.setContent(content, listener);
    }

    private List<String> getImagesFromHTML(String html) {
        List<String> imagePaths = new ArrayList<>();
        Html.fromHtml(html, source -> {
            imagePaths.add(source);
            return null;
        }, null);
        return imagePaths;
    }

    public void getContent(RichTextEditorInternal.ContentFetchListener listener) {
        mEditor.getContent(content -> {
            checkImagesInContent(content);
            if (listener != null) {
                listener.onContentFetched(content);
            }
        });
    }

    private void checkImagesInContent(String content) {
        List<String> imagePaths = getImagesFromHTML(content);
        Set<String> set = new HashSet<>(imagePaths);
        for (String path : mImagePaths) {
            if (!set.contains(path)) {
                FileUtils.removeFile(path);
            }
        }
        mImagePaths = imagePaths;
    }

    public void removeFiles() {
        for (String path : mImagePaths) {
            FileUtils.removeFile(path);
        }
        mImagePaths.clear();
    }

    public void onDestroy() {
        removeFiles();
        EventBus.getDefault().unregister(this);
    }

    public void setRichEditorTextListener(RichTextEditorListener listener) {
        mEditorListener = listener;
    }

    void onUndoButtonClicked() {
        mEditor.undo();
    }

    void onRedoButtonClicked() {
        mEditor.redo();
    }

    void onInsertImageButtonClicked() {
        pickImage();
    }

    void onInsertLinkButtonClicked() {
        mEditor.getSelectedText(selected -> LinkDialog.show(mContext, null, selected, mLinkDialogListener));
    }

    void onBoldButtonClicked() {
        mEditor.setBold();
        mBoldButton.setChecked(!mBoldButton.isChecked());
    }

    void onFontSizeButtonClicked() {
        mFontSizeButton.setChecked(!mFontSizeButton.isChecked());
        FontSizePickerWindow.show(mContext, mEditorToolbar, mFontSize, mFontSizeChangedListener);
    }

    void onTextColorButtonClicked() {
        mTextColorButton.setChecked(!mTextColorButton.isChecked());
        ColorPickerWindow.show(mContext, mEditorToolbar, mTextColorIndex, mTextColorPickerListener);
    }

    void onTextBGColorButtonClicked() {
        mTextBGColorButton.setChecked(!mTextBGColorButton.isChecked());
        ColorPickerWindow.show(mContext, mEditorToolbar, mTextBGColorIndex, mTextBGColorPickerListener);
    }

    void onItalicButtonClicked() {
        mEditor.setItalic();
        mItalicButton.setChecked(!mItalicButton.isChecked());
    }

    void onUnderlineButtonClicked() {
        mEditor.setUnderline();
        mUnderlineButton.setChecked(!mUnderlineButton.isChecked());
    }

    void onStrikethroughButtonClicked() {
        mEditor.setStrikethrough();
        mStrikethroughButton.setChecked(!mStrikethroughButton.isChecked());
    }

    void onSubscriptButtonClicked() {
        mEditor.setSubscript();
        mSubscriptButton.setChecked(!mSubscriptButton.isChecked());
    }

    void onSuperscriptButtonClicked() {
        mEditor.setSuperscript();
        mSuperscriptButton.setChecked(!mSuperscriptButton.isChecked());
    }

    void onBlockquoteButtonClicked() {
        mEditor.setBlockquote();
//        mBlockquoteButton.setChecked(!mBlockquoteButton.isChecked());
    }

    void onInsertNumbersButtonClicked() {
        mEditor.setNumbers();
        mInsertNumberButton.setChecked(!mSuperscriptButton.isChecked());
    }

    void onInsertBulletsButtonClicked() {
        mEditor.setBullets();
        mInsertBulletsButton.setChecked(!mInsertBulletsButton.isChecked());
    }

    void onIndentButtonClicked() {
        mEditor.setIndent();
    }

    void onOutdentButtonClicked() {
        mEditor.setOutdent();
    }

    void onAlignLeftButtonClicked() {
        mEditor.setTextAlignLeft();
        mAlignLeftButton.setChecked(!mAlignLeftButton.isChecked());
    }

    void onAlignCenterButtonClicked() {
        mEditor.setTextAlignCenter();
        mAlignCenterButton.setChecked(!mAlignCenterButton.isChecked());
    }

    void onAlignRightButtonClicked() {
        mEditor.setTextAlignRight();
        mAlignRightButton.setChecked(!mAlignRightButton.isChecked());
    }

    void onRemoveFormatButtonClicked() {
        resetColorIndex();
        mEditor.removeFormat();
    }

    private void resetColorIndex() {
        mTextColorIndex = -1;
        mTextBGColorIndex = -1;
    }

    @Override
    public void onFormatChanged(Map<EditorFormat, Object> formats) {
        Log.d(TAG, "onFormatChanged");
        refreshFormatStatus(formats);
    }

    @Override
    public void onCursorChanged(Map<EditorFormat, Object> enabledFormats) {
        Log.d(TAG, "onCursorChanged");
        uncheckButtons();
        refreshFormatStatus(enabledFormats);
    }

    @Override
    public void onLinkClicked(String url, String title) {
        LinkDialog.show(mContext, url, title, mLinkDialogListener);
    }

    @Override
    public void onImageClicked(String src, String alt) {
        if (mEditorListener != null) {
            mEditorListener.onImageClicked(src);
        }
    }

    @Override
    public void onImageRemoved(String src) {
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onImageAdded(ImageEvent event) {
        String imagePath = event.getUri().getPath();
        mEditor.insertImage(imagePath, "image");
    }

    @Override
    public void onPageLoaded() {
        if (mEditorListener != null) {
            mEditorListener.onInitialized();
        }
        setEditable(mEditable);
    }

    public void setEditable(boolean editable) {
        mEditable = editable;
        if (mEditable) {
            mTitle.setKeyListener(mTitleInputKeyListener);
            mTitle.setInputType(InputType.TYPE_CLASS_TEXT);
            mEditor.setEditorEnabled(true);
            mEditor.focus();
            Helper.showKeyboard(mContext);
            mEditorToolbar.setVisibility(VISIBLE);
        } else {
            mTitle.setInputType(InputType.TYPE_NULL);
            mEditor.setEditorEnabled(false);
            Helper.hideKeyboard(mContext);
            mEditorToolbar.setVisibility(GONE);
        }
    }

    private void uncheckButtons() {
        mBoldButton.setChecked(false);
        mItalicButton.setChecked(false);
        mUnderlineButton.setChecked(false);
        mStrikethroughButton.setChecked(false);
        mSubscriptButton.setChecked(false);
        mSuperscriptButton.setChecked(false);
        mInsertNumberButton.setChecked(false);
        mInsertBulletsButton.setChecked(false);
        mAlignLeftButton.setChecked(false);
        mAlignCenterButton.setChecked(false);
        mAlignRightButton.setChecked(false);
    }

    private void refreshFormatStatus(Map<EditorFormat, Object> formats) {
        for (Map.Entry<EditorFormat, Object> entry : formats.entrySet()) {
            EditorFormat format = entry.getKey();
            Object value = entry.getValue();
            switch (format) {
                case BOLD:
                    if (value instanceof Boolean) {
                        mBoldButton.setChecked((Boolean) value);
                    }
                    break;
                case ITALIC:
                    if (value instanceof Boolean) {
                        mItalicButton.setChecked((Boolean) value);
                    }
                    break;
                case UNDERLINE:
                    if (value instanceof Boolean) {
                        mUnderlineButton.setChecked((Boolean) value);
                    }
                    break;
                case STRIKETHROUGH:
                    if (value instanceof Boolean) {
                        mStrikethroughButton.setChecked((Boolean) value);
                    }
                    break;
                case SUBSCRIPT:
                    if (value instanceof Boolean) {
                        mSubscriptButton.setChecked((Boolean) value);
                    }
                    break;
                case SUPERSCRIPT:
                    if (value instanceof Boolean) {
                        mSuperscriptButton.setChecked((Boolean) value);
                    }
                    break;
                case ALIGN_LEFT:
                    if (value instanceof Boolean) {
                        mAlignLeftButton.setChecked((Boolean) value);
                    }
                    break;
                case ALIGN_CENTER:
                    if (value instanceof Boolean) {
                        mAlignCenterButton.setChecked((Boolean) value);
                    }
                    break;
                case ALIGN_RIGHT:
                    if (value instanceof Boolean) {
                        mAlignRightButton.setChecked((Boolean) value);
                    }
                    break;
                case BLOCKQUOTE:
                    break;
                case BULLET_LIST:
                    if (value instanceof Boolean) {
                        mInsertBulletsButton.setChecked((Boolean) value);
                    }
                    break;
                case ORDERED_LIST:
                    if (value instanceof Boolean) {
                        mInsertNumberButton.setChecked((Boolean) value);
                    }
                    break;
                case HEADER:
                    break;
                case LINK:
                    break;
                default:
                    break;
            }
        }
    }

    private LinkDialog.LinkDialogListener mLinkDialogListener = new LinkDialog.LinkDialogListener() {
        @Override
        public void onInsert(String newAddress, String linkText) {
            mEditor.insertLink(newAddress, linkText);
        }

        @Override
        public void onRemove() {
            mEditor.removeLink();
        }
    };

    private ColorPickerListener mTextColorPickerListener = new ColorPickerListener() {
        @Override
        public void onPickColor(int index, int color) {
            mTextColorIndex = index;
            mEditor.setTextColor(color);
        }

        @Override
        public void onDismiss() {
            mTextColorButton.setChecked(false);
        }

        @Override
        public void onColorRemove() {
            mTextColorIndex = -1;
            mEditor.removeTextColor();
        }
    };

    private ColorPickerListener mTextBGColorPickerListener = new ColorPickerListener() {
        @Override
        public void onPickColor(int index, int color) {
            mTextBGColorIndex = index;
            mEditor.setTextBGColor(color);
        }

        @Override
        public void onDismiss() {
            mTextBGColorButton.setChecked(false);
        }

        @Override
        public void onColorRemove() {
            mTextBGColorIndex = -1;
            mEditor.removeTextBGColor();
        }
    };

    private FontSizeChangedListener mFontSizeChangedListener = new FontSizeChangedListener() {
        @Override
        public void onFontSizeChanged(int fontSize) {
            mFontSize = fontSize;
            mEditor.setFontSize(fontSize);
            mFontSizeButton.setText(fontSize + "");
        }

        @Override
        public void onDismiss() {
            mFontSizeButton.setChecked(false);
        }
    };

    private void pickImage() {
        Helper.hideKeyboard(mContext);
        String imagePath = String.format("%s/%s.jpg",
                mContext.getExternalFilesDir("images"),
                Helper.getUUID());
        ImageChooserActivity.open(mContext, imagePath);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_undo) {
            onUndoButtonClicked();
        } else if (id == R.id.btn_redo) {
            onRedoButtonClicked();
        } else if (id == R.id.btn_insert_image) {
            onInsertImageButtonClicked();
        } else if (id == R.id.btn_insert_link) {
            onInsertLinkButtonClicked();
        } else if (id == R.id.btn_bold) {
            onBoldButtonClicked();
        } else if (id == R.id.btn_font_size) {
            onFontSizeButtonClicked();
        } else if (id == R.id.btn_text_color) {
            onTextColorButtonClicked();
        } else if (id == R.id.btn_text_bg_color) {
            onTextBGColorButtonClicked();
        } else if (id == R.id.btn_italic) {
            onItalicButtonClicked();
        } else if (id == R.id.btn_underline) {
            onUnderlineButtonClicked();
        } else if (id == R.id.btn_strikethrough) {
            onStrikethroughButtonClicked();
        } else if (id == R.id.btn_subscript) {
            onSubscriptButtonClicked();
        } else if (id == R.id.btn_superscript) {
            onSuperscriptButtonClicked();
        } else if (id == R.id.btn_blockquote) {
            onBlockquoteButtonClicked();
        } else if (id == R.id.btn_insert_numbers) {
            onInsertNumbersButtonClicked();
        } else if (id == R.id.btn_insert_bullets) {
            onInsertBulletsButtonClicked();
        } else if (id == R.id.btn_indent) {
            onIndentButtonClicked();
        } else if (id == R.id.btn_outdent) {
            onOutdentButtonClicked();
        } else if (id == R.id.btn_align_left) {
            onAlignLeftButtonClicked();
        } else if (id == R.id.btn_align_center) {
            onAlignCenterButtonClicked();
        } else if (id == R.id.btn_align_right) {
            onAlignRightButtonClicked();
        } else if (id == R.id.btn_remove_format) {
            onRemoveFormatButtonClicked();
        }
    }
}
