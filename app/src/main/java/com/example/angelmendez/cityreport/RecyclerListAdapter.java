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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Random;

public class RecyclerListAdapter extends RecyclerView.Adapter<RecyclerViewHolder> {

    ArrayList<ReportObject> dataSet;
    Context context;
    RecyclerView recyclerView;
    MainActivity mainActivity;

    public RecyclerListAdapter(ArrayList<ReportObject> dataSet, Context context, RecyclerView recyclerView, MainActivity mainActivity) {

        this.dataSet = dataSet;
        this.context = context;
        this.recyclerView = recyclerView;
        this.mainActivity = mainActivity;
    }

    public void setData(ArrayList<ReportObject> data) {
        this.dataSet = data;
    }

    // returns the length of the dataset
    @Override
    public int getItemCount() {
        int size = dataSet == null ? 0 : dataSet.size();
        return size;
    }

    // returns the layout file that will be used for each item in the list
    @Override
    public int getItemViewType(final int position) {
        return R.layout.report_card;
    }

    // This function is called when the layout for a new item in the list is created, but not yet appended to the list
    @NonNull
    @Override
    public RecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        // inflate the layout for the new report
        final View view = LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false);

        // Set an onClickListener for the new report that triggers updateReport
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position  = recyclerView.getChildLayoutPosition(view);
                mainActivity.updateReport(dataSet.get(position));
            }
        });

        // Set an onLongClickListener for the new report that triggers deleteReport
        view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                int position  = recyclerView.getChildLayoutPosition(view);
                return mainActivity.deleteReport(dataSet.get(position));

            }
        });
        return new RecyclerViewHolder(view);
    }

    // This function is called when the new item is appended to the list
    @Override
    public void onBindViewHolder(@NonNull RecyclerViewHolder holder, final int position) {


        // Format date and time to be displayed in the report
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss aaa z");
        String dateTime = simpleDateFormat.format(dataSet.get(position).getDate().getTime());

        // Set date and near address test
        holder.dateText.setText("On " + dateTime);
        holder.nearStreetText.setText("Near " + dataSet.get(position).getLocationObject().getFormattedAddress());

        // TODO MAKE SURE THAT ITS SET TO NULL BEFORE SAVED SO THAT YOU ONLY HAVE TO CHECK FOR NULL HERE
        // Check if the report has pictures attached
        if (dataSet.get(position).getPhotoArray() != null && dataSet.get(position).getPhotoArray().size() > 0)
        {
            // Set display picture for the new report
            Bitmap bitmap = dataSet.get(position).getPhotoArray().get(0);
            holder.photo.setImageBitmap(Bitmap.createScaledBitmap(bitmap, bitmap.getWidth(), bitmap.getHeight(), false));
        }
        else
        {
            // Set the display picture to "not image available"
            int imageResource = context.getResources().getIdentifier( "@drawable/no_image_available", null, context.getPackageName());
            Drawable noPhotoAvailble = context.getResources().getDrawable(imageResource);
            holder.photo.setImageDrawable(noPhotoAvailble);
        }

        // Sets onClickListener for share button in the report
        holder.shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                share(position, v);
            }
        });

        // give the last element in the list ba bottom margin //TODO THIS MIGHT BE CAUSING PROBLEMS, WEIRD SPACES BETWEEN LIST ITEMS
        if (position == dataSet.size() - 1)
        {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) holder.view.getLayoutParams();
            p.setMargins(0, 20, 0, 20);
            holder.view.requestLayout();
        }


    }


    // This function allows users to share reports through other apps like Gmail or WhatsApp
    private void share(int position, View v)
    {

        Intent shareIntent;

        // URI for location of the report in Google Maps
        String mapsURI = "http://maps.google.com/maps?saddr=" +
                dataSet.get(position).getLocationObject().getLatitude() +","+
                dataSet.get(position).getLocationObject().getLongitude();

        // Report Category
        String category = dataSet.get(position).getCategory();

        // Report description
        String description = dataSet.get(position).getDescription();
        String descriptionMessage = (description == "" ? "" : ( "Description: \n" + description));

        // Message to be shared
        String message = "Hey, I wanted to make you aware of this issue: " + "\n\n" +
                "Report Type: " + category + "\n\n" +
                descriptionMessage + "\n\n" +
                "Location: \n" + mapsURI;


        // Get the files of the photos of this report
        final String PHOTO_DIR_PATH = v.getContext().getFilesDir().getAbsolutePath() +
                File.separator + "ReportsDir" + File.separator + "ReportPhotos" +
                File.separator + dataSet.get(position).getPhotoDirectoryName();
        File[] photoFiles = new File(PHOTO_DIR_PATH).listFiles();

        ArrayList<Uri> photoURIs;

        // Check if there are any photos attached
        if (photoFiles != null)
        {
            photoURIs = new ArrayList<Uri>();

            // Get URIs for every photo and store them in photoURIs
            for (int i = 0; i < photoFiles.length; i++)
            {
                photoURIs.add(FileProvider.getUriForFile(v.getContext(), "com.mydomain.fileprovider", photoFiles[i]));
            }

            // Create intent with images and message
            shareIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
            shareIntent.setType("image/jpeg");
            shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, photoURIs);              // photoURIs
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "I wanted to share this with you!"); // subject
            shareIntent.putExtra(Intent.EXTRA_TEXT, message);                                     // message
        }
        else
        {
            // Create intent with message
            shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/html");
            shareIntent.putExtra(Intent.EXTRA_TEXT, message); //message
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "I wanted to share this with you!"); //subject
        }

        // Show app chooser and send intent
        v.getContext().startActivity(Intent.createChooser(shareIntent, "Share via"));

    }


}
