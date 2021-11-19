package com.example.angelmendez.cityreport;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
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

    //private static final long serialVersionUID = 6529685098267757690L;

    private final String PARENT_DIR_PATH;
    private final String REPORTS_DIR_PATH;
    private final String PHOTOS_DIR_PATH;

    private Calendar date;
    private transient ArrayList<Bitmap> photoArray; // Bitmaps are not serielizable
    private String description;
    private String category;
    private String fileName;
    private String photoDirectoryName;
    private ReportLocation locationObject;

    private transient Context context;

    public ReportObject(
            ReportLocation locationObject, Calendar date, ArrayList<Bitmap> photoArray, String description, String category, Context context)
    {
        //this.nearStreet = street;
        this.locationObject = locationObject;
        this.date = date;
        this.photoArray = photoArray;
        this.description = description;
        this.category = category;
        this.fileName = generateFileName();
        this.photoDirectoryName = generateFileName() + "_DIR";
        this.context = context;

        PARENT_DIR_PATH =  context.getFilesDir().getAbsolutePath() + File.separator + "ReportsDir";
        REPORTS_DIR_PATH = PARENT_DIR_PATH + File.separator + "Reports";
        PHOTOS_DIR_PATH =  PARENT_DIR_PATH + File.separator + "ReportPhotos";

    }

    // Getter Methods

    public Context getContext(){return this.context;}

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

    public ReportLocation getLocationObject() {
        return locationObject;
    }

    public String getDescription() {
        return description;
    }

    public String getCategory() {
        return category;
    }


    // Setter Methods

    public void setContext(Context context){this.context = context;}
    public void setPhotoDirectoryName(String string)
    {
        this.photoDirectoryName = string;
    }

    public void setPhotoArray(ArrayList<Bitmap> photoArray) {
        this.photoArray = photoArray;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setDate(Calendar date) {
        this.date = date;
    }

    public void setLocationObject(ReportLocation locationObject) {
        this.locationObject = locationObject;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCategory(String category) {
        this.category = category;
    }



    public void saveToFile() {

        // If photos were added to the report, save them to persistent storage
        if (photoArray != null && photoArray.size() != 0) {

            // Create a photo directory for this report, if doesnt currently exist
            String filePath = PHOTOS_DIR_PATH + File.separator + photoDirectoryName;
            File reportPhotoDir = new File(filePath);

            if (!reportPhotoDir.exists()) {
                reportPhotoDir.mkdirs();
            }

            String fileName;
            FileOutputStream fos = null;

            try {
                for(int i = 0; i < photoArray.size(); i++)
                {
                    fileName = filePath + File.separator + generateFileName() + ".jpg";
                    fos = new FileOutputStream(new File(fileName));
                    photoArray.get(i).compress(Bitmap.CompressFormat.JPEG, 90, fos);
                }

                fos.close();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        else // TODO SET DELETE THIS ELSE STATEMENT, CHECK PARTS OF THE CODE THAT RELY ON THIS
        {
            photoDirectoryName = null;
        }

        try {

            String filePath =  REPORTS_DIR_PATH + File.separator + fileName;

            FileOutputStream fileOutputStream = new FileOutputStream(new File(filePath));
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);

            objectOutputStream.writeObject(this);

            objectOutputStream.close();
            fileOutputStream.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void uploadToServer()
    {
        // URL of server and API call to upload the current report
        String url = "http://18.217.120.94/api/upload-report";

        // Create JSON object for the report using a hashmap
        Map map = new HashMap();

        map.put("country", locationObject.getCountry());
        map.put("city", locationObject.getCity());
        map.put("state", locationObject.getState());
        map.put("county", locationObject.getCounty());
        map.put("zipcode", locationObject.getZip());
        map.put("street_number", locationObject.getStreetNumber());
        map.put("street_name", locationObject.getStreetName());
        map.put("latitude", locationObject.getLatitude());
        map.put("longitude", locationObject.getLongitude());
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

        // Access the RequestQueue through the Volley singleton class.
        VolleySingleton.getInstance(context).getQueue().add(jsonObjectRequest);
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
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            Log.d("File", e.toString());
        }


        if (reportObject != null && reportObject.getPhotoDirectoryName() != null) {

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

        reportObject.setContext(context);
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

        FileOutputStream fos = null;
        String dirPath = context.getFilesDir().getAbsolutePath() + File.separator + "ReportsDir" + File.separator + "ReportPhotos" + File.separator + report.getPhotoDirectoryName();

       //  if photodirname is null, initialize it
        if (report.getPhotoDirectoryName() == null) {

            report.setPhotoDirectoryName(generateFileName() + "_DIR");
            dirPath = context.getFilesDir().getAbsolutePath() + File.separator + "ReportsDir" + File.separator + "ReportPhotos" + File.separator + report.getPhotoDirectoryName();
            File reportPhotoDir = new File(dirPath);

            if (!reportPhotoDir.exists()) {                  // if the report happens to exist this could cause trouble, figure out what to do if it fails
                reportPhotoDir.mkdirs();
            }
        }

        // if there is something in the photoarray, delete all of the photos in the files, and add everything in the photoarray to the files.
        if (report.getPhotoArray() != null && report.getPhotoArray().size() != 0)
        {
            // delete all pictures
            File reportPhotoDir = new File(dirPath);
            File[] files = reportPhotoDir.listFiles();
            for (int i = 0; i < files.length; i++)
            {
                files[i].delete();
            }

            // add pictures
            String fileName;
            File filePhoto;

            try {
                for(int i = 0; i < report.getPhotoArray().size(); i++)
                {
                    fileName = dirPath + File.separator + generateFileName() + ".jpg";
                    filePhoto = new File(fileName);
                    fos = new FileOutputStream(filePhoto);
                    report.getPhotoArray().get(i).compress(Bitmap.CompressFormat.JPEG, 90, fos);
                }

                fos.close();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
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
