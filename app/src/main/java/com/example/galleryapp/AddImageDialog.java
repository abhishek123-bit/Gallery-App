package com.example.galleryapp;

import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
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

public class AddImageDialog implements ItemHelper.OnCompleteListener {

    private Context context;
    private OnCompleteListener listener;
    private LayoutInflater inflater;
    private DialogAddImageBinding b;
    private boolean isCustomLabel;
    private Bitmap image;
    private AlertDialog dialog;

    /**
     * @param context  Activity state
     * @param listener Complete listener handler
     */
    public void showDialog(Context context, OnCompleteListener listener) {
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
            return;
        }

        //Show AlertDialog
        dialog = new MaterialAlertDialogBuilder(context, R.style.CustomDialogTheme)
                .setCancelable(false)
                .setView(b.getRoot())
                .show();

        //Handle dimensions
        handelDimensions();

        //Handle cancel event
        handelCancelButton();


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
                //Get height and idth
                String height = b.height.getText().toString().trim(),
                        width = b.width.getText().toString().trim();

                //Check height and image
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
                    fetchImage(Integer.parseInt(height), Integer.parseInt(width));
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
    private void fetchImage(int height, int width) {
        //call fetchData function of ItemHelper class
        new ItemHelper().
                fetchData(height, width, context, this);
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

    private void handelAddImageEvent() {
        b.btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int chipLabelId = b.chipLabelGroup.getCheckedChipId(),
                        chipPaletteId = b.chipPaletteGroup.getCheckedChipId();

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
                listener.onImageAdd(new Item(image, color, label));

                dialog.dismiss();
            }
        });
    }

    /**
     * Call when image all data fetch completely
     *
     * @param bitmap       Store Image
     * @param colorPalette Store Image colors
     * @param labels       Store Image labels
     */
    @Override
    public void onFetch(Bitmap bitmap, Set<Integer> colorPalette, List<String> labels) {
        //call function
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

        //Handel Custom label
        handelCustomLabel();

        //update views
        b.progressIndiacatorRoot.setVisibility(View.GONE);
        b.mainRoot.setVisibility(View.VISIBLE);
        b.customInputLabel.setVisibility(View.GONE);
        b.btnCancel.setVisibility(View.VISIBLE);

        //Handel add Button
        handelAddImageEvent();
    }

    /**
     * Add palette in AddImageDialog
     *
     * @param colors Image colors
     */
    private void inflatePaletteChips(Set<Integer> colors) {
        for (Integer color : colors) {
            ChipColorBinding binding = ChipColorBinding.inflate(inflater);
            binding.getRoot().setChipBackgroundColor(ColorStateList.valueOf(color));
            this.b.chipPaletteGroup.addView(binding.getRoot());
        }
    }

    /**
     * Add labels in AddImageDialog
     *
     * @param labels Labels of image
     */
    private void inflateLabelChips(List<String> labels) {
        for (String label : labels) {
            ChipLabelBinding binding = ChipLabelBinding.inflate(inflater);
            binding.getRoot().setText(label);
            this.b.chipLabelGroup.addView(binding.getRoot());
        }
    }

    /**
     * Handle custom label
     */
    private void handelCustomLabel() {
        ChipLabelBinding binding = ChipLabelBinding.inflate(inflater);
        binding.getRoot().setText("Custom");
        b.chipLabelGroup.addView(binding.getRoot());
        binding.getRoot().setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                b.customInputLabel.setVisibility(isChecked ? View.VISIBLE : View.GONE);
                isCustomLabel = isChecked;
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

