package com.example.galleryapp;

import androidx.annotation.NonNull;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;


import com.example.galleryapp.adapters.ImageAdapter;
import com.example.galleryapp.databinding.ActivityGalleryBinding;
import com.example.galleryapp.models.Item;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class GalleryActivity extends AppCompatActivity {
    private static final int REQUEST_READ_EXTERNAL_STORAGE = 32;
    ActivityGalleryBinding b;
    List<Item> itemList = new ArrayList<>();
    private boolean isEdited;
    private boolean isAdd;
    public ImageAdapter adapter;
    public boolean isEnable;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityGalleryBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());


        //Load data from sharedPreferences
        loadSharedPreferenceData();

        //set Adapter
        setupAdapter();

        //Set sw
        setupSwipeAndDragOption();

    }

    private void setupAdapter() {
        adapter = new ImageAdapter(this, itemList);
        b.RecycleView.setLayoutManager(new LinearLayoutManager(this));
        b.RecycleView.setAdapter(adapter);
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
//        for (Item item : itemList) {
//            ItemCardBinding binding = ItemCardBinding.inflate(getLayoutInflater());
//
//            Glide.with(this)
//                    .asBitmap()
//                    .load(item.url)
//                    .into(binding.fetchImage);
//
//            binding.Title.setBackgroundColor(item.color);
//            binding.Title.setText(item.label);
//
//            Log.d("Abhi", "onResourceReady: " + item.label);
//
////            b.linearLayout.addView(binding.getRoot());
////            setupContextMenu(binding, b.linearLayout.getChildCount() - 1);
//
//
//        }


    }

    /**
     * Inflate menu
     * @param menu items on action bar
     */
    private void createMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gallery, menu);

        //Search
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();

        searchView.setQueryHint("Search");

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter.filter(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.filter(newText);
                return true;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.downloadPhoto) {
            showAddImageDialog();
            return true;
        } else if (item.getItemId() == R.id.addPhoto) {
            addPhotoFromFile();
            return true;
        } else if (item.getItemId() == R.id.sort) {
            sortData();
            return true;
        } else if (item.getItemId() == R.id.Drag) {
            isEnable = true;
            invalidateOptionsMenu();
            return true;
        } else if(item.getItemId()==R.id.Select){
            isEnable = false;
            invalidateOptionsMenu();
            return true;
        }

        return false;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (isEnable) {
            menu.clear();
            getMenuInflater().inflate(R.menu.drag_and_drop,menu);
            getSupportActionBar().setTitle("Drag&Drop");
        } else {
            menu.clear();
            getSupportActionBar().setTitle(R.string.app_name);
            createMenu(menu);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    /**
     * Sort Data Alphabetically
     */
    private void sortData() {
        Collections.sort(adapter.visibleItems, new Comparator<Item>() {

            @Override
            public int compare(Item o1, Item o2) {
                return o1.label.toLowerCase().compareTo(o2.label.toLowerCase());
            }
        });

        adapter.notifyDataSetChanged();
    }

    /**
     * Add Image from Gallery
     */
    private void addPhotoFromFile() {
        if (!checkPermission()) {
            return;
        }
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .start(this);

    }


    /**
     * Check Storage permission
     *
     * @return Permission Granted or not
     */
    private boolean checkPermission() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, REQUEST_READ_EXTERNAL_STORAGE);

            return false;

        }

        return true;
    }


    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.edit:
                editImage();
                return true;
            case R.id.share:
                shareImage();
                return true;

        }
        return super.onContextItemSelected(item);
    }


    /**
     * Share Image
     */
    private void shareImage() {

        //Take screenShort of cardView
        adapter.view.setDrawingCacheEnabled(true);
        Bitmap bitmap = Bitmap.createBitmap(adapter.view.getDrawingCache());
        adapter.view.setDrawingCacheEnabled(false);

        //Path of image
        Uri uri = getImageToShare(bitmap);
        Intent intent = new Intent(Intent.ACTION_SEND);

        // putting uri of image to be shared
        intent.putExtra(Intent.EXTRA_STREAM, uri);

        // setting type to image
        intent.setType("image/jpeg");

        // calling startactivity() to share
        startActivity(Intent.createChooser(intent, "Share Via"));

    }


    /**
     * Edit Image
     */
    private void editImage() {
        new ImageDialog().editFetchImage(this, adapter.visibleItems.get(adapter.selectedPosition), new ImageDialog.OnCompleteListener() {
            @Override
            public void onImageAdd(Item item) {
                adapter.visibleItems.set(adapter.selectedPosition, new Item(item.color, item.label, item.url));

                for (int i = 0; i < itemList.size(); i++) {
                    if (itemList.get(i).url.equals(item.url)) {
                        itemList.set(i, new Item(item.color, item.label, item.url));
                        break;
                    }
                }


                adapter.notifyItemChanged(adapter.selectedPosition);
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

        new ImageDialog()
                .showDialog(this, new ImageDialog.OnCompleteListener() {
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
        //Add Item
        Item newItem = new Item(item.color, item.label, item.url);


        itemList.add(newItem);
        adapter.visibleItems.add(newItem);
        isAdd = true;

        adapter.notifyItemInserted(itemList.size() - 1);

    }

    /**
     * Add swipe and DragOption on RecycleView
     */
    public void setupSwipeAndDragOption() {


        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            //Move data
            @Override
            public boolean onMove(@NotNull RecyclerView recyclerView, RecyclerView.@NotNull ViewHolder viewHolder, RecyclerView.@NotNull ViewHolder target) {

                Collections.swap(adapter.visibleItems, viewHolder.getAbsoluteAdapterPosition(), target.getAbsoluteAdapterPosition());

                // and notify the adapter that its dataset has changed
                adapter.notifyItemMoved(viewHolder.getAbsoluteAdapterPosition(), target.getAbsoluteAdapterPosition());

                int originalPosition = 0, targetPosition = 0;

                for (int i = 0; i < itemList.size(); i++) {
                    if (itemList.get(i).url.equals(adapter.visibleItems.get(viewHolder.getAbsoluteAdapterPosition()).url)) {
                        originalPosition = i;
                    } else if (itemList.get(i).url.equals(adapter.visibleItems.get(target.getAbsoluteAdapterPosition()).url)) {
                        targetPosition = i;
                    }

                }

                Collections.swap(itemList, originalPosition, targetPosition);

                isEdited = true;

                return true;
            }

            //Delete data
            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {

                //Remove swiped item from list and notify the RecyclerView
                int position = viewHolder.getAbsoluteAdapterPosition();
                itemList.remove(adapter.visibleItems.get(position));
                adapter.visibleItems.remove(position);
                adapter.notifyItemRemoved(position);
                isEdited = true;
                if (itemList.size() == 0) {
                    b.heading.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public boolean isLongPressDragEnabled() {
                return isEnable;
            }
        };

        //Implement swipe and drag option
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(b.RecycleView);

    }

    @Override
    protected void onPause() {
        super.onPause();

        //save in SharedPreference
        if (isEdited || isAdd) {
            Gson gson = new Gson();
            String json = gson.toJson(itemList);
            getPreferences(MODE_PRIVATE).edit().putString("ITEMS", json).apply();
            isAdd = false;
            isEdited = false;
        }

    }

    /**
     * Retrieving the url to share
     *
     * @param bitmap Image
     * @return Uri of file
     */
    private Uri getImageToShare(Bitmap bitmap) {
        File imagefolder = new File(getCacheDir(), "images");
        imagefolder.mkdirs();
        Uri uri = null;
        try {
            File file = new File(imagefolder, "shared_image.png");
            FileOutputStream outputStream = new FileOutputStream(file);

            Log.d("Abhi", "getImageToShare: " + file.getPath());
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream);
            outputStream.flush();
            outputStream.close();
            uri = FileProvider.getUriForFile(this, "com.anni.shareimage.fileprovider", file);
        } catch (Exception e) {
            Log.d("Abhi", "getImageToShare: " + e.getMessage());
        }
        return uri;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            Uri resultUri = result.getUri();

            Log.d("Abhi", "onActivityResult: " + resultUri.getPath());

            new ImageDialog().fetchImageFromFiles(this, resultUri.getPath(), new ImageDialog.OnCompleteListener() {
                @Override
                public void onImageAdd(Item item) {
                    inflateViewForItem(item);
                }

                @Override
                public void onError(String error) {

                }
            });

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_READ_EXTERNAL_STORAGE) {
            // If request is cancelled, the result arrays are empty.
            Log.d("Abhi", "onRequestPermissionsResult: " + grantResults.length);
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(this);
            }
        }
    }


}