package com.example.galleryapp;

import androidx.annotation.NonNull;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.TextView;
import android.widget.Toast;

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
    List<Item> itemList;
    int selectedPosition;
    List<Item> removeItem;
    private boolean isEdited;
    private boolean isAdd;

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
        if (items == null || items.equals("[]")) {
            return;
        }
        b.heading.setVisibility(View.GONE);
        Log.d("Abhi", "loadSharedPreferenceData: " + items);
        Gson gson = new Gson();
        Type type = new TypeToken<List<Item>>() {
        }.getType();

        itemList = gson.fromJson(items, type);

        //Fetch data from caches
        for (Item item : itemList) {
            ItemCardBinding binding = ItemCardBinding.inflate(getLayoutInflater());

            Glide.with(this)
                    .asBitmap()
                    .onlyRetrieveFromCache(true)
                    .load(item.url)
                    .into(binding.fetchImage);

            binding.Title.setBackgroundColor(item.color);
            binding.Title.setText(item.label);

            Log.d("Abhi", "onResourceReady: " + item.label);

            b.linearLayout.addView(binding.getRoot());

            setupContextMenu(binding, b.linearLayout.getChildCount() - 2);




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


    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.edit:
                editImage();
                return true;
            case R.id.delete:
                deleteImage();
                return true;
        }
        return super.onContextItemSelected(item);
    }

    /**
     * Set context menu
     *
     * @param binding  Reference of ItemCardBinding
     * @param position LinearLayout child position
     */
    private void setupContextMenu(ItemCardBinding binding, int position) {

        binding.cardView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
            @Override
            public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                v.getId();
                Log.d("Abhi", "onCreateContextMenu: ");


                getMenuInflater().inflate(R.menu.context_menu, menu);
                selectedPosition = position;

            }
        });
    }


    /**
     * Delete Image
     */
    private void deleteImage() {
        Log.d("Abhi", "deleteImage: ");

        b.linearLayout.getChildAt(selectedPosition + 1).setVisibility(View.GONE);

        if (removeItem == null) {
            removeItem = new ArrayList<>();
        }

        removeItem.add(itemList.get(selectedPosition));

        //check all child are Gone
        int count = 0;
        for (int i = 0; i < b.linearLayout.getChildCount(); i++) {
            if (b.linearLayout.getChildAt(i).getVisibility() == View.GONE) {
                Log.d("Abhi", "deleteImage: " + b.linearLayout.getChildAt(i).getVisibility());
                count++;
            }
        }
        //show heading
        if (count == b.linearLayout.getChildCount()) {
            b.heading.setVisibility(View.VISIBLE);
        }

    }

    /**
     * Edit Image
     */
    private void editImage() {
        new AddImageDialog().editFetchImage(this, itemList.get(selectedPosition), new AddImageDialog.OnCompleteListener() {
            @Override
            public void onImageAdd(Item item) {
                TextView textView = b.linearLayout.getChildAt(selectedPosition + 1).findViewById(R.id.Title);
                textView.setText(item.label);
                textView.setBackgroundColor(item.color);
                itemList.set(selectedPosition, new Item(item.color, item.label, item.url));
                isEdited = true;
            }

            @Override
            public void onError(String error) {


            }
        });
    }


    /**
     * To show the dialog to add image
     */
    private void showAddImageDialog() {

        new AddImageDialog()
                .showDialog(this, new AddImageDialog.OnCompleteListener() {
                    @Override
                    public void onImageAdd(Item item) {
                        inflateViewForItem(item);
                    }

                    @Override
                    public void onError(String error) {
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

        b.heading.setVisibility(View.GONE);
        //Inflate layout
        ItemCardBinding binding = ItemCardBinding.inflate(getLayoutInflater());

        //Bind data
        binding.fetchImage.setImageBitmap(item.image);
        binding.Title.setBackgroundColor(item.color);
        binding.Title.setText(item.label);


        b.linearLayout.addView(binding.getRoot());

        //Add Item
        Item newItem = new Item(item.color, item.label, item.url);

        if (itemList == null) {
            itemList = new ArrayList<>();
        }

        itemList.add(newItem);
        isAdd = true;

        setupContextMenu(binding, b.linearLayout.getChildCount() - 2);
    }


    @Override
    protected void onPause() {
        super.onPause();

        //Remove Item and save
        if (removeItem != null) {
            itemList.removeAll(removeItem);

            Gson gson = new Gson();
            String json = gson.toJson(itemList);

            getPreferences(MODE_PRIVATE).edit().putString("ITEMS", json).apply();

            finish();
        }

        //save in SharedPreference
        if (itemList != null && (isEdited || isAdd)) {
            Gson gson = new Gson();
            String json = gson.toJson(itemList);
            getPreferences(MODE_PRIVATE).edit().putString("ITEMS", json).apply();
            isAdd = false;
            isEdited = false;
        }

    }


}