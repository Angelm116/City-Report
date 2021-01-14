package com.example.angelmendez.cityreport;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

public class ReportObject implements Serializable {
    private String nearStreet;
    private String date;
    private  transient ArrayList<Bitmap> photoArray;
    private transient LatLng location;
    private String description;
    private String category;
    private String fileName;
    private double lat;
    private double lng;
    private String photoDirectoryName;

    public ReportObject(String street, String date, ArrayList<Bitmap> photoArray, LatLng location, String description, String category)
    {
        this.nearStreet = street;
        this.date = date;
        this.photoArray = photoArray;
        this.location = location;
        this.description = description;
        this.category = category;
        fileName = generateFileName();
        lat = location.latitude;
        lng = location.longitude;

//        if(photoArray != null)
//        {
//            photoArray.compress(Bitmap.CompressFormat.PNG, 100, stream);
//        }
    }

    public void fixLocation()
    {
        location = new LatLng(lat, lng);
    }

    public static String generateFileName() {
        Random generator = new Random();
        StringBuilder randomStringBuilder = new StringBuilder();
        //int randomLength = generator.nextInt(5);
        int randomLength = 6;
        char tempChar;
        int holder;
        for (int i = 0; i < randomLength; i++){

            holder = generator.nextInt(3);

            if (holder == 0)
            {
                tempChar = (char) (generator.nextInt(26) + 65);
            }
            else if (holder == 1)
            {
                tempChar = (char) (generator.nextInt(26) + 97);
            }
            else
            {
                tempChar = (char) (generator.nextInt(10) + 48);
            }

            randomStringBuilder.append(tempChar);
        }
        return randomStringBuilder.toString();
    }

    public ArrayList<Bitmap> getPhotoArray() {
        return photoArray;
    }

    public String getDate() {
        return date;
    }

    public LatLng getLocation() {
        return location;
    }

    public String getNearStreet() {
        return nearStreet;
    }

    public void setNearStreet(String nearStreet) {
        this.nearStreet = nearStreet;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setPhotoArray(ArrayList<Bitmap> photoArray) {
        this.photoArray = photoArray;
    }

    public void setLocation(LatLng location) {
        this.location = location;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getPhotoDirName() {
        return photoDirectoryName;
    }

    public void saveToFile(Context context) {

        if (photoArray != null) {

            // make directory of photos for this particular report
            photoDirectoryName = generateFileName() + "_DIR";
            String dirPath = context.getFilesDir().getAbsolutePath() + File.separator + "ReportsDir" + File.separator + "ReportPhotos" + File.separator + photoDirectoryName;
            File reportPhotoDir = new File(dirPath);
            if (!reportPhotoDir.exists()) {
                reportPhotoDir.mkdirs();

            }


            
            //photoDirectoryName = generateFileName() + ".jpg";
            //String photoDirPath = context.getFilesDir().getAbsolutePath() + File.separator + "ReportsDir" + File.separator + "ReportPhotos" + File.separator + photoDirectoryName;
            
            
            
            File filePhoto;
            FileOutputStream fos = null;

           
            try {
                for(int i = 0; i < photoArray.size(); i++)
                {
                    filePhoto = new File(dirPath + File.separator + generateFileName() + ".jpg");
                    fos = new FileOutputStream(filePhoto);
                    photoArray.get(i).compress(Bitmap.CompressFormat.JPEG, 90, fos);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else
        {
            photoDirectoryName = null;
        }

        try {
            String dirPath =  context.getFilesDir().getAbsolutePath() + File.separator + "ReportsDir" + File.separator + "Reports" + File.separator + fileName;
            File file = new File(dirPath);
           // FileOutputStream fileOutputStream = context.openFileOutput(file, Context.MODE_PRIVATE);
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(this);
            objectOutputStream.close();
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static ReportObject readFromFile(Context context, String fileName) {
        ReportObject reportObject = null;
        try {
            String dirPath =  context.getFilesDir().getAbsolutePath() + File.separator + "ReportsDir" + File.separator + "Reports" + File.separator + fileName;
            File file = new File(dirPath);
            //FileInputStream fileInputStream = context.openFileInput(fileName);
            FileInputStream fileInputStream = new FileInputStream(file);
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            reportObject = (ReportObject) objectInputStream.readObject();
            objectInputStream.close();
            fileInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        if (reportObject.getPhotoDirName() != null) {

            ArrayList<Bitmap> photos = new ArrayList<>();
            String photoDirPath = context.getFilesDir().getAbsolutePath() + File.separator + "ReportsDir" + File.separator + "ReportPhotos" + File.separator + reportObject.getPhotoDirName();
            File photoDir  = new File(photoDirPath);
            File[] photoFiles = photoDir.listFiles();
            try {

                for (int i = 0; i < photoFiles.length; i++)
                {
                    photos.add(BitmapFactory.decodeStream(new FileInputStream(photoFiles[i])));
                }
                reportObject.setPhotoArray(photos);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

        }

        return reportObject;
    }
}
