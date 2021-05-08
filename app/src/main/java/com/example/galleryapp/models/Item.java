package com.example.galleryapp.models;

import android.graphics.Bitmap;

/**
 * Represents the item for the gallery activity
 */
public class Item {
    public Bitmap image;
    public int color;
    public String label;
    public String url;

    /**
     * @param image Image
     * @param color Image color
     * @param label Image label
     * @param url   Image Url
     */
    public Item(Bitmap image, int color, String label, String url) {
        this.image = image;
        this.color = color;
        this.label = label;
        this.url = url;
    }

    /**
     * @param color Image color
     * @param label Image Label
     * @param url   Image Url
     */
    public Item(int color, String label, String url) {
        this.color = color;
        this.label = label;
        this.url = url;
    }
}
