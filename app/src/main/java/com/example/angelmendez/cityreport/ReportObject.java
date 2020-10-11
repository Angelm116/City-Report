package com.example.angelmendez.cityreport;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

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
import java.util.Date;
import java.util.Random;

public class ReportObject implements Serializable {
    private String nearStreet;
    private String date;
    private  transient Bitmap photo;
    private transient LatLng location;
    private String description;
    private String category;
    private String fileName;
    private double lat;
    private double lng;
    private String photoFileName;

    public ReportObject(String street, String date, Bitmap photo, LatLng location, String description, String category)
    {
        this.nearStreet = street;
        this.date = date;
        this.photo = photo;
        this.location = location;
        this.description = description;
        this.category = category;
        fileName = generateFileName();
        lat = location.latitude;
        lng = location.longitude;

//        if(photo != null)
//        {
//            photo.compress(Bitmap.CompressFormat.PNG, 100, stream);
//        }
    }

    public void fixLocation()
    {
        location = new LatLng(lat, lng);
    }

    public static String generateFileName() {
        Random generator = new Random();
        StringBuilder randomStringBuilder = new StringBuilder();
        int randomLength = generator.nextInt(5);
        char tempChar;
        for (int i = 0; i < randomLength; i++){
            tempChar = (char) (generator.nextInt(96) + 32);
            randomStringBuilder.append(tempChar);
        }
        return randomStringBuilder.toString();
    }

    public Bitmap getPhoto() {
        return photo;
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

    public void setPhoto(Bitmap photo) {
        this.photo = photo;
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

    public String getPhotoFileName() {
        return photoFileName;
    }

    public void saveToFile(Context context) {

        if (photo != null) {
            photoFileName = generateFileName();
            String dirPathPhoto = context.getFilesDir().getAbsolutePath() + File.separator + "ReportsDir" + File.separator + "ReportPhotos" + File.separator + photoFileName;
            File filePhoto = new File(dirPathPhoto);
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(filePhoto);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            photo.compress(Bitmap.CompressFormat.PNG, 90, fos);
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else
        {
            photoFileName = null;
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

        if (reportObject.getPhotoFileName() != null) {
            String dirPathPhoto = context.getFilesDir().getAbsolutePath() + File.separator + "ReportsDir" + File.separator + "ReportPhotos" + File.separator + reportObject.getPhotoFileName();
            File filePhoto = new File(dirPathPhoto);
            try {
                Bitmap bitmap = BitmapFactory.decodeStream(new FileInputStream(filePhoto));
                reportObject.setPhoto(bitmap);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

        }

        return reportObject;
    }
}