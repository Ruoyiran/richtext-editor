package com.royran.rteditor.media;

import android.net.Uri;

public class ImageEvent {
    private Uri mUri;
    public ImageEvent(Uri uri) {
        mUri = uri;
    }

    public Uri getUri() {
        return mUri;
    }
}
