package com.example.galleryapp.adapters;

import android.content.Context;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;


import com.bumptech.glide.Glide;
import com.example.galleryapp.GalleryActivity;
import com.example.galleryapp.R;
import com.example.galleryapp.databinding.ItemCardBinding;
import com.example.galleryapp.models.Item;

import java.util.ArrayList;
import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageHolder> {

    private final Context context;

    private List<Item> items;

    public List<Item> visibleItems;

    public int selectedPosition;

    public CardView view;

    /**
     *
     * @param context Activity state
     * @param items list of items
     */
    public ImageAdapter(Context context, List<Item> items) {

        this.context = context;
        this.items = items;
        this.visibleItems = new ArrayList<>(items);
    }

    @NonNull
    @Override
    public ImageHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCardBinding itemCardBinding = ItemCardBinding.inflate(LayoutInflater.from(context), parent, false);

        return new ImageHolder(itemCardBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageHolder holder, int position) {
        Item item = visibleItems.get(position);

        Glide.with(context)
                .asBitmap()
                .load(item.url)
                .into(holder.b.fetchImage);


        holder.b.Title.setBackgroundColor(item.color);
        holder.b.Title.setText(item.label);

        setupContextualMenu(holder);

    }

    /**
     *
     * @param holder it holds the views
     */
    private void setupContextualMenu(ImageHolder holder) {
        holder.b.cardView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
            @Override
            public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {

                if(((GalleryActivity)context).isEnable)
                    return;

                ((GalleryActivity) context).getMenuInflater().inflate(R.menu.context_menu, menu);
                Log.d("Abhi", "onCreateContextMenu: " + holder.getAbsoluteAdapterPosition());
                selectedPosition = holder.getAbsoluteAdapterPosition();
                view = holder.b.cardView;}

        });
    }

    @Override
    public int getItemCount() {

        return visibleItems.size();
    }


    /**
     * Filter recycleView
     * @param query Search string
     */
    public void filter(String query) {

        visibleItems.clear();

        if (query.trim().isEmpty()) {
            visibleItems.addAll(items);
        } else {
            for (Item item : items) {
                if (item.label.toLowerCase().contains(query.toLowerCase())) {
                    visibleItems.add(item);
                }
            }
        }

        if(visibleItems.size()==0){
            ((GalleryActivity)context).findViewById(R.id.heading).setVisibility(View.VISIBLE);
        }
        else{

            ((GalleryActivity)context).findViewById(R.id.heading).setVisibility(View.GONE);
        }

        notifyDataSetChanged();

    }


    static class ImageHolder extends RecyclerView.ViewHolder {
        ItemCardBinding b;

        public ImageHolder(@NonNull ItemCardBinding b) {
            super(b.getRoot());

            this.b = b;


        }
    }
}
