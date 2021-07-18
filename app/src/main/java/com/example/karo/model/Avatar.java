package com.example.karo.model;

import android.graphics.Bitmap;

public class Avatar {
    private Bitmap bitmap;
    private String ref;

    public Avatar() {
    }

    public Avatar(Bitmap bitmap, String ref) {
        this.bitmap = bitmap;
        this.ref = ref;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }
}
