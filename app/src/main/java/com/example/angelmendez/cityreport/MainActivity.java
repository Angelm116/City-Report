package com.example.angelmendez.cityreport;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;


public class MainActivity extends AppCompatActivity implements locationCheckFragment.OnFragmentInteractionListener,
        DescriptionFragment.OnFragmentInteractionListener, NavigationView.OnNavigationItemSelectedListener {


    CustomViewPager viewPager;
    SectionsPagerAdapter sectionsPagerAdapter;
    TabLayout tabIndicator;
    LatLng latLng;
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    androidx.appcompat.widget.Toolbar toolbar;

    private FusedLocationProviderClient fusedLocationProviderClient;
    private Location lastKnownLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        // This code makes the activity on full screen
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // this code inflates the layout of the mainactivity
//        setContentView(R.layout.activity_intro);
        setContentView(R.layout.submit_activity);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        getLastLocation();

//        drawerLayout = findViewById(R.id.drawer_layout);
//        navigationView = findViewById(R.id.nav_view);
//        toolbar = findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//        navigationView.bringToFront();
//        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
//        drawerLayout.addDrawerListener(toggle);
//        toggle.setDrawerIndicatorEnabled(true);
//        toggle.syncState();
//        navigationView.setNavigationItemSelectedListener(this);


        // This code initializes the variables
//        tabIndicator = findViewById(R.id.tab_indicator);              // this is the dots in the bottom left corner


        // setup viewpager; The ViewPager is like a container for all the fragment; it is in
        // charge of transitioning the view of the page from fragment to fragment, hence "viewpager".
        // The PagerAdapter tells the viewpager which fragment to display

//        viewPager = findViewById(R.id.screen_viewpager);
//        sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
//        viewPager.setAdapter(sectionsPagerAdapter);
//
//        viewPager.setPagingEnabled(false);


        // setup tablayout with viewpager, so that the tab layout reacts to the change in pages
        // The tablayout is basically the layout where all of this is taking place, it includes
        // the three dots on the bottom and their animation and it allows us to swipe among pages

//        tabIndicator.setupWithViewPager(viewPager);

        // this method is for the maps portion of the app


    }

    // This function gets executed whenever the user chooses a complaint in the
    // complaint fragment; IT STILL NEEDS WORK
    // this function is supposed to add the users selection (what complaint) to
    // the object that packs all the info that we will send to the server
    // also it needs to show the last screen


    // the purpose of this method is to be called from the locationCheckfragment
    // it acts as an intermediate between getlastLocation() and locationcheckfragment
    public LatLng getLatLng() {

        getLastLocation();
        return latLng;
    }

    // the methods gets the required permissions for the location and gets the current location
    // honestly this method miraculously started working once I gave up on it... so just know that it works
    // for now, no need worry about it
    public void getLastLocation() {

        getLocationPermission();
        // Get last known recent location using new Google Play Services SDK (v11+)
        FusedLocationProviderClient locationClient = getFusedLocationProviderClient(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d("PERMISSION", "getLastLocation: PERMISSION NOT GRANTED ");

            return;
        }

        try {

                Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            lastKnownLocation = task.getResult();
                            latLng = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());

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

    // you can ignore this
    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public void onBackPressed(){
        if(drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.nav_home:
                break;
            case R.id.drafts:
                Intent draftsIntent = new Intent(MainActivity.this,MainActivity.class);
                startActivity(draftsIntent);
                break;
            case R.id.history:
                Intent historyIntent = new Intent(MainActivity.this,MainActivity.class);
                startActivity(historyIntent);
                break;
            case R.id.share:
                Toast.makeText(this, "Share", Toast.LENGTH_SHORT).show();
                break;
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    // [START maps_current_place_location_permission]
    private void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.INTERNET},
                    1);
            // Permission is not granted
            Log.d("MapDemoActivity", "Internet permission not granted");

        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    2);
            Log.d("MapDemoActivity", "Fine permission not granted");

        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    3);
            Log.d("MapDemoActivity", "Coarse permission not granted");

        }
    }

    public void submit(View view) {
        finish();
    }
}
