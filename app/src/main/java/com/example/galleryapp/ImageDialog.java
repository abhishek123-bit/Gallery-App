package com.example.galleryapp;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.example.galleryapp.databinding.ChipColorBinding;
import com.example.galleryapp.databinding.ChipLabelBinding;
import com.example.galleryapp.databinding.DialogAddImageBinding;
import com.example.galleryapp.models.Item;
import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;
import java.util.Set;

public class ImageDialog implements ItemHelper.OnCompleteListener {

    private Context context;
    private OnCompleteListener listener;
    private LayoutInflater inflater;
    private DialogAddImageBinding b;
    private boolean isCustomLabel;
    private Bitmap image;
    private AlertDialog dialog;
    private String url;
    private Item item;
    private boolean isAlreadyChecked;

    /**
     * @param context  Activity state
     * @param listener Complete listener handler
     */
    public void showDialog(Context context, OnCompleteListener listener) {
        if (!initializeDialog(context, listener))
            return;

        //Handle dimensions
        handelDimensions();

        //Handle cancel event
        handelCancelButton();


    }

    /**
     * Edit Image which is come from caches
     *
     * @param context  Activity state
     * @param item     {@link Item }
     * @param listener Complete listener handler
     */

    public void editFetchImage(Context context, Item item, OnCompleteListener listener) {
        this.url = item.url;
        this.item = item;
        if (!initializeDialog(context, listener))
            return;

        b.DialogTitle.setText("Edit image");
        b.btnAdd.setText("Edit");
        b.loadingText.setText("Please wait...");

        editImage(url);

        //Handle cancel event
        handelCancelButton();
    }

    /**
     * Check Dialog Initialize or not
     *
     * @param context  Activity state
     * @param listener Complete listener handler
     */
    private boolean initializeDialog(Context context, OnCompleteListener listener) {
        this.context = context;
        this.listener = listener;

        //Check context is GalleryActivity or not
        if (context instanceof GalleryActivity) {
            //Initialize inflater
            inflater = ((GalleryActivity) context).getLayoutInflater();

            //Initialize binding
            b = DialogAddImageBinding.inflate(inflater);

        } else {
            dialog.dismiss();
            //call listener
            listener.onError("Cast Exception");
            return false;
        }

        //Show AlertDialog
        dialog = new MaterialAlertDialogBuilder(context, R.style.CustomDialogTheme)
                .setCancelable(false)
                .setView(b.getRoot())
                .show();
        return true;
    }


    /**
     * @param context  Activity State
     * @param path     Image Path
     * @param listener Complete event handler
     */
    public void fetchImageFromFiles(Context context, String path, OnCompleteListener listener) {
        if (!initializeDialog(context, listener))
            return;

        this.context = context;
        this.listener = listener;
        b.inputDimensionsRoot.setVisibility(View.GONE);
        b.progressIndiacatorRoot.setVisibility(View.VISIBLE);
        b.loadingText.setText("Please wait...");

        new ItemHelper().fetchImageFromGallery(path, context, this);

    }


    /**
     * Edit Image
     *
     * @param url Image Url
     */
    private void editImage(String url) {
        b.inputDimensionsRoot.setVisibility(View.GONE);
        b.progressIndiacatorRoot.setVisibility(View.VISIBLE);

        new ItemHelper().editImage(url, context, this);
    }

