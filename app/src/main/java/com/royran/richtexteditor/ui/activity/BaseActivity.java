package com.royran.richtexteditor.ui.activity;

import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.royran.richtexteditor.R;

import butterknife.ButterKnife;

public abstract class BaseActivity extends AppCompatActivity {

    public abstract int getResourceId();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.toolbar));
        }
        setContentView(getResourceId());
        ButterKnife.bind(this);
    }

    protected void initToolBar(Toolbar toolbar, boolean hasBackArrow) {
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            toolbar.setTitleTextColor(ActivityCompat.getColor(this, R.color.title_text_color));
            toolbar.setTitle(getTitle());
            if (hasBackArrow) {
                setActionBarIndicator(R.drawable.ic_arrow_back_white);
            }
        }
    }

    protected void setActionBarIndicator(int resId) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(resId);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            ActivityCompat.finishAfterTransition(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
