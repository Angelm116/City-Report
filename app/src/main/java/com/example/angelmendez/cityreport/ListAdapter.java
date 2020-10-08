package com.example.angelmendez.cityreport;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Random;

public class ListAdapter extends RecyclerView.Adapter<RecyclerViewHolder> {

    int reportCount;
    ArrayList<ReportObject> dataSet;

    public ListAdapter(ArrayList<ReportObject> dataSet) {
        reportCount = dataSet.size();
        this.dataSet = dataSet;
    }

    public void setData( ArrayList<ReportObject> data)
    {
        reportCount = data.size();
        this.dataSet = data;
    }

    @Override
    public int getItemViewType(final int position) {
        return R.layout.report_card;
    }

    @NonNull
    @Override
    public RecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false);
        return new RecyclerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerViewHolder holder, final int position) {
        holder.nearStreetText.setText("Near " + dataSet.get(position).getNearStreet());
        holder.dateText.setText("On " + dataSet.get(position).getDate());

        Bitmap bitmap = dataSet.get(position).getPhoto();
        if (bitmap != null)
        {
            holder.photo.setImageBitmap(Bitmap.createScaledBitmap(bitmap, bitmap.getWidth(), bitmap.getHeight(), false));
        }

        holder.shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                share(position, v);
            }
        });

        //holder.photo.setImageBitmap(Bitmap.createScaledBitmap(bitmap, 120, 120, false));

    }

    private void share(int position, View v)
    {
        String uri = "http://maps.google.com/maps?saddr=" + dataSet.get(position).getLocation().latitude +","+ dataSet.get(position).getLocation().longitude;
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        String ShareSub = "Report of type: ";
        sharingIntent.putExtra(Intent.EXTRA_SUBJECT, ShareSub);
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, uri );
        v.getContext().startActivity(Intent.createChooser(sharingIntent, "Share via"));

    }

    @Override
    public int getItemCount() {
        return reportCount;
    }
}
