package com.example.angelmendez.cityreport;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

public class SubmitActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private MapView mMapView;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Location lastKnownLocation;
    private LatLng latLng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);


        setContentView(R.layout.submit_activity);

        mMapView = findViewById(R.id.map);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        getLastLocation();




    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng currentLocation = latLng;
        mMap.addMarker(new MarkerOptions().position(currentLocation).title("Current Location").draggable(false));

        float zoomLevel = 18.5f; //This goes up to 21
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, zoomLevel));

        mMap.getUiSettings().setScrollGesturesEnabled(false);
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

    public void updateMap(){
        mMapView.onCreate(null);
        mMapView.onResume();
        mMapView.getMapAsync(this);
    }

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
                        if (latLng == null)
                        {
                            Log.d("LOCATION", "onComplete: ");
                        }
                        Log.d("test", "onCreate: " + latLng);

                    } else {
                        Log.d("LOCATION", "Current location is null. Using defaults.");
                        Log.e("LOCATION", "Exception: %s", task.getException());

                    }

                   updateMap();
                }
            });

        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage(), e);
        }

    }
}