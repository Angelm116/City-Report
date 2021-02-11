package com.example.angelmendez.cityreport;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;


import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private FragmentManager fragmentManager;
    private Fragment switchFragment;
    public Fragment currentFragment;
    public FormFragment formFragment;
    public ChangeLocationFragment changeLocationFragment;

    ReportsFragment reportsFragment;

    private FusedLocationProviderClient fusedLocationProviderClient;
    private Location lastKnownLocation;
    private LatLng latLng;
    private ArrayList<ReportObject> dataSet;
    private TextView title;

    LatLng locationHolder = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main_activity_container);
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        title = findViewById(R.id.toolbar_title);
        //dataSet = new ArrayList<>();

        Window window = this.getWindow();

// clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

// add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

// finally change the color
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.black_overlay));


        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        String dirPath = getFilesDir().getAbsolutePath() + File.separator + "ReportsDir";
        File projDir = new File(dirPath);
        if (!projDir.exists()) {
            projDir.mkdirs();
            Log.d("filestart", "onCreate: here");

        }

        String reportFormDirPath = dirPath + File.separator + "Reports";
        File reportFormDir = new File(reportFormDirPath);
        if (!reportFormDir.exists()) {
            reportFormDir.mkdirs();
            Log.d("filestart", "onCreate: here");

        }

        String photoDirPath = dirPath + File.separator + "ReportPhotos";
        File reportPhotosDir = new File(photoDirPath);
        if (!reportPhotosDir.exists()) {
            reportPhotosDir.mkdirs();
            Log.d("filestart", "onCreate: here");

        }

        String[] files = this.fileList();

        getReports();

        fragmentManager = getSupportFragmentManager();
        formFragment = new FormFragment();
        changeLocationFragment = new ChangeLocationFragment();
        reportsFragment = new ReportsFragment(dataSet);

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();


        fragmentTransaction.add(R.id.main_activity_container, formFragment, "formFragment").hide(formFragment);
        fragmentTransaction.add(R.id.main_activity_container, changeLocationFragment, "changeLocationFragment").hide(changeLocationFragment);
        fragmentTransaction.add(R.id.main_activity_container, reportsFragment, "reportsFragment");
        fragmentTransaction.commit();


        if (formFragment == null)
        {
            Log.d("TAG", "onCreate: rage");
        }

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        Log.d("mapTrack", "about to go in getlastlocation");
        getLocationPermission();


//        Log.d("FILE", "" + files[12]);

    }

    @Override
    public void onBackPressed() {

        BackPressSupport fragmentPressed = (BackPressSupport)currentFragment;
        if(fragmentPressed.onBackPressed() == false)
        {
            super.onBackPressed();
        }


    }

    public void startNewReport()
    {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        title.setText("New Report");
        formFragment.resetForm();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.hide(reportsFragment);
        fragmentTransaction.show(formFragment);
        fragmentTransaction.commit();
        currentFragment = formFragment;
    }

    public void getReports()
    {

        String dirPath = this.getFilesDir().getAbsolutePath() + File.separator + "ReportsDir" + File.separator + "Reports";
        File directory = new File(dirPath);
        File[] files = directory.listFiles();

        if (files == null)
        {
            return;
        }

        Log.d("Files", "Size: "+ files.length);
        dataSet = new ArrayList<>();
        for (int i = 0; i < files.length; i++)
        {
            dataSet.add(ReportObject.readFromFile(this, files[i].getName()));
            dataSet.get(i).fixLocation();
            Log.d("Files", "FileName:" + files[i].getName());
            Log.d("save", dataSet.get(i).getDate());
        }

        Log.d("TAG", dataSet.size() + "");

    }

    public void loadReportsFragment(){

        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().show();
        title.setText("Reports");
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.hide(formFragment);

        getReports();
        reportsFragment.updateReports(dataSet);

        fragmentTransaction.show(reportsFragment);
        fragmentTransaction.commit();
        currentFragment = reportsFragment;

    }

    public void changeLocationStart(View view){

        getSupportActionBar().hide();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.hide(formFragment);
        fragmentTransaction.show(changeLocationFragment);
        fragmentTransaction.commit();
        currentFragment = changeLocationFragment;
        changeLocationFragment.updateMap(latLng);
    }

    public void setLocationHolder(LatLng location)
    {
        locationHolder = location;
    }

    public LatLng getLocationHolder() {
        return locationHolder;
    }

    public void changeLocationEnd(LatLng location){

        getSupportActionBar().show();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.hide(changeLocationFragment);
        fragmentTransaction.show(formFragment);
        fragmentTransaction.commit();
        currentFragment = formFragment;
        formFragment.updateMap(location);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

    }

    public void getLastLocation() {


        //getLocationPermission();
        // Get last known recent location using new Google Play Services SDK (v11+)
        FusedLocationProviderClient locationClient = getFusedLocationProviderClient(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            Log.d("PERMISSION", "getLastLocation: PERMISSION NOT GRANTED ");

          //  return;
        }

        try {

            Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();
            locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    if (task.isSuccessful()) {
                        // Set the map's camera position to the current location of the device.
                        lastKnownLocation = task.getResult();

                        if(lastKnownLocation == null)
                        Log.d("LOCATION", "isnull");

                        latLng = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
                        if (latLng == null)
                        {
                            Log.d("LOCATION", "onComplete: ");
                        }
                        Log.d("test", "onCreate: " + latLng);

                        if (formFragment == null)
                        {
                            Log.d("fragment", "isnull");
                        }
                        Log.d("mapTrack", "about to update map");
                       formFragment.updateMap(latLng);
                    } else {
                        Log.d("LOCATION", "Current location is null. Using defaults.");
                        Log.e("LOCATION", "Exception: %s", task.getException());

                    }

                }
            });

        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage(), e);
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                getSupportActionBar().show();
                title.setText("Reports");
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.hide(formFragment);
                fragmentTransaction.show(reportsFragment);
                fragmentTransaction.commit();
                currentFragment = reportsFragment;
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        Log.d("mapTrack", "onRequestPermissionsResult: ");

        getLastLocation();
//        switch (requestCode) {
//            case 3: {
//                if (permissions[0].equals(Manifest.permission.ACCESS_COARSE_LOCATION)) {
//                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                        getLastLocation();
//
//                    } else{
//
//                    }
//                }
//            }
//        }
    }

    private void getLocationPermission() {



        if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.INTERNET},
                    1);
            // Permission is not granted
            Log.d("MapDemoActivity", "Internet permission not granted");

        }
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
//                != PackageManager.PERMISSION_GRANTED) {
//
//            ActivityCompat.requestPermissions(this,
//                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
//                    2);
//            Log.d("MapDemoActivity", "Fine permission not granted");
//
//        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    3);
            Log.d("MapDemoActivity", "Coarse permission not granted");

        }
        else
        {
            getLastLocation();
        }
    }

    public LatLng getLatLng() {


        return latLng;
    }

    public void submit(View view)
    {
        (formFragment).setNearStreet(latLng);
    }


}
