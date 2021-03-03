package com.example.angelmendez.cityreport;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Random;

public class ListAdapter extends RecyclerView.Adapter<RecyclerViewHolder> {

    int reportCount;
    ArrayList<ReportObject> dataSet;
    Context context;
    RecyclerView recyclerView;
    MainActivity mainActivity;

    public ListAdapter(ArrayList<ReportObject> dataSet, Context context, RecyclerView recyclerView, MainActivity mainActivity) {

        reportCount = dataSet.size();
        this.dataSet = dataSet;
        this.context = context;
        this.recyclerView = recyclerView;
        this.mainActivity = mainActivity;
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

        final View view = LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position  = recyclerView.getChildLayoutPosition(view);
                mainActivity.updateReport(dataSet.get(position));
            }
        });
        view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                int position  = recyclerView.getChildLayoutPosition(view);
                return mainActivity.deleteReport(dataSet.get(position));

            }
        });
        return new RecyclerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerViewHolder holder, final int position) {


        holder.nearStreetText.setText("Near " + dataSet.get(position).getNearStreet());
        holder.dateText.setText("On " + dataSet.get(position).getDate());

        if (dataSet.get(position).getPhotoArray() == null)
        {
            String uri = "@drawable/no_image_available";
            int imageResource = context.getResources().getIdentifier(uri, null, context.getPackageName());
            Drawable noPhotoAvailble = context.getResources().getDrawable(imageResource);
            holder.photo.setImageDrawable(noPhotoAvailble);
        }
        else
        {
            Bitmap bitmap = dataSet.get(position).getPhotoArray().get(0);
            holder.photo.setImageBitmap(Bitmap.createScaledBitmap(bitmap, bitmap.getWidth(), bitmap.getHeight(), false));
        }

        holder.shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                share(position, v);
            }
        });

        if (position == dataSet.size() - 1)
        {
            //holder.view.setBottom(30);
            setMargins( holder.view, 0, 20, 0, 20);
        }
        //holder.photo.setImageBitmap(Bitmap.createScaledBitmap(bitmap, 120, 120, false));


    }

    public static void setMargins (View v, int l, int t, int r, int b) {
        if (v.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            p.setMargins(l, t, r, b);
            v.requestLayout();
        }
    }

    private void share(int position, View v)
    {
        // photos
        ArrayList<Uri> imageUris = null;
        String path = v.getContext().getFilesDir().getAbsolutePath() + File.separator + "ReportsDir" + File.separator + "ReportPhotos" + File.separator + dataSet.get(position).getPhotoDirectoryName();
        File photoDir  = new File(path);
        File[] photoFiles = photoDir.listFiles();

        // location
        String uri = "http://maps.google.com/maps?saddr=" + dataSet.get(position).getLocation().latitude +","+ dataSet.get(position).getLocation().longitude;

        // category
       // String category = "Report of type: " + dataSet.get(position).getCategory() + " in this location";
        String category = dataSet.get(position).getCategory();

        //description
        String description = dataSet.get(position).getDescription();
        String descriptionMessage = (description == "" ? "" : ( "Description: \n" + description));

        String message = "Hey, I wanted to make you aware of this issue: " + "\n\n" + "Report Type: " + category + "\n\n" + descriptionMessage + "\n\n" + "Location: \n" + uri;
        Intent shareIntent;

        if (photoFiles != null)
        {
            imageUris = new ArrayList<Uri>();

            for (int i = 0; i < photoFiles.length; i++)
            {
                imageUris.add(FileProvider.getUriForFile(v.getContext(), "com.mydomain.fileprovider", photoFiles[i]));
            }

            shareIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
            shareIntent.setType("image/jpeg");
            shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, imageUris);
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "I wanted to share this with you!");
            shareIntent.putExtra(Intent.EXTRA_TEXT, message);
        }
        else
        {
            shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/html");
            shareIntent.putExtra(Intent.EXTRA_TEXT, message);
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "I wanted to share this with you!");
        }

        v.getContext().startActivity(Intent.createChooser(shareIntent, "Share via"));

    }

    @Override
    public int getItemCount() {
        return reportCount;
    }
}
