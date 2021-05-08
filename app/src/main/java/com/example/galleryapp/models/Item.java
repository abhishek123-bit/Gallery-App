package com.example.galleryapp.models;

import android.graphics.Bitmap;

/**
 * Represents the item for the gallery activity
 */
public class Item {
    public Bitmap image;
    public int color;
    public String label;

    /**
     * @param image Store Image
     * @param color Store Image color
     * @param label Store Image label
     */
    public Item(Bitmap image, int color, String label) {
        this.image = image;
        this.color = color;
        this.label = label;
    }
}
