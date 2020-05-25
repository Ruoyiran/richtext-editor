package com.royran.rteditor.media;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.royran.rteditor.R;
import com.yalantis.ucrop.UCrop;
import com.yalantis.ucrop.UCropActivity;

import org.greenrobot.eventbus.EventBus;

import java.io.File;

public class ImageChooserActivity extends AppCompatActivity {
    private final static String TAG = "ImageChooserActivity";
    private final static int PICK_IMAGE_REQUEST = 0x100;
    private final static String PREFIX = ImageChooserActivity.class.getSimpleName();
    public final static String EXTRA_OUTPUT_IMAGE_PATH = PREFIX + "_EXTRA_OUTPUT_IMAGE_PATH";
    private String mOutputPath;

    public static void open(Context context, String outputPath) {
        Intent intent = new Intent(context, ImageChooserActivity.class);
        intent.putExtra(EXTRA_OUTPUT_IMAGE_PATH, outputPath);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        mOutputPath = intent.getStringExtra(EXTRA_OUTPUT_IMAGE_PATH);
        if (TextUtils.isEmpty(mOutputPath)) {
            mOutputPath = getCacheDir() + "/sample.jpg";
        }
        Log.i(TAG, "output file path: " + mOutputPath);
        pickImage();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == PICK_IMAGE_REQUEST) {
                startCrop(data.getData());
            } else if (requestCode == UCrop.REQUEST_CROP) {
                final Uri resultUri = UCrop.getOutput(data);
                EventBus.getDefault().post(new ImageEvent(resultUri));
                finish();
            } else if (resultCode == UCrop.RESULT_ERROR) {
                final Throwable cropError = UCrop.getError(data);
                cropError.printStackTrace();
                finish();
            }
        } else {
            finish();
        }
    }

    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null)
                .addCategory(Intent.CATEGORY_OPENABLE)
                .setType("image/*");
        String title = getString(R.string.rte_pick_image);
        startActivityForResult(Intent.createChooser(intent, title), PICK_IMAGE_REQUEST);
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
        UCrop uCrop = UCrop.of(uri, Uri.fromFile(new File(mOutputPath)));
        uCrop = uCropWithOptions(uCrop);
        uCrop = uCrop.withMaxResultSize(1600, 900);
        uCrop.start(this);
    }
}
