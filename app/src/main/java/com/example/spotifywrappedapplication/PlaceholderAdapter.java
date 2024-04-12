package com.example.spotifywrappedapplication;

import android.graphics.Color;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
        String color = fragmentDataList.get(position).getColor();
        holder.itemView.setBackgroundColor(Color.parseColor(color));
        holder.itemView.setOnClickListener(v -> listener.onItemClick(fragmentDataList.get(position)));
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
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
