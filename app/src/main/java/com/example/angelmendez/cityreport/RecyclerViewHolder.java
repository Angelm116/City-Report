package com.example.angelmendez.cityreport;

import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class RecyclerViewHolder extends RecyclerView.ViewHolder {

    public TextView nearStreetText;
    public TextView dateText;
    public ImageView photo;
    public ImageButton shareButton;
    public View view;

    public RecyclerViewHolder(@NonNull View itemView) {
        super(itemView);

        view = itemView;
        nearStreetText = itemView.findViewById(R.id.street_text);
        dateText = itemView.findViewById(R.id.date_text);
        photo = itemView.findViewById(R.id.imageView);
        shareButton = itemView.findViewById(R.id.shareButton);
    }


}


