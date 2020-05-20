package com.royran.richtexteditor.ui.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.royran.richtexteditor.R;
import com.royran.richtexteditor.editor.EditorFormat;
import com.royran.richtexteditor.editor.EditorListener;
import com.royran.richtexteditor.editor.RichTextEditor;
import com.royran.richtexteditor.ui.widget.LinkDialog;
import com.royran.richtexteditor.ui.widget.RTToolbarImageButton;
import com.royran.richtexteditor.ui.widget.colorpicker.ColorPickerListener;
import com.royran.richtexteditor.ui.widget.colorpicker.ColorPickerWindow;
import com.royran.richtexteditor.ui.widget.fontsizepicker.FontSizeChangedListener;
import com.royran.richtexteditor.ui.widget.fontsizepicker.FontSizePickerWindow;
import com.royran.richtexteditor.utils.Helper;
import com.yalantis.ucrop.UCrop;
import com.yalantis.ucrop.UCropActivity;

import java.io.File;
import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;

public class MainActivity extends BaseActivity implements EditorListener {
    public static final int DEFAULT_FONT_SIZE = 16;

    public static final int PICK_IMAGE_REQUEST = 0x01;

    private final static String TAG = "MainActivity";

    @BindView(R.id.web_editor)
    WebView mWebView;

    @BindView(R.id.editor_toolbar)
    FrameLayout mEditorToolbar;

    @BindView(R.id.btn_bold)
    RTToolbarImageButton mBoldButton;

    @BindView(R.id.btn_italic)
    RTToolbarImageButton mItalicButton;

    @BindView(R.id.btn_underline)
    RTToolbarImageButton mUnderlineButton;

    @BindView(R.id.btn_strikethrough)
    RTToolbarImageButton mStrikethroughButton;

    @BindView(R.id.btn_font_size)
    RTToolbarImageButton mFontSizeButton;

    @BindView(R.id.btn_text_color)
    RTToolbarImageButton mTextColorButton;

    @BindView(R.id.btn_text_bg_color)
    RTToolbarImageButton mTextBGColorButton;

    @BindView(R.id.btn_subscript)
    RTToolbarImageButton mSubscriptButton;

    @BindView(R.id.btn_superscript)
    RTToolbarImageButton mSuperscriptButton;

    @BindView(R.id.btn_blockquote)
    ImageButton mBlockquoteButton;

    @BindView(R.id.btn_insert_numbers)
    RTToolbarImageButton mInsertNumberButton;

    @BindView(R.id.btn_insert_bullets)
    RTToolbarImageButton mInsertBulletsButton;

    @BindView(R.id.btn_align_left)
    RTToolbarImageButton mAlignLeftButton;

    @BindView(R.id.btn_align_center)
    RTToolbarImageButton mAlignCenterButton;

    @BindView(R.id.btn_align_right)
    RTToolbarImageButton mAlignRightButton;

    private RichTextEditor mEditor;
    private int mTextColorIndex = -1;
    private int mTextBGColorIndex = -1;
    private int mFontSize = DEFAULT_FONT_SIZE;

