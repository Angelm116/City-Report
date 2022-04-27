package com.example.angelmendez.cityreport;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.common.api.HasApiKey;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class ReportObject implements Serializable {

    private static String PARENT_DIR_PATH;
    private static String REPORTS_DIR_PATH;
    private static String PHOTOS_DIR_PATH;

    private Calendar date;
    private transient ArrayList<Bitmap> photoArray; // Bitmaps are not serializable
    private String description;
    private String category;
    private String fileName; // name of the file containing the report in persistent storage
    private String photoDirectoryName;
    private HashMap<String, String> locationInfo;
    private double latitude;
    private double longitude;


    // Constructor
    public ReportObject()
    {
        this.fileName = generateFileName();
        this.photoDirectoryName = generateFileName() + "_DIR";
        //this.photoArray = new ArrayList<>();

    }


    // Getter Methods

    public ArrayList<Bitmap> getPhotoArray() {
        return photoArray;
    }
    public String getPhotoDirectoryName() {
        return photoDirectoryName;
    }
    public String getFileName() {
        return fileName;
    }
    public Calendar getDate() {
        return date;
    }
    public HashMap<String, String> getLocationInfo() {
        return locationInfo;
    }
    public String getDescription() {
        return description;
    }
    public String getCategory() {
        return category;
    }
    public String getPhotoDirPath(){return PHOTOS_DIR_PATH + File.separator + photoDirectoryName;}
    public String getReportDirPath(){return REPORTS_DIR_PATH + File.separator + fileName;}
    //public LatLng getLatLng() {return latLng;}
    public double getLatitude() {return this.latitude;}
    public double getLongitude() {return this.longitude;}


    // Setter Methods

    public void setPhotoArray(ArrayList<Bitmap> photoArray) {
        this.photoArray = photoArray;
    }
    public void setDate(Calendar date) {
        this.date = date;
    }
    public void setLocationObject(HashMap locationInfo) { this.locationInfo = locationInfo; }
    public void setDescription(String description) {
        this.description = description;
    }
    public void setCategory(String category) {
        this.category = category;
    }
    public void setLatLng(LatLng latLng) {
        this.latitude = latLng.latitude;
        this.longitude = latLng.longitude;
    }


    // This function saves or updates the given report in persistent storage
    public static void saveToFile(ReportObject report) {

        // Check if the report object has any photos attached and save them to persistent storage.
        if (report.getPhotoArray() != null)
        {
            File reportPhotoDir = new File(report.getPhotoDirPath());

            // Create the photo directory for this report, if it doesnt exist
            if (!reportPhotoDir.exists()) {
                reportPhotoDir.mkdirs();
            }

            // Delete any photos in the report's photo directory, if any
            File[] photoFiles = reportPhotoDir.listFiles();

            if (photoFiles != null) {

                // delete photos
                for (int i = 0; i < photoFiles.length; i++)
                {

                    photoFiles[i].delete();
                }
            }

            // Save all the photos in the report's photoArray to persistent storage.
            try {
                String fileName;
                FileOutputStream fileOutputStream;

                for(int i = 0; i < report.getPhotoArray().size(); i++)
                {
                    fileName = report.getPhotoDirPath() + File.separator + generateFileName() + ".jpg";
                    File file = new File(fileName);
                    fileOutputStream = new FileOutputStream(file);
                    report.getPhotoArray().get(i).compress(Bitmap.CompressFormat.JPEG, 90, fileOutputStream);
                    fileOutputStream.close();
                }

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        // Save the form portion of the report to persistent storage.
        try {

            File file = new File(report.getReportDirPath());

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

    // Uploads the current report to the server.
    public void uploadToServer(Context context, Boolean isUpdate) {

        // URL of server and API call to upload the current report
        String url;

        if (isUpdate) {
            return; // TODO: Should set the URL to the update api instead of returning
        }
        else {
            url = "http://test3-env-1.eba-sag8w2d6.us-east-2.elasticbeanstalk.com/api/upload-report"; // TODO: Change this to the current server URL
        }

        // Create JSON object for the report using a hashmap
        Map map = new HashMap();

        map.put("lat", latitude);
        map.put("lng", longitude);
        map.put("category", category);
        map.put("report_description", description);

        // Format date and time into mysql-acceptable format
        SimpleDateFormat simpleDateFormat;
        simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateTime = simpleDateFormat.format(date.getTime());

        map.put("date_time", dateTime);

        // JSON object for this report
        JSONObject reportJSON = new JSONObject(map);

        // Make http post request
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.POST, url, reportJSON, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("response", response.toString());
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO: Handle error
                        Log.d("response", error.toString());

                    }
                });

        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(20 * 1000, 2, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        // Access the RequestQueue through the Volley singleton class.
        VolleySingleton.getInstance(context).getQueue().add(jsonObjectRequest);
    }

    // This function deletes all the files associated with the given report
    public static void deleteReportFiles(ReportObject report) {
        // delete photo files
        File photoDirectory = new File(report.getPhotoDirPath());
        File[] photoFiles = photoDirectory.listFiles();

        if (photoFiles != null) {
            for (int i = 0; i < photoFiles.length; i++) {
                photoFiles[i].delete();
            }
        }

        // delete photo directory
        photoDirectory.delete();

        // delete report's form file
        File formFile = new File(report.getReportDirPath());
        formFile.delete();
    }

    // This function is used to generate random file names
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

    // This function creates a ReportObject from the given file
    public static ReportObject readFromFile(String fileName) {

        ReportObject reportObject = null;
        try {
            String dirPath =  ReportObject.REPORTS_DIR_PATH + File.separator + fileName;
            File file = new File(dirPath);
            FileInputStream fileInputStream = new FileInputStream(file);
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            reportObject = (ReportObject) objectInputStream.readObject();
            objectInputStream.close();
            fileInputStream.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            Log.d("File", e.toString());
        }


        String photoDirPath = ReportObject.PHOTOS_DIR_PATH + File.separator + reportObject.getPhotoDirectoryName();
        File photoDir  = new File(photoDirPath);
        File[] photoFiles = photoDir.listFiles();

        if (reportObject != null && photoFiles != null) {

            ArrayList<Bitmap> photos = new ArrayList<>();

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

    // This functions fetches the reports in persistent storage and returns them as an ArrayList of ReportObjects
    public static ArrayList loadReportsFromFiles() {
        // Store all the files inside of ReportsDir/Reports in an array, files
        File directory = new File(ReportObject.REPORTS_DIR_PATH);

        // Get a list of files in the directory
        File[] files = directory.listFiles();
        ReportObject holder;

        // Check if list is empty
        if (files == null)
        {
            Log.d("loadReportsFromFiles", "There are no reports in storage");
            return null;
        }

        // Turn the list of files into a list of ReportObjects
        ArrayList<ReportObject> reportList = new ArrayList<>();

        for (int i = 0; i < files.length; i++)
        {
            // deserialize the file into a ReportObject
            holder = ReportObject.readFromFile(files[i].getName());

            // Check that it didnt fail
            if (holder != null)
            {
                reportList.add(holder);  // add to list
            }
        }

        return reportList;
    }

    // this function creates the directories where the reports are stored in the device's persistent storage
    public static void createDirectories(Context context)
    {
        // Instantiate Directory paths
        PARENT_DIR_PATH =  context.getFilesDir().getAbsolutePath() + File.separator + "ReportsDir";
        REPORTS_DIR_PATH = PARENT_DIR_PATH + File.separator + "Reports";
        PHOTOS_DIR_PATH =  PARENT_DIR_PATH + File.separator + "ReportPhotos";

        File fileHolder;

        // Create the parent directory of the reports
        final String PARENT_DIR_PATH = context.getFilesDir().getAbsolutePath() + File.separator + "ReportsDir";
        fileHolder = new File(PARENT_DIR_PATH);

        // Create parent directory
        if (!fileHolder.exists()) {
            fileHolder.mkdirs();
        }

        // Within parent directory, create Reports directory to store report forms
        final String REPORT_DIR_PATH = PARENT_DIR_PATH + File.separator + "Reports";
        fileHolder = new File(REPORT_DIR_PATH);

        if (!fileHolder.exists()) {
            fileHolder.mkdirs();
        }

        // Within parent directory, create ReportPhotos directory to store the photos associated with each form
        final String PHOTO_DIR_PATH = PARENT_DIR_PATH + File.separator + "ReportPhotos";
        fileHolder = new File(PHOTO_DIR_PATH);

        if (!fileHolder.exists()) {
            fileHolder.mkdirs();
        }
    }



    // This might be helpful in the future
    // This function would return the formatted address of the report
//    public String getFormattedAddress()
//    {
//        String formattedAddress = "";
//
//        formattedAddress += streetNumber == null ? "" : streetNumber + " ";
//        formattedAddress += streetName == null ? "" : streetName + ", ";
//        formattedAddress += city == null ? "" : city + " ";
//        formattedAddress += state == null ? "" : state + " ";
//        formattedAddress += zip == null ? "" : zip;
//
//
//        return formattedAddress.trim();
//    }
//
//    public String getStreetAddress()
//    {
//        return streetNumber + " " + streetName;
//    }
}
