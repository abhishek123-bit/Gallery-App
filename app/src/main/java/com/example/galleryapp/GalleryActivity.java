package com.example.galleryapp;

import androidx.annotation.NonNull;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.palette.graphics.Palette;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.galleryapp.databinding.ActivityGalleryBinding;
import com.example.galleryapp.databinding.DialogAddImageBinding;
import com.example.galleryapp.databinding.ItemCardBinding;
import com.example.galleryapp.models.Item;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.label.ImageLabel;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;


public class GalleryActivity extends AppCompatActivity {
    ActivityGalleryBinding b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityGalleryBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());


        String path = getCacheDir() + "/" + "image_manager_disk_cache" + "/";


        File[] files = new File(path).listFiles();


        for (File file1 : files) {
            try {
                if (file1.getName().equals("journal")) {
                    continue;
                }
                Log.d("Abhi", "onCreate: "+file1);
                Bitmap bitmap = BitmapFactory.decodeStream(new FileInputStream(file1));
                ItemCardBinding binding = ItemCardBinding.inflate(getLayoutInflater());

                //Bind data
                binding.fetchImage.setImageBitmap(bitmap);
                b.linearLayout.addView(binding.getRoot());

            } catch (FileNotFoundException e) {
                Log.d("Abhi", "onCreate: ");
                e.printStackTrace();
            }
        }

    }

//Testing
//    private void loadImages() {
//
//        DialogAddImageBinding binding=DialogAddImageBinding.inflate(getLayoutInflater());
//        new MaterialAlertDialogBuilder(this, R.style.CustomDialogTheme)
//                .setCancelable(false)
//                .setView(binding.getRoot())
//                .show();
//        Glide.with(this)
//                .asBitmap()
//                .load("https://picsum.photos/1080")
//                .skipMemoryCache(true)
//                .diskCacheStrategy(DiskCacheStrategy.NONE)
//                .listener(new RequestListener<Bitmap>() {
//                    @Override
//                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
////                        b.loader.setVisibility(View.GONE);
////                        b.title.setText(getString(R.string.loading_failed, e.toString()));
//                        return true;
//                    }
//
//                    @Override
//                    public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
////                        b.loader.setVisibility(View.GONE);
////                        b.title.setText(R.string.load_successfully);
//                        binding.fetchImage.setImageBitmap(resource);
////                        labelImage(resource );
//
////                        createPaletteAsync(resource);
//                        return true;
//                    }
//                })
//
//                .into(binding.fetchImage);
//    }
//
//    private void labelImage(Bitmap resource) {
//        InputImage inputImage = InputImage.fromBitmap(resource, 0);
//        ImageLabeler labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS);
//
//        labeler.process(inputImage)
//                .addOnSuccessListener(new OnSuccessListener<List<ImageLabel>>() {
//                    @Override
//                    public void onSuccess(List<ImageLabel> labels) {
//                        new MaterialAlertDialogBuilder(GalleryActivity.this)
//                                .setTitle("label fetch")
//                                .setMessage(labels.toString())
//                                .show();
//                    }
//                })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        new MaterialAlertDialogBuilder(GalleryActivity.this, R.style.CustomDialogTheme)
//                                .setTitle("Error")
//                                .setMessage(e.toString())
//                                .show();
//                    }
//                });
//    }
//
//    public void createPaletteAsync(Bitmap bitmap) {
//        Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
//            public void onGenerated(Palette p) {
//                new MaterialAlertDialogBuilder(GalleryActivity.this, R.style.CustomDialogTheme)
//                        .setTitle("Palette")
//                        .setMessage(p.getSwatches().toString())
//                        .show();
//            }
//        });
//    }


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
    }

}