    @Override
    public int getResourceId() {
        return R.layout.activity_main;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initToolBar(findViewById(R.id.toolbar), false);
//        setTitle(getString(R.string.edit));
        mEditor = new RichTextEditor(mWebView);
        mEditor.setEditorListener(this);
        mFontSizeButton.setText(mFontSize + "");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_preview:
                showHTMLPreview();
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @OnClick(R.id.btn_undo)
    void onUndoButtonClicked() {
        mEditor.undo();
    }

    @OnClick(R.id.btn_redo)
    void onRedoButtonClicked() {
        mEditor.redo();
    }

    @OnClick(R.id.btn_insert_image)
    void onInsertImageButtonClicked() {
        pickImage();
    }

    @OnClick(R.id.btn_insert_link)
    void onInsertLinkButtonClicked() {
        mEditor.getSelectedText(selected -> LinkDialog.show(this, null, selected, mLinkDialogListener));
    }

    @OnClick(R.id.btn_bold)
    void onBoldButtonClicked() {
        mEditor.setBold();
        mBoldButton.setChecked(!mBoldButton.isChecked());
    }

    @OnClick(R.id.btn_font_size)
    void onFontSizeButtonClicked() {
        mFontSizeButton.setChecked(!mFontSizeButton.isChecked());
        FontSizePickerWindow.show(this, mEditorToolbar, mFontSize, mFontSizeChangedListener);
    }

    @OnClick(R.id.btn_text_color)
    void onTextColorButtonClicked() {
        mTextColorButton.setChecked(!mTextColorButton.isChecked());
        ColorPickerWindow.show(this, mEditorToolbar, mTextColorIndex, mTextColorPickerListener);
    }

    @OnClick(R.id.btn_text_bg_color)
    void onTextBGColorButtonClicked() {
        mTextBGColorButton.setChecked(!mTextBGColorButton.isChecked());
        ColorPickerWindow.show(this, mEditorToolbar, mTextBGColorIndex, mTextBGColorPickerListener);
    }

    @OnClick(R.id.btn_italic)
    void onItalicButtonClicked() {
        mEditor.setItalic();
        mItalicButton.setChecked(!mItalicButton.isChecked());
    }

    @OnClick(R.id.btn_underline)
    void onUnderlineButtonClicked() {
        mEditor.setUnderline();
        mUnderlineButton.setChecked(!mUnderlineButton.isChecked());
    }

    @OnClick(R.id.btn_strikethrough)
    void onStrikethroughButtonClicked() {
        mEditor.setStrikethrough();
        mStrikethroughButton.setChecked(!mStrikethroughButton.isChecked());
    }

    @OnClick(R.id.btn_subscript)
    void onSubscriptButtonClicked() {
        mEditor.setSubscript();
        mSubscriptButton.setChecked(!mSubscriptButton.isChecked());
    }

    @OnClick(R.id.btn_superscript)
    void onSuperscriptButtonClicked() {
        mEditor.setSuperscript();
        mSuperscriptButton.setChecked(!mSuperscriptButton.isChecked());
    }

    @OnClick(R.id.btn_blockquote)
    void onBlockquoteButtonClicked() {
        mEditor.setBlockquote();
//        mBlockquoteButton.setChecked(!mBlockquoteButton.isChecked());
    }

    @OnClick(R.id.btn_insert_numbers)
    void onInsertNumbersButtonClicked() {
        mEditor.setNumbers();
        mInsertNumberButton.setChecked(!mSuperscriptButton.isChecked());
    }

    @OnClick(R.id.btn_insert_bullets)
    void onInsertBulletsButtonClicked() {
        mEditor.setBullets();
        mInsertBulletsButton.setChecked(!mInsertBulletsButton.isChecked());
    }

    @OnClick(R.id.btn_indent)
    void onIndentButtonClicked() {
        mEditor.setIndent();
    }

    @OnClick(R.id.btn_outdent)
    void onOutdentButtonClicked() {
        mEditor.setOutdent();
    }

    @OnClick(R.id.btn_align_left)
    void onAlignLeftButtonClicked() {
        mEditor.setTextAlignLeft();
        mAlignLeftButton.setChecked(!mAlignLeftButton.isChecked());
    }

    @OnClick(R.id.btn_align_center)
    void onAlignCenterButtonClicked() {
        mEditor.setTextAlignCenter();
        mAlignCenterButton.setChecked(!mAlignCenterButton.isChecked());
    }

    @OnClick(R.id.btn_align_right)
    void onAlignRightButtonClicked() {
        mEditor.setTextAlignRight();
        mAlignRightButton.setChecked(!mAlignRightButton.isChecked());
    }

    @OnClick(R.id.btn_remove_format)
    void onRemoveFormatButtonClicked() {
        resetColorIndex();
        mEditor.removeFormat();
    }

    private void resetColorIndex() {
        mTextColorIndex = -1;
        mTextBGColorIndex = -1;
    }

    private void showHTMLPreview() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("HTML Preview");
        TextView textView = new TextView(this);
        mEditor.getHTML(html -> runOnUiThread(() -> textView.setText(html)));
        builder.setView(textView);
        builder.show();
    }

    @Override
    public void onFormatChanged(Map<EditorFormat, Object> formats) {
        Log.d(TAG, "onFormatChanged");
        runOnUiThread(() -> refreshFormatStatus(formats));
    }

    @Override
    public void onCursorChanged(Map<EditorFormat, Object> enabledFormats) {
        Log.d(TAG, "onCursorChanged");
        runOnUiThread(() -> {
            uncheckButtons();
            refreshFormatStatus(enabledFormats);
        });
    }

    @Override
    public void onLinkClicked(String url, String title) {
        LinkDialog.show(this, url, title, mLinkDialogListener);
    }

    @Override
    public void onImageClicked(String src, String alt) {
        Toast.makeText(this, "alt="+alt+", src=" + src, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPageLoaded() {
        mEditor.setEditorEnabled(true);
        mEditor.focus();
        showKeyboard();
    }


    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getWindow().getDecorView().getRootView().getWindowToken(),0);
    }

    private void showKeyboard() {
        hideKeyboard();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
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

    private boolean pickImage() {
        hideKeyboard();
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null)
                .addCategory(Intent.CATEGORY_OPENABLE)
                .setType("image/*");
        String title = getString(R.string.pick_image);
        startActivityForResult(Intent.createChooser(intent, title), PICK_IMAGE_REQUEST);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == PICK_IMAGE_REQUEST) {
                startCrop(data.getData());
            } else if (requestCode == UCrop.REQUEST_CROP) {
                final Uri resultUri = UCrop.getOutput(data);
                mEditor.insertImage(resultUri.getPath(), "image");
            } else if (resultCode == UCrop.RESULT_ERROR) {
                final Throwable cropError = UCrop.getError(data);
                cropError.printStackTrace();
                Toast.makeText(this, cropError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private UCrop uCropWithOptions(@NonNull UCrop uCrop) {
        UCrop.Options options = new UCrop.Options();
        options.setCompressionQuality(100); // TODO: read from config
        options.setFreeStyleCropEnabled(true);
        options.setCompressionFormat(Bitmap.CompressFormat.JPEG);
        options.setAllowedGestures(UCropActivity.SCALE, UCropActivity.SCALE, UCropActivity.SCALE);
        options.setMaxScaleMultiplier(100);
        return uCrop.withOptions(options);
    }

    private void startCrop(@NonNull Uri uri) {
        String imagePath = String.format("%s/%s.jpg", getExternalFilesDir("images"), Helper.getUUID());
        UCrop uCrop = UCrop.of(uri, Uri.fromFile(new File(imagePath)));
        uCrop = uCropWithOptions(uCrop);
        uCrop = uCrop.withMaxResultSize(1600, 900);
        uCrop.start(this);
    }
}
