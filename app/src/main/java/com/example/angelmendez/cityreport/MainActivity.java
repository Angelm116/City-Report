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

    // Fragments where the different pages of the app are displayed
    public FragmentForm fragmentForm;
    public FragmentSelectLocation fragmentSelectLocation;
    public FragmentHome fragmentHome;


    private FusedLocationProviderClient fusedLocationClient;
    private LatLng userLastKnownLocation;
    private ArrayList<ReportObject> dataSet;
    private TextView toolbarTitle;

    LatLng locationHolder = null;

    int PERMISSION_ID = 44;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main_activity_container);

        // Setup the toolbar
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar) ;
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // Set the toolbar title and color
        toolbarTitle = findViewById(R.id.toolbar_title);
        setStatusBarColor();



        ReportObject.createDirectories(this);
        dataSet = ReportObject.loadReportsFromFiles();

        // Initialize Fragments and Fragment Manager
        fragmentManager = getSupportFragmentManager();
        fragmentForm = new FragmentForm();
        fragmentSelectLocation = new FragmentSelectLocation();
        fragmentHome = new FragmentHome(dataSet);

        // Add fragments to the Fragment Manager
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.main_activity_container, fragmentForm, "formFragment");
        fragmentTransaction.add(R.id.main_activity_container, fragmentSelectLocation, "changeLocationFragment");
        fragmentTransaction.add(R.id.main_activity_container, fragmentHome, "reportsFragment");

        // Hide the Form Fragment and the ChangeLocation Fragments as we only want to display the Home Fragment
        fragmentTransaction.hide(fragmentForm);
        fragmentTransaction.hide(fragmentSelectLocation);
        fragmentTransaction.commit();

        // Instantiate location client and get users location
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        getLastLocation();
        
    }

    // This function sets the color of the status bar
    private void setStatusBarColor()
    {
        Window window = this.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.black_overlay));
    }


    // This function transitions from the current fragment (FragmentHome) to FragmentForm to start a new report
    public void startNewReport()
    {
        // Change the actions bar's title and enable home button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbarTitle.setText("New Report");

        // reset the form in fragmentForm
        fragmentForm.resetForm();

        // Hide current fragment and display fragmentForm fragment
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.hide(fragmentHome);
        fragmentTransaction.show(fragmentForm);
        fragmentTransaction.commit();
        currentFragment = fragmentForm;
    }

    // This function transitions from the current fragment (FragmentHome) to FragmentForm to update an existing report
    public void updateReport(ReportObject report)
    {
        // Change the actions bar's title and enable home button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbarTitle.setText("Update Report");

        // reset the form in fragmentForm
        fragmentForm.resetForm();

        // populate the form in FragmentForm with the data of the report being updated
        fragmentForm.populateForm(report);

        // Hide current fragment and display fragmentForm fragment
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.hide(fragmentHome);
        fragmentTransaction.show(fragmentForm);
        fragmentTransaction.commit();
        currentFragment = fragmentForm;
        Log.d("SAVED", "pulled location:" + report.getLocationObject().getLatitude() + ", " + report.getLocationObject().getLongitude());

    }

    // This function deletes a report
    public boolean deleteReport(ReportObject report)
    {

        // Delete the report in storage
        ReportObject.deleteReportFiles(report);

        // Remove the report object from dataset
        dataSet.remove(report);

        // Update the RecyclerView to display the updated dataset in FragmentHome
        fragmentHome.updateReportsList(dataSet);


        return true;
    }


    public void loadHomeFragment(){

        // Change the actions bar's title and disable home button
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().show();
        toolbarTitle.setText("Reports");

        // Fetch reports from storage and update the dataset
        dataSet = ReportObject.loadReportsFromFiles();

        //Log.d("PHOTOS", "updateReportsList: " + dataSet.get(dataSet.size() - 1).getPhotoArray().size());
        fragmentHome.updateReportsList(dataSet);

        // Hide current fragment and display fragmentHome fragment
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.hide(fragmentForm);
        fragmentTransaction.show(fragmentHome);
        fragmentTransaction.commit();
        currentFragment = fragmentHome;

        Log.d("loadHomeFragment", " out MainActivity: loadReportsFragment");

    }

    // This function opens the FragmentSelectLocation from the FragmentForm
    public void selectNewLocation(View view){

        // Hide the action bar
        getSupportActionBar().hide();

        // Hide current fragment (FragmentForm) and display fragmentSelectLocation fragment
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.hide(fragmentForm);
        fragmentTransaction.show(fragmentSelectLocation);
        fragmentTransaction.commit();
        currentFragment = fragmentSelectLocation;

        // Update the map in fragmentSelectLocation with the user's last known location
        fragmentSelectLocation.updateMap(userLastKnownLocation);
    }

    // This function closes the FragmentSelectLocation fragment, takes the user back to the FormFragment,
    // and sets the location of the report to the one chosen in the FragmentSelectLocation fragment
    public void confirmNewLocation(LatLng location){

        // show action bar
        getSupportActionBar().show();

        // Hide current fragment (fragmentSelectLocation) and display FormFragment fragment
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.hide(fragmentSelectLocation);
        fragmentTransaction.show(fragmentForm);
        fragmentTransaction.commit();
        currentFragment = fragmentForm;

        // Update Location in FragmentForm
        fragmentForm.updateMap(location);

    }




    // This function gets the user last known location
    // for more info: https://www.geeksforgeeks.org/how-to-get-user-location-in-android/
    @SuppressLint("MissingPermission")
    private void getLastLocation() {

        // Check that the needed permissions are given
        if (checkPermissions()) {

            // Check if location is enabled
            if (isLocationEnabled()) {

                // Get the last known location using the fusedLocationClient
               fusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {

                   // Listen for the completion of the asynchronous task
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {

                        Location lastKnownLocation = task.getResult();
                        // If task failed, call the backup method
                        if (lastKnownLocation == null) {
                            requestNewLocationData();
                        }
                        else {
                            userLastKnownLocation = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
                            fragmentForm.updateMap(userLastKnownLocation);
                            Log.d("userLastKnownLocation", "User's last known location: " + userLastKnownLocation);
                        }
                    }
                });
            }
            else { // if location is disabled

                // Tell user to turn on their location and take them to the settings to do so
                Toast.makeText(this, "Please turn on your location...", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        }
        else { // if permissions are not available, request permissions
            requestPermissions();
        }
    }

    // This function serves as a backup for getLastKnownLocation()
    // If getLastKnownLocation() fails, this function will keep trying
    // for more info: https://www.geeksforgeeks.org/how-to-get-user-location-in-android/
    @SuppressLint("MissingPermission")
    private void requestNewLocationData() {

        // Initializing LocationRequest object with appropriate methods
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(5);
        mLocationRequest.setFastestInterval(0);
        mLocationRequest.setNumUpdates(1);

        // setting LocationRequest on FusedLocationClient
       fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
       fusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
    }

    // This function is called when requestNewLocationData() is completed
    private LocationCallback mLocationCallback = new LocationCallback() {

        @Override
        public void onLocationResult(LocationResult locationResult) {
            Location lastKnownLocation = locationResult.getLastLocation();

            userLastKnownLocation = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
            fragmentForm.updateMap(userLastKnownLocation);
            Log.d("userLastKnownLocation", "User's last known location: " + userLastKnownLocation);
        }
    };

    // This function checks for the user has granted the necessary permissions to access location
    // for more info: https://www.geeksforgeeks.org/how-to-get-user-location-in-android/
    private boolean checkPermissions() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;

    }

    // This function requests the necessary permissions to access user's location
    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_ID);
    }

    // This function checks if the location is enabled on the device
    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    // This functions gets the user location after the permissions are given.
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
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

    public LatLng getUserLastKnownLocation() {


        return userLastKnownLocation;
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
