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
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.File;
import java.util.ArrayList;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;


public class MainActivity extends AppCompatActivity{

    private FragmentManager fragmentManager;
    public Fragment currentFragment;

    // Fragments where the different paged of the app are displayed
    public FragmentForm fragmentForm;
    public FragmentSelectLocation fragmentSelectLocation;
    public FragmentHome fragmentHome;


    private FusedLocationProviderClient fusedLocationClient;
    private Location lastKnownLocation;
    private LatLng latLng;
    private ArrayList<ReportObject> dataSet;
    private TextView toolbarTitle;

    LatLng locationHolder = null;

    int PERMISSION_ID = 44;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main_activity_container);
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        toolbarTitle = findViewById(R.id.toolbar_title);

        setStatusBarColor();

        setUpDirectories();
        loadReportsFromFiles();

        setUpFragments();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        getLastLocation();
        
    }

    private void setStatusBarColor()
    {
        Window window = this.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.black_overlay));
    }

    private void setUpFragments()
    {
        fragmentManager = getSupportFragmentManager();
        fragmentForm = new FragmentForm();
        fragmentSelectLocation = new FragmentSelectLocation();
        fragmentHome = new FragmentHome(dataSet);

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();


        fragmentTransaction.add(R.id.main_activity_container, fragmentForm, "formFragment").hide(fragmentForm);
        fragmentTransaction.add(R.id.main_activity_container, fragmentSelectLocation, "changeLocationFragment").hide(fragmentSelectLocation);
        fragmentTransaction.add(R.id.main_activity_container, fragmentHome, "reportsFragment");
        fragmentTransaction.commit();
    }
    
    private void setUpDirectories()
    {
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
    }
    

    public void loadReportsFromFiles()
    {
        Log.d("Method", "entering MainActivity: getReports");
        String dirPath = this.getFilesDir().getAbsolutePath() + File.separator + "ReportsDir" + File.separator + "Reports";
        File directory = new File(dirPath);
        File[] files = directory.listFiles();
        ReportObject holder;

        if (files == null)
        {
            return;
        }

        Log.d("Method", "getReports, files size: "+ files.length);
        dataSet = new ArrayList<>();
        for (int i = 0; i < files.length; i++)
        {
            holder = ReportObject.readFromFile(this, files[i].getName());
            if (holder != null)
            {
                dataSet.add(holder);
                dataSet.get(i).fixLocation();
            }

            Log.d("Method", "getReports, FileName:" + files[i].getName());

        }

        Log.d("Method", " out MainActivity: getReports");

    }

    public void startNewReport()
    {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbarTitle.setText("New Report");
        fragmentForm.resetForm();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.hide(fragmentHome);
        fragmentTransaction.show(fragmentForm);
        fragmentTransaction.commit();
        currentFragment = fragmentForm;
    }

    public void updateReport(ReportObject report)
    {
        // go to form fragment
        // populate it with report info

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbarTitle.setText("Update Report");
        fragmentForm.resetForm();
        fragmentForm.populateForm(report);
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.hide(fragmentHome);
        fragmentTransaction.show(fragmentForm);
        fragmentTransaction.commit();
        currentFragment = fragmentForm;


    }

    public boolean deleteReport(ReportObject report)
    {
        // delete directory of the report form
        // delete directory of the photos of the report
        // delete directory from dataset
        // update recyrclerview

        ReportObject.deleteReportFiles(this, report);
        dataSet.remove(report);
        fragmentHome.updateReportsList(dataSet);
        return true;
    }


    public void loadReportsFragment(){

        Log.d("Method", " on MainActivity: loadReportsFragment");
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().show();
        toolbarTitle.setText("Reports");
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.hide(fragmentForm);

        loadReportsFromFiles();
        fragmentHome.updateReportsList(dataSet);

        fragmentTransaction.show(fragmentHome);
        fragmentTransaction.commit();
        currentFragment = fragmentHome;

        Log.d("Method", " out MainActivity: loadReportsFragment");

    }

    public void selectNewLocation(View view){

        getSupportActionBar().hide();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.hide(fragmentForm);
        fragmentTransaction.show(fragmentSelectLocation);
        fragmentTransaction.commit();
        currentFragment = fragmentSelectLocation;
        fragmentSelectLocation.updateMap(latLng);
    }

    public void confirmNewLocation(LatLng location){

        getSupportActionBar().show();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.hide(fragmentSelectLocation);
        fragmentTransaction.show(fragmentForm);
        fragmentTransaction.commit();
        currentFragment = fragmentForm;
        fragmentForm.updateMap(location);

    }




    //get users location and request appropiate permissions

    @SuppressLint("MissingPermission")
    private void getLastLocation() {
        // check if permissions are given
        if (checkPermissions()) {

            // check if location is enabled
            if (isLocationEnabled()) {

                // getting last
                // location from
                // FusedLocationClient
                // object
               fusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        lastKnownLocation = task.getResult();
                        if (lastKnownLocation == null) {
                            requestNewLocationData();
                        } else {
                            latLng = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
                            fragmentForm.updateMap(latLng);
                        }
                    }
                });
            }
            else {
                Toast.makeText(this, "Please turn on" + " your location...", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        }
        else {
            // if permissions aren't available,
            // request for permissions
            requestPermissions();
        }
    }

    @SuppressLint("MissingPermission")
    private void requestNewLocationData() {

        // Initializing LocationRequest
        // object with appropriate methods
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(5);
        mLocationRequest.setFastestInterval(0);
        mLocationRequest.setNumUpdates(1);

        // setting LocationRequest
        // on FusedLocationClient
       fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
       fusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
    }

    private LocationCallback mLocationCallback = new LocationCallback() {

        @Override
        public void onLocationResult(LocationResult locationResult) {
            lastKnownLocation = locationResult.getLastLocation();

            latLng = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
            fragmentForm.updateMap(latLng);
        }
    };

    // method to check for permissions
    private boolean checkPermissions() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        // If we want background location
        // on Android 10.0 and higher,
        // use:
        // ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    // method to request for permissions
    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_ID);
    }

    // method to check
    // if location is enabled
    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    // If everything is alright then
    @Override
    public void
    onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_ID) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (checkPermissions()) {
            getLastLocation();
        }
    }


    public void setLocationHolder(LatLng location)
    {
        locationHolder = location;
    }

    public LatLng getLocationHolder() {
        return locationHolder;
    }

    public LatLng getLatLng() {


        return latLng;
    }


    // controls the back arrow in the toolbar
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                getSupportActionBar().show();
                toolbarTitle.setText("Reports");
                fragmentForm.resetForm();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.hide(fragmentForm);
                fragmentTransaction.show(fragmentHome);
                fragmentTransaction.commit();
                currentFragment = fragmentHome;
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {

        BackPressSupport fragmentPressed = (BackPressSupport)currentFragment;
        if(fragmentPressed.onBackPressed() == false)
        {
            super.onBackPressed();
        }


    }





}
