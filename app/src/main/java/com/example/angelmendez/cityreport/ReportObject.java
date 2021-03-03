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
import java.nio.file.Files;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

public class ReportObject implements Serializable {
    private String nearStreet;
    private String date;
    private  transient ArrayList<Bitmap> photoArray;
    private ArrayList<String> photoFileNames;
    private  transient ArrayList<Bitmap> photosToAdd;
    private ArrayList<String> photosToDelete;
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
        this.fileName = generateFileName();
        this.lat = location.latitude;
        this.lng = location.longitude;

    }


    public ArrayList<Bitmap> getPhotoArray() {
        return photoArray;
    }

    public ArrayList<Bitmap> getPhotosToAdd() {
        return photosToAdd;
    }

    public ArrayList<String> getPhotosToDelete() {
        return photosToDelete;
    }

    public String getPhotoDirectoryName() {
        return photoDirectoryName;
    }

    public ArrayList<String> getPhotoFileNames() {
        return photoFileNames;
    }


    public String getFileName() {
        return fileName;
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

    public String getDescription() {
        return description;
    }

    public String getCategory() {
        return category;
    }



    public void setPhotoDirectoryName(String string)
    {
        this.photoDirectoryName = string;
    }

    public void setPhotoArray(ArrayList<Bitmap> photoArray) {
        this.photoArray = photoArray;
    }

    public void setPhotoFileNames(ArrayList<String> photoFileNames) {
        this.photoFileNames = photoFileNames;
    }

    public void setPhotosToDelete(ArrayList<String> photosToDelete) {
        this.photosToDelete = photosToDelete;
    }

    public void initializePhotoArray()
    {
        this.photoArray = new ArrayList<>();
    }

    public void addPhotoToBeAdded(Bitmap bitmap)
    {
        if(photosToAdd == null)
        {
            photosToAdd = new ArrayList<>();
        }

        photosToAdd.add(bitmap);
    }

    public void addPhotoToBeDeleted(String photoFileName)
    {
        if(photosToDelete == null)
        {
            photosToDelete = new ArrayList<>();
        }

        photosToDelete.add(photoFileName);
    }


    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setNearStreet(String nearStreet) {
        this.nearStreet = nearStreet;
    }

    public void setLocation(LatLng location) {
        this.location = location;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCategory(String category) {
        this.category = category;
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

            File filePhoto;
            FileOutputStream fos = null;

            try {
                for(int i = 0; i < photoArray.size(); i++)
                {
                    filePhoto = new File(dirPath + File.separator + generateFileName() + ".jpg");
                    fos = new FileOutputStream(filePhoto);
                    photoArray.get(i).compress(Bitmap.CompressFormat.JPEG, 90, fos);
                }

                fos.close();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
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

        if (reportObject.getPhotoDirectoryName() != null) {

            ArrayList<Bitmap> photos = new ArrayList<>();
            String photoDirPath = context.getFilesDir().getAbsolutePath() + File.separator + "ReportsDir" + File.separator + "ReportPhotos" + File.separator + reportObject.getPhotoDirectoryName();
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

    public static void deleteReportFiles(Context context, ReportObject report)
    {
        String dirPath;
        File fileHolder;

        // delete report form
        dirPath = context.getFilesDir().getAbsolutePath() + File.separator + "ReportsDir" + File.separator + "Reports" + File.separator + report.getFileName();
        fileHolder = new File(dirPath);
        fileHolder.delete();

        // delete photos directory
        dirPath = context.getFilesDir().getAbsolutePath() + File.separator + "ReportsDir" + File.separator + "ReportPhotos" + File.separator + report.getPhotoDirectoryName();
        fileHolder = new File(dirPath);
        fileHolder.delete();

    }

    public static void updateReportFile(Context context, ReportObject report) {
        // the deletion and addition to the photos array should be done before getting here
        // here we will only update the file for the object, the update of the object itself takes place somewhere else

        File fileHolder;
        FileOutputStream fos = null;
        String dirPath = context.getFilesDir().getAbsolutePath() + File.separator + "ReportsDir" + File.separator + "ReportPhotos" + File.separator + report.getPhotoDirectoryName();


        if (report.picturesUpdated()) // if photos were added or deleted
        {
            if (report.photoArray == null || report.photoArray.size() == 0) // user updated the report and deleted all existing pictures attached
            {

                if (report.getPhotosToDelete() != null) {
                    for (int i = 0; i < report.getPhotosToDelete().size(); i++) {
                        //get file names of photos to be deleted, go into dir and delete them one by one
                        fileHolder = new File(dirPath + File.separator + report.getPhotosToDelete().get(i));
                        fileHolder.delete();
                    }
                }

                report.setPhotosToDelete(null);
                report.setPhotoArray(null);

                fileHolder = new File(dirPath);
                fileHolder.delete();

                report.setPhotoDirectoryName(null);

            }
            else  // user updated the report and deleted or added some pictures
            {

                    // if the photo directory is null, create one
                if (report.getPhotoDirectoryName() == null) {

                    report.setPhotoDirectoryName(generateFileName() + "_DIR");
                    dirPath = context.getFilesDir().getAbsolutePath() + File.separator + "ReportsDir" + File.separator + "ReportPhotos" + File.separator + report.getPhotoDirectoryName();
                    File reportPhotoDir = new File(dirPath);

                    if (!reportPhotoDir.exists()) {                  // if the report happens to exist this could cause trouble, figure out what to do if it fails
                        reportPhotoDir.mkdirs();
                    }

                    // process deleted pictures
                    if (report.getPhotosToDelete() != null) {
                        for (int i = 0; i < report.getPhotosToDelete().size(); i++) {
                            //get file names of photos to be deleted, go into dir and delete them one by one
                            fileHolder = new File(dirPath + File.separator + report.getPhotosToDelete().get(i));
                            fileHolder.delete();
                        }
                    }

                    // process added photos
                    if (report.getPhotosToAdd() != null) {
                        try {
                            for (int i = 0; i < report.getPhotosToAdd().size(); i++) {
                                //transform photos to be added, go into photos directory and add them one by one
                                fileHolder = new File(dirPath + File.separator + generateFileName() + ".jpg");
                                fos = new FileOutputStream(fileHolder, false);
                                report.getPhotosToAdd().get(i).compress(Bitmap.CompressFormat.JPEG, 90, fos);
                            }
                            fos.flush();
                            fos.close();

                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            //update report form, which is inside of the reports directory
            try {
                dirPath = context.getFilesDir().getAbsolutePath() + File.separator + "ReportsDir" + File.separator + "Reports" + File.separator + report.getFileName();
               // File file = new File(dirPath);
                //file.delete();

                new FileOutputStream(dirPath).close();
                File file = new File(dirPath);

                FileOutputStream fileOutputStream = new FileOutputStream(file, false);
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
                objectOutputStream.writeObject(report);
                objectOutputStream.flush();
                objectOutputStream.close();
                fileOutputStream.flush();
                fileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }



    public boolean picturesUpdated()
    {
        if (photosToAdd != null || photosToDelete != null)
        {
            return true;
        }
        return false;
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
}
