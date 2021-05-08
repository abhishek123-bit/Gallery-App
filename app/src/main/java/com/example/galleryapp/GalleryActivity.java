package com.example.galleryapp;

import androidx.annotation.NonNull;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.galleryapp.databinding.ActivityGalleryBinding;
import com.example.galleryapp.databinding.ItemCardBinding;
import com.example.galleryapp.models.Item;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;


public class GalleryActivity extends AppCompatActivity {
    ActivityGalleryBinding b;
    List<Item> itemList = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityGalleryBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        //Load data from sharedPreferences
        loadSharedPreferenceData();

    }

    /**
     * Load data from sharedPreferences
     * Fetch Images from caches
     */
    private void loadSharedPreferenceData() {
        String items = getPreferences(MODE_PRIVATE).getString("ITEMS", null);
        if (items == null) {
            return;
        }

        Gson gson = new Gson();
        Type type = new TypeToken<List<Item>>() {
        }.getType();

        itemList = gson.fromJson(items, type);

        //Fetch data from caches
        for (Item item : itemList) {
            Glide.with(this)
                    .asBitmap()
                    .onlyRetrieveFromCache(true)
                    .load(item.url)
                    .into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            ItemCardBinding binding = ItemCardBinding.inflate(getLayoutInflater());
                            Log.d("Abhi", "onResourceReady: ");
                            binding.fetchImage.setImageBitmap(resource);
                            binding.Title.setBackgroundColor(item.color);
                            binding.Title.setText(item.label);
                            b.linearLayout.addView(binding.getRoot());
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {

                        }
                    });

        }


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gallery, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.addPhoto) {
            showAddImageDialog();
            return true;
        }
        return false;
    }


    /**
     * To show the dialog to add image
     */
    private void showAddImageDialog() {
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        new AddImageDialog()
                .showDialog(this, new AddImageDialog.OnCompleteListener() {
                    @Override
                    public void onImageAdd(Item item) {
                        GalleryActivity.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                        inflateViewForItem(item);
                    }

                    @Override
                    public void onError(String error) {
                        GalleryActivity.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                        new MaterialAlertDialogBuilder(GalleryActivity.this)
                                .setTitle("Error")
                                .setMessage(error)
                                .show();

                    }
                });
    }

    /**
     * To inflate the view for the incoming item
     *
     * @param item {@link Item}
     */
    private void inflateViewForItem(Item item) {
        //Inflate layout
        ItemCardBinding binding = ItemCardBinding.inflate(getLayoutInflater());

        //Bind data
        binding.fetchImage.setImageBitmap(item.image);
        binding.Title.setBackgroundColor(item.color);
        binding.Title.setText(item.label);

        b.linearLayout.addView(binding.getRoot());

        //Save Item
        SaveDataInSharedPreference(item);
    }

    /**
     * Save data in sharedPreferences
     *
     * @param item {@link Item}
     */
    private void SaveDataInSharedPreference(Item item) {
        Item storeItem = new Item(item.color, item.label, item.url);

        //StoreItem in sharedPreferences
        Gson gson = new Gson();
        String json;

        if (itemList == null) {
            itemList = new ArrayList<>();
        }

        itemList.add(storeItem);
        json = gson.toJson(itemList);
        getPreferences(MODE_PRIVATE).edit().putString("ITEMS", json).apply();
    }

}