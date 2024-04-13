package com.example.spotifywrappedapplication;

import android.graphics.Color;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PlaceholderAdapter extends RecyclerView.Adapter<PlaceholderAdapter.ViewHolder> {

    private final List<FragmentData> fragmentDataList;
    private final OnItemClicked listener;

    public interface OnItemClicked {
        void onItemClick(FragmentData fragmentData);
    }

    public PlaceholderAdapter(List<FragmentData> fragmentDataList, OnItemClicked listener) {
        this.fragmentDataList = fragmentDataList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_blank, parent, false);

        // Getting screen height to calculate the height of each item
        DisplayMetrics displayMetrics = parent.getContext().getResources().getDisplayMetrics();
        int screenHeight = displayMetrics.heightPixels;

        // Setting the height of the view to be 1/8th of the screen's height
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = screenHeight / 8;
        view.setLayoutParams(layoutParams);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Retrieve the FragmentData object for the current position
        FragmentData fragmentData = fragmentDataList.get(position);

        // Set the background color of the itemView
        String color = fragmentData.getColor();
        holder.itemView.setBackgroundColor(Color.parseColor(color)); // Make sure the color string is correctly formatted (e.g., "#RRGGBB")

        // Set the text for the TextView
        String name = fragmentData.getName();
        holder.textView.setText(name); // Assuming getName() returns the text you want to display

        // Set an OnClickListener for the itemView
        holder.itemView.setOnClickListener(v -> listener.onItemClick(fragmentData));
    }

    public void updateData(FragmentData newData) {
        fragmentDataList.add(newData);
        notifyDataSetChanged(); // Refresh entire list
    }

    @Override
    public int getItemCount() {
        return fragmentDataList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView textView; // TextView for displaying the name.
        public View itemView; // This is already part of the superclass, included here for clarity.

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.textView = itemView.findViewById(R.id.text_view_id); // Make sure you have a TextView with an id in your item layout.
            this.itemView = itemView; // The itemView is the whole item view passed to the ViewHolder.
        }

    }
}
