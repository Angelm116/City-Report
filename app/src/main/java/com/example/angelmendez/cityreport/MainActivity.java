package com.example.angelmendez.cityreport;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;


public class MainActivity extends AppCompatActivity implements locationCheckFragment.OnFragmentInteractionListener,
         DescriptionFragment.OnFragmentInteractionListener{


    CustomViewPager viewPager;
    SectionsPagerAdapter sectionsPagerAdapter;
    TabLayout tabIndicator;
    LatLng latLng;

    //overview: this app has 3 pages represented by fragments, enclosed in a tablayout, and controlled by a viewpager
    // page 1 or locationcheckfragment, gets the user's location, displays it, asks for confirmation and trasitions to the next page.
    // page 2 or complaintfragment, allows the user to choose a category for their complaint and moves on to the next page
    // page 3 or descriptionfragment, prompts the user for a description, allows them to add a picture and submit. STILL NEEDS WORK (A LOT lol)
    // the idea is that as the user provides us with info in the app, we store that information in an object, so that we can simply upload
    // that object to the server

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        // This code makes the activity on full screen
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);


        // this code inflates the layout of the mainactivity
        setContentView(R.layout.activity_intro);


        // This code initializes the variables
        tabIndicator = findViewById(R.id.tab_indicator);              // this is the dots in the bottom left corner



        // setup viewpager; The ViewPager is like a container for all the fragment; it is in
        // charge of transitioning the view of the page from fragment to fragment, hence "viewpager".
        // The PagerAdapter tells the viewpager which fragment to display
        viewPager =findViewById(R.id.screen_viewpager);
        sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        viewPager.setAdapter(sectionsPagerAdapter);

        viewPager.setPagingEnabled(false);


        // setup tablayout with viewpager, so that the tab layout reacts to the change in pages
        // The tablayout is basically the layout where all of this is taking place, it includes
        // the three dots on the bottom and their animation and it allows us to swipe among pages
        tabIndicator.setupWithViewPager(viewPager);

        // this method is for the maps portion of the app
        getLastLocation();
    }

    // This function gets executed whenever the user chooses a complaint in the
    // complaint fragment; IT STILL NEEDS WORK
    // this function is supposed to add the users selection (what complaint) to
    // the object that packs all the info that we will send to the server
    // also it needs to show the last screen
    public void complaintButton(final View view)
    {
        viewPager.setCurrentItem(2);// sets screen to last screen
    }

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
        // Get last known recent location using new Google Play Services SDK (v11+)
        FusedLocationProviderClient locationClient = getFusedLocationProviderClient(this);

        locationClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // GPS location can be null if GPS is switched off
                        if (location != null) {
                            latLng = new LatLng(location.getLatitude(), location.getLongitude());
                            //onLocationChanged(location);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("MapDemoActivity", "Error trying to get last GPS location");
                        e.printStackTrace();
                    }
                });
    }

    // you can ignore this
    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
