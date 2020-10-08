package com.example.angelmendez.cityreport;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.util.Calendar;


public class FormFragment extends Fragment {

    MapView mMapView;
    private GoogleMap googleMap;
    TextView pictureAttached;
    ReportObject reportObject;

    String date = null;
    String description = "";
    String category = "";
    String nearStreet = "";
    LatLng location = null;
    Bitmap photo = null;
    LoadingDialog dialog;
    Marker marker;
    RadioGroup radioGroup;
    EditText descriptionInput;
    NestedScrollView scrollView;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.fragment_form, container, false);

//        Toolbar toolbar = rootView.findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);

        scrollView = rootView.findViewById(R.id.scrollFeed);

        dialog = new LoadingDialog(getActivity());

        Button cameraButton = (Button) rootView.findViewById(R.id.add_picture_btn);
        Button submitButton = (Button) rootView.findViewById(R.id.submit_btn);

        pictureAttached = rootView.findViewById(R.id.picture_attached);
        pictureAttached.setVisibility(View.INVISIBLE);

        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, 0);
            }
        });

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              //  Log.d("TAG", "onClick: ");
                //System.exit(0);
                setNearStreet(location);


               // date = DateFormat.getDateInstance().format(DareFormat);


            }
        });


        descriptionInput = rootView.findViewById(R.id.description_input);
        descriptionInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                description = s.toString();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        radioGroup = rootView.findViewById(R.id.radioGroup);
        radioGroup.setOnCheckedChangeListener( new RadioGroup.OnCheckedChangeListener() {
                                                  @Override
                                                  public void onCheckedChanged(RadioGroup group, int checkedId) {
                                                      RadioButton radioButton = rootView.findViewById(checkedId);

                                                      if (radioButton != null && radioButton.isChecked())
                                                      {
                                                          category = radioButton.getText().toString();
                                                      }
                                                  }
                                              }
        );



        mMapView = (MapView) rootView.findViewById(R.id.map);
        mMapView.onCreate(savedInstanceState);

        mMapView.onResume(); // needed to get the map to display immediately

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap mMap) {
                googleMap = mMap;
//
//                // For showing a move to my location button
//                if (ActivityCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//                    // TODO: Consider calling
//                    //    ActivityCompat#requestPermissions
//                    // here to request the missing permissions, and then overriding
//                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//                    //                                          int[] grantResults)
//                    // to handle the case where the user grants the permission. See the documentation
//                    // for ActivityCompat#requestPermissions for more details.
//                    return;
//                }
//                googleMap.setMyLocationEnabled(true);
//
//                // For dropping a marker at a point on the Map
//                LatLng sydney = new LatLng(-34, 151);
//                googleMap.addMarker(new MarkerOptions().position(sydney).title("Marker Title").snippet("Marker Description"));
//
//                // For zooming automatically to the location of the marker
//                CameraPosition cameraPosition = new CameraPosition.Builder().target(sydney).zoom(12).build();
//                googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                //new LatLng(-34, 151);

//                try {
//                    Thread.sleep(5000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }

//                LatLng currentLocation = ((MainActivity) getActivity()).getLatLng();
//                mMap.addMarker(new MarkerOptions().position(currentLocation).title("Current Location").draggable(false));
//
//                float zoomLevel = 18.5f; //This goes up to 21
//                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, zoomLevel));
//
//                mMap.getUiSettings().setAllGesturesEnabled(false);

            }
        });

        return rootView;
    }

    public void resetForm()
    {
         date = null;
         description = "";
         category = "";
         nearStreet = "";
         location = null;
         photo = null;

         scrollView.scrollTo(0, 0);
         descriptionInput.clearFocus();
         descriptionInput.setText("");

        pictureAttached.setVisibility(View.INVISIBLE);
         updateMap(((MainActivity) getActivity()).getLatLng());
    }


    public void saveReport()
    {
        reportObject = new ReportObject(nearStreet, date, photo, location, description, category);
        reportObject.saveToFile(getContext());
        Log.d("save", "saveReport: saved");
        radioGroup.clearCheck();
        ((MainActivity) getActivity()).loadReportsFragment();




    }

    public void setNearStreetFinished(JSONObject response){
        try {
            //nearStreet = response.getJSONArray("results").getJSONObject(0).getString("formatted_address");

            String streetNumber = response.getJSONArray("results").getJSONObject(0).getJSONArray("address_components").getJSONObject(0).getString("short_name");
            String streetName = response.getJSONArray("results").getJSONObject(0).getJSONArray("address_components").getJSONObject(1).getString("short_name");
            String city = response.getJSONArray("results").getJSONObject(0).getJSONArray("address_components").getJSONObject(2).getString("short_name");
            String state = response.getJSONArray("results").getJSONObject(0).getJSONArray("address_components").getJSONObject(4).getString("short_name");

            nearStreet = streetNumber + " " + streetName + ", " + city + ", " + state;

            Log.d("near", nearStreet);

            Calendar cal = Calendar.getInstance();
            int year = cal.get(Calendar.YEAR); // get the current year
            int month = cal.get(Calendar.MONTH); // month...
            int day = cal.get(Calendar.DAY_OF_MONTH); // current day in the month

            // sets your textview to e.g. 2012/03/15 for today
            date = (month + "/" + day + "/" + year);

            Log.d("TAG", date);

            dialog.dismissDialog();
            saveReport();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setNearStreet(LatLng latLng)
    {
        //Log.d("near", "setNearStreet: ");
        RequestQueue queue = Volley.newRequestQueue(getContext());
        String url = "https://maps.googleapis.com/maps/api/geocode/json?latlng=" + String.valueOf(latLng.latitude) + "," + String.valueOf(latLng.longitude) + "&key=AIzaSyAQw10ndgEutTniHm00lcLXAnZVbBFEweM";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new com.android.volley.Response.Listener<JSONObject>() {



                    @Override
                    public void onResponse(JSONObject response) {

                           // Log.d("near", response.toString());
                            dialog.startLoadingDialog();
                            setNearStreetFinished(response);





                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO: Handle error

                        Log.d("near", "didnt work2");

                    }
                });

        queue.add(jsonObjectRequest);

    }


    public void updateMap(LatLng lat) {

        ((MainActivity) getActivity()).setLocationHolder(lat);

        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap mMap) {
                googleMap = mMap;

                Log.d("update", "onMapReady: on update");


                location = ((MainActivity) getActivity()).getLocationHolder();

                if (marker != null)
                {
                    marker.remove();
                }

                marker = googleMap.addMarker(new MarkerOptions().position(location).title("Current Location").draggable(false));

                float zoomLevel = 18.5f; //This goes up to 21
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, zoomLevel));

                googleMap.getUiSettings().setAllGesturesEnabled(false);

            }
        });


    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        photo = (Bitmap)data.getExtras().get("data");

        //imageView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));


        pictureAttached.setVisibility(View.VISIBLE);


    }
}