    /**
     * Handle cancel button
     */
    private void handelCancelButton() {
        //click event on Cancel button
        b.btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    /**
     * Handle height and width of image
     */
    private void handelDimensions() {
        //click event on FetchImage button
        b.btnFetchImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Get height and width
                String height = b.height.getText().toString().trim(),
                        width = b.width.getText().toString().trim();

                //Check height and width of image
                if (height.isEmpty() && width.isEmpty()) {
                    b.height.setError("Please enter at least one dimension");
                    return;
                }

                b.inputDimensionsRoot.setVisibility(View.GONE);
                b.progressIndiacatorRoot.setVisibility(View.VISIBLE);


                if (width.isEmpty()) {
                    fetchImage(Integer.parseInt(height));
                } else if (height.isEmpty()) {
                    fetchImage(Integer.parseInt(width));
                } else {
                    fetchImage(Integer.parseInt(width), Integer.parseInt(height));
                }

                //hide keyboard
                hideKeyboard();
            }
        });

    }

    /**
     * Hide keyBoard when add button click
     */
    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);

        imm.hideSoftInputFromWindow(b.btnAdd.getWindowToken(), 0);
    }

    /**
     * Fetch rectangular Image
     *
     * @param height Height of Image
     * @param width  Width of Image
     */
    private void fetchImage(int width, int height) {
        //call fetchData function of ItemHelper class
        new ItemHelper().
                fetchData(width, height, context, this);
    }

    /**
     * Fetch square Image
     *
     * @param x Height and Width of image
     */
    private void fetchImage(int x) {
        //call fetchData function of ItemHelper class
        new ItemHelper().fetchData(x, context, this);
    }


    /**
     * Call when image all data fetch completely
     *
     * @param bitmap       Store Image
     * @param colorPalette Store Image colors
     * @param labels       Store Image labels
     */
    @Override
    public void onFetch(Bitmap bitmap, Set<Integer> colorPalette, List<String> labels, String url) {
        //call function
        this.url = url;
        showData(bitmap, colorPalette, labels);
    }

    @Override
    public void onError(String exception) {
        dialog.dismiss();
        listener.onError(exception);
    }

    /**
     * Update views
     *
     * @param bitmap       Store Image
     * @param colorPalette Store Image colors
     * @param labels       Store Image labels
     */
    private void showData(Bitmap bitmap, Set<Integer> colorPalette, List<String> labels) {
        this.image = bitmap;
        b.fetchImage.setImageBitmap(bitmap);

        //Inflate colorChip layout
        inflatePaletteChips(colorPalette);

        //Inflate labelChip layout
        inflateLabelChips(labels);

        //update views
        b.progressIndiacatorRoot.setVisibility(View.GONE);
        b.mainRoot.setVisibility(View.VISIBLE);
        b.customInputLabel.setVisibility(View.GONE);
        b.btnCancel.setVisibility(View.VISIBLE);

        //Handel Custom label
        handelCustomLabel();


        //Handel add Button
        handelAddImageEvent();
    }

    /**
     * Add palette in ImageDialog
     *
     * @param colors Image colors
     */
    private void inflatePaletteChips(Set<Integer> colors) {
        for (Integer color : colors) {
            ChipColorBinding binding = ChipColorBinding.inflate(inflater);
            binding.getRoot().setChipBackgroundColor(ColorStateList.valueOf(color));
            this.b.chipPaletteGroup.addView(binding.getRoot());


            //Edit Image
            //select chip if color present
            if (item != null && item.color == color) {
                binding.getRoot().setChecked(true);
                Log.d("Abhi", "inflatePaletteChips: ");
            }
        }
    }

    /**
     * Add labels in ImageDialog
     *
     * @param labels Labels of image
     */
    private void inflateLabelChips(List<String> labels) {
        for (String label : labels) {
            ChipLabelBinding binding = ChipLabelBinding.inflate(inflater);

            binding.getRoot().setText(label);
            this.b.chipLabelGroup.addView(binding.getRoot());

            //Edit Image
            //Select chip if label present
            if (item != null && item.label.equals(label)) {
                Log.d("Abhi", "inflateLabelChips: ");
                binding.getRoot().setChecked(true);
                isAlreadyChecked = true;
            }
        }
    }

    /**
     * Handle custom label
     */
    private void handelCustomLabel() {
        ChipLabelBinding binding = ChipLabelBinding.inflate(inflater);
        binding.getRoot().setText("Custom");
        b.chipLabelGroup.addView(binding.getRoot());

        //Edit Image
        //Check  chip label already selected or not
        if (item != null && !isAlreadyChecked) {
            binding.getRoot().setChecked(true);
            b.customInputLabel.setVisibility(View.VISIBLE);
            b.customLabel.setText(item.label);
            isCustomLabel = true;
        }

        binding.getRoot().setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                b.customInputLabel.setVisibility(isChecked ? View.VISIBLE : View.GONE);
                isCustomLabel = isChecked;
            }
        });
    }

    /**
     * Handle AddImage button
     */
    private void handelAddImageEvent() {
        //click event on Add Butoon
        b.btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int chipLabelId = b.chipLabelGroup.getCheckedChipId(),
                        chipPaletteId = b.chipPaletteGroup.getCheckedChipId();

                //check label and palette is selected or not
                if (chipLabelId == -1 || chipPaletteId == -1) {
                    Toast.makeText(context, "Please select color & label", Toast.LENGTH_SHORT).show();
                    return;
                }

                //Get color & label
                String label;
                if (isCustomLabel) {
                    label = b.customLabel.getText().toString().trim();
                    if (label.isEmpty()) {
                        Toast.makeText(context, "Please enter custom label", Toast.LENGTH_SHORT).show();
                        return;
                    }
                } else {
                    label = ((Chip) b.chipLabelGroup.findViewById(chipLabelId)).getText().toString();
                }


                int color = ((Chip) b.chipPaletteGroup.findViewById(chipPaletteId)).getChipBackgroundColor().getDefaultColor();

                //Send callback
                listener.onImageAdd(new Item(image, color, label, url));

                dialog.dismiss();
            }
        });
    }

    /**
     * Interface call when a image is fetch or give some error.
     */
    public interface OnCompleteListener {
        /**
         * @param item {@link Item}
         */
        void onImageAdd(Item item);

        void onError(String error);
    }

}

