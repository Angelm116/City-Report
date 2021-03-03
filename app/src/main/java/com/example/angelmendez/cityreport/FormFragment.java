package com.example.angelmendez.cityreport;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;

import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.net.Uri;
import android.widget.Toast;

import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;


public class FormFragment extends Fragment implements PopupMenu.OnMenuItemClickListener, BackPressSupport{


    NestedScrollView scrollView;

    //Map
    MapView mMapView;
    private GoogleMap googleMap;
    Marker marker;

    // Report
    ReportObject reportObject;
    String date = null;
    String description = "";
    String category = "";
    String nearStreet = "";
    LatLng location = null;
    ArrayList<Bitmap> photoArray;
    LoadingDialog dialog;

    //RadioGroup
    RadioGroup radioGroup;
    HashMap<String, RadioButton> categoryLink; //given the title of a catergory, find the radiobutton object
    RadioButton selected;
    ArrayList<RadioButton> radioButtons;

    EditText descriptionInput;
    TextView pictureAttached;
    GridLayout grid;
    Button submitButton;
    
    //update report 
    ReportObject updateReport; 
    boolean isUpdate;




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.fragment_form, container, false);

//        Toolbar toolbar = rootView.findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);

        scrollView = rootView.findViewById(R.id.scrollFeed);

        dialog = new LoadingDialog(getActivity());

        grid = rootView.findViewById(R.id.grid);

        Button cameraButton = (Button) rootView.findViewById(R.id.add_picture_btn);
        submitButton = (Button) rootView.findViewById(R.id.submit_btn);

        photoArray = new ArrayList<>();
        pictureAttached = rootView.findViewById(R.id.picture_attached);
        pictureAttached.setVisibility(View.INVISIBLE);

        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                PopupMenu popup = new PopupMenu(getActivity().getApplicationContext(), v);
                popup.setOnMenuItemClickListener(FormFragment.this);
                popup.inflate(R.menu.add_photo_menu);
                popup.show();
            }
        });

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isUpdate)
                {
                    setNearStreet(location);
                   // updateReport();
                }
                else
                {
                    submitReport();
                }

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

        descriptionInput.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    InputMethodManager inputMethodManager =(InputMethodManager)getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        });


        radioGroup = rootView.findViewById(R.id.radioGroup);
        radioButtons = new ArrayList<>();
        radioButtons.add((RadioButton)rootView.findViewById(R.id.category_1));
        radioButtons.add((RadioButton)rootView.findViewById(R.id.category_2));
        radioButtons.add((RadioButton)rootView.findViewById(R.id.category_3));
        radioButtons.add((RadioButton)rootView.findViewById(R.id.category_4));

        categoryLink = new HashMap<>();
        for (int i = 0; i < 4; i++){
            categoryLink.put(radioButtons.get(i).getText().toString(), radioButtons.get(i));
        }

        radioGroup.setOnCheckedChangeListener( new RadioGroup.OnCheckedChangeListener() {

                                                  @Override
                                                  public void onCheckedChanged(RadioGroup group, int checkedId) {

                                                      RadioButton radioButton = rootView.findViewById(checkedId);

                                                      if (radioButton != null && radioButton.isChecked())
                                                      {
                                                          selected = radioButton;
                                                          category = radioButton.getText().toString();
                                                          radioButton.setTextColor(getResources().getColor(R.color.white));

                                                          for (int i = 0; i < 4; i++)
                                                          {
                                                              if(!(radioButtons.get(i) == radioButton))
                                                              {
                                                                  radioButtons.get(i).setTextColor(getResources().getColor(R.color.black));
                                                              }
                                                          }
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

            }
        });

        return rootView;
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


    public void populateForm(ReportObject report)
    {
        // set map to the location of the report
        // set radio button to the category
        // set description to what it is supposed to be
        // set photos text to the number it is supposed to be
        // set pictures on the bottom to what they are supposed to be

        isUpdate = true;

        updateReport = report;
        updateMap(report.getLocation());

        RadioButton select = categoryLink.get(report.getCategory());
        radioGroup.check(select.getId());
        category = report.getCategory();

        descriptionInput.setText(report.getDescription());
        description = report.getDescription();

        if (report.getPhotoArray() != null) {
            int photoArraySize = report.getPhotoArray().size();
            setPhotoCountText(photoArraySize);

            for (int i = 0; i < photoArraySize; i++) {
                addPhotoToGrid(report.getPhotoArray().get(i));
            }
        }

        submitButton.setText("Update");

    }

    public boolean onBackPressed() {

        resetForm();
        ((MainActivity) getActivity()).loadReportsFragment();

        return true;
    }

    public void clearCategories()
    {
        radioGroup.clearCheck();
        if (selected != null)
        {
            selected.setTextColor(getResources().getColor(R.color.black));
        }

    }

    public boolean categoryIsSelected(){
        if (selected == null)
        {
            Toast.makeText(getActivity(), "Must Select a Category",
                    Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    public void resetForm()
    {
         isUpdate = false;
         date = null;
         description = "";
         category = "";
         nearStreet = "";
         location = null;
         photoArray = null;

         scrollView.scrollTo(0, 0);
         descriptionInput.clearFocus();
         descriptionInput.setText("");
         grid.removeAllViews();
        pictureAttached.setVisibility(View.INVISIBLE);
        submitButton.setText("Submit");
        clearCategories();
        updateMap(((MainActivity) getActivity()).getLatLng());

    }


    public void saveReport()
    {
        if (categoryIsSelected())
       {

        reportObject = new ReportObject(nearStreet, date, photoArray, location, description, category);
           Log.d("save", nearStreet + "888");
        reportObject.saveToFile(getContext());
        Log.d("save", "saveReport: saved");

        ((MainActivity) getActivity()).loadReportsFragment();

        }


    }

    // not the best approach need to figure out how to call setnearstreet and wait for the result without stopping main execution
    public void submitReport()
    {
        setNearStreet(location);

    }

    public void updateReport()
    {
        Log.d("Method", "on FormFragment: updateReport()");

        //setNearStreet(location);

        updateReport.setDescription(description);
        updateReport.setCategory(category);
        updateReport.setLocation(location);
        updateReport.setNearStreet(nearStreet);
        // update file with new info.
        ReportObject.updateReportFile(getContext(), updateReport);

        ((MainActivity) getActivity()).loadReportsFragment();

        Log.d("Method", "out FormFragment: updateReport()");
    }

    public void setDate()
    {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR); // get the current year
        int month = cal.get(Calendar.MONTH) + 1; // month...
        int day = cal.get(Calendar.DAY_OF_MONTH); // current day in the month
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);
        String am_pm = cal.get(Calendar.AM_PM) == Calendar.AM ? "AM" : "PM";

        // sets your textview to e.g. 2012/03/15 for today

        String monthS = month < 10 ? ("0" + month) : ("" + month);
        String dayS = day < 10 ? ("0" + day) : ("" + day);
        String hourS = hour < 10 ? ("0" + hour) : ("" + hour);
        String minuteS = minute < 10 ? ("0" + minute) : ("" + minute);

        date = (monthS + "/" + dayS + "/" + year + "  at " + hourS + ":" + minuteS + " " + am_pm);

       // Log.d("TAG", date);
    }

    public void setNearStreet(LatLng latLng)
    {
        //Log.d("near", "setNearStreet: ");

        // Request neat street
        RequestQueue queue = Volley.newRequestQueue(getContext());
        String url = "https://maps.googleapis.com/maps/api/geocode/json?latlng=" + String.valueOf(latLng.latitude) + "," + String.valueOf(latLng.longitude) + "&key=AIzaSyAQw10ndgEutTniHm00lcLXAnZVbBFEweM";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new com.android.volley.Response.Listener<JSONObject>() {

                    // Process response
                    @Override
                    public void onResponse(JSONObject response) {

                         //  Log.d("near", response.toString());
                            //dialog.startLoadingDialog();
                            //processStreetResponse(response);

                        try {
                            String streetNumber = response.getJSONArray("results").getJSONObject(0).getJSONArray("address_components").getJSONObject(0).getString("short_name");
                            String streetName = response.getJSONArray("results").getJSONObject(0).getJSONArray("address_components").getJSONObject(1).getString("short_name");
                            String city = response.getJSONArray("results").getJSONObject(0).getJSONArray("address_components").getJSONObject(2).getString("short_name");
                            String state = response.getJSONArray("results").getJSONObject(0).getJSONArray("address_components").getJSONObject(4).getString("short_name");

                            nearStreet = streetNumber + " " + streetName + ", " + city + ", " + state;

                           // Log.d("near", nearStreet);

                            setDate();

                            if (isUpdate)
                            {
                                updateReport();
                            }
                            else
                            {
                                saveReport();
                            }

                            
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


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

    // Menu that appears when you click add photo, lets you choose between camera or gallery
    @Override
    public boolean onMenuItemClick(MenuItem item) {
        Toast.makeText(getActivity().getApplicationContext(), "Selected Item: " + item.getTitle(), Toast.LENGTH_SHORT).show();
        switch (item.getItemId()) {
            case R.id.from_camera:
                Intent takePicture = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(takePicture, 0);
                return true;
            case R.id.from_gallery:
                Intent pickPhoto = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(pickPhoto, 1);//one can be replaced with any action code
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        ArrayList<Bitmap> photoArrayHolder;

        if (isUpdate == true)
        {
            if (updateReport.getPhotoArray() == null)
            {
                updateReport.initializePhotoArray();
            }
            photoArrayHolder = updateReport.getPhotoArray();
        }
        else
        {
            if (photoArray == null)
            {
                photoArray = new ArrayList<>();
            }
            photoArrayHolder = photoArray;
        }



        if (resultCode != RESULT_CANCELED) {
            switch (requestCode) {
                case 0:  // from camera
                    if (resultCode == RESULT_OK && data != null) {
                        Bitmap selectedImage = (Bitmap) data.getExtras().get("data");
                        photoArrayHolder.add(selectedImage);
                        if (isUpdate){updateReport.addPhotoToBeAdded(selectedImage);}
                        pictureAttached.setVisibility(View.VISIBLE);
                    }

                    if (photoArrayHolder.size() == 1)
                    {
                        pictureAttached.setText("1 Picture Attached");
                    }
                    else
                    {
                        pictureAttached.setText(photoArrayHolder.size() + " Pictures Attached");
                    }

                    break;
                case 1: // from gallery
                    if (resultCode == RESULT_OK && data != null) {
                        Uri selectedImage = data.getData();
                        if (selectedImage != null) {

                            try {

                                photoArrayHolder.add(MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), selectedImage));

                                if (isUpdate)
                                {
                                    updateReport.addPhotoToBeAdded(photoArrayHolder.get(photoArrayHolder.size() - 1));
                                }

                            } catch (IOException e) {

                            }

                            setPhotoCountText(photoArrayHolder.size());

                            pictureAttached.setVisibility(View.VISIBLE);
                        }
                    }
                    break;
            }

            // update grid
            addPhotoToGrid(photoArrayHolder.get(photoArrayHolder.size() - 1));

        }
    }

    private int dpToPixel(float dp) {
        DisplayMetrics metrics = this.getResources().getDisplayMetrics();
        int px = (int)(dp * (metrics.densityDpi/160f));
        return px;
    }


    private void addPhotoToGrid(Bitmap ImageBitmap)
    {
        ImageView image = new ImageView(getContext());
        image.setImageBitmap(ImageBitmap);
        GridLayout.LayoutParams layoutParams = new GridLayout.LayoutParams(new ViewGroup.LayoutParams(dpToPixel(80), dpToPixel(100)));
        layoutParams.setMargins(dpToPixel(3), dpToPixel(3), dpToPixel(3), dpToPixel(3));
        image.setLayoutParams(layoutParams);
        grid.addView(image);
    }

    private void setPhotoCountText(int count)
    {
        if (count == 1)
        {
            pictureAttached.setText("1 Picture Attached");
        }
        else
        {
            pictureAttached.setText(count + " Picture Attached");
        }
    }


    }
