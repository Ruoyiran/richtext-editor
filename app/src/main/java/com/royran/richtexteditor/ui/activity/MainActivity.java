package com.royran.richtexteditor.ui.activity;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.royran.richtexteditor.R;
import com.royran.rteditor.RichTextEditor;

import butterknife.BindView;

public class MainActivity extends BaseActivity {
    private final static String TAG = "MainActivity";

    @BindView(R.id.richtext_editor)
    RichTextEditor mEditor;

    @Override
    public int getResourceId() {
        return R.layout.activity_main;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initToolBar(findViewById(R.id.toolbar), false);
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

    private void showHTMLPreview() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("HTML Preview");
        TextView textView = new TextView(this);
        mEditor.getContent(content -> textView.setText(content));
        builder.setView(textView);
        builder.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mEditor.onDestroy();
    }
}
