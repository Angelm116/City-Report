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
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;


public class FragmentForm extends Fragment implements PopupMenu.OnMenuItemClickListener, BackPressSupport{


    NestedScrollView scrollView;

    //Map
    MapView mMapView;
    private GoogleMap googleMap;
    Marker marker;

    // Report
    ReportObject reportObject;
    Calendar date = null;
    String description = "";
    String category = "";
    String nearStreet = "";
    LatLng location = null;
    ArrayList<Bitmap> photoArray;
    LoadingDialog dialog;
    ReportLocation locationObject;

    //RadioGroup
    RadioGroup radioGroup;
    HashMap<String, RadioButton> categoryLink; //given the title of a catergory, find the radiobutton object
    RadioButton selected;
    ArrayList<RadioButton> radioButtons;

    EditText descriptionInput;
    TextView pictureAttached;
    GridLayout grid;
    Button submitButton;
    Button cameraButton;
    
    //update report 
    ReportObject updateReport; 
    boolean isUpdate;

    View rootView;




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View root = inflater.inflate(R.layout.fragment_form, container, false);

        this.rootView = root;
        scrollView = rootView.findViewById(R.id.scrollFeed);
        grid = rootView.findViewById(R.id.grid);
        cameraButton = (Button) rootView.findViewById(R.id.add_picture_btn);
        submitButton = (Button) rootView.findViewById(R.id.submit_btn);
        pictureAttached = rootView.findViewById(R.id.picture_attached);
        radioGroup = rootView.findViewById(R.id.radioGroup);
        descriptionInput = rootView.findViewById(R.id.description_input);
        mMapView = (MapView) rootView.findViewById(R.id.map);

        dialog = new LoadingDialog(getActivity());


        photoArray = new ArrayList<>();
        pictureAttached.setVisibility(View.INVISIBLE);


        setUpGrid();
        setUpCameraListener();
        setUpSubmitListener();
        setUpDescriptionListeners();
        setUpRadioButtons();
        setUpMap(savedInstanceState);
        setUpRadioGroupListener();

        return rootView;
    }

    private void setUpGrid()
    {

    }

    private void setUpCameraListener()
    {
        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                PopupMenu popup = new PopupMenu(getActivity().getApplicationContext(), v);
                popup.setOnMenuItemClickListener(FragmentForm.this);
                popup.inflate(R.menu.add_photo_menu);
                popup.show();
            }
        });
    }
    private void setUpSubmitListener()
    {
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isUpdate)
                {
                    setLocationDetails(location);
                    // updateReport();
                }
                else
                {
                    submitReport();
                }

            }
        });
    }
    private void setUpDescriptionListeners()
    {
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
    }

    private void setUpRadioGroupListener()
    {
        radioGroup.setOnCheckedChangeListener(
                new RadioGroup.OnCheckedChangeListener() {

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
    }

    private void setUpMap(Bundle savedInstanceState)
    {
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
    }

    private void setUpRadioButtons()
    {
        radioButtons = new ArrayList<>();
        radioButtons.add((RadioButton)rootView.findViewById(R.id.category_1));
        radioButtons.add((RadioButton)rootView.findViewById(R.id.category_2));
        radioButtons.add((RadioButton)rootView.findViewById(R.id.category_3));
        radioButtons.add((RadioButton)rootView.findViewById(R.id.category_4));

        categoryLink = new HashMap<>();
        for (int i = 0; i < 4; i++){
            categoryLink.put(radioButtons.get(i).getText().toString(), radioButtons.get(i));
        }
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
         locationObject = null;
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

        reportObject = new ReportObject(locationObject, date, photoArray, description, category);
        reportObject.uploadToServer(getContext());
        reportObject.saveToFile(getContext());
        Log.d("save", "saveReport: saved");

        ((MainActivity) getActivity()).loadReportsFragment();

        }


    }

    // not the best approach need to figure out how to call setnearstreet and wait for the result without stopping main execution
    public void submitReport()
    {
        setDate();
        setLocationDetails(location);

    }

    public void updateReport()
    {
        Log.d("Method", "on FormFragment: updateReport()");

        //setNearStreet(location);

        updateReport.setDescription(description);
        updateReport.setCategory(category);
        updateReport.setLocation(location);
        updateReport.setLocationObject(locationObject);
        // update file with new info.
        ReportObject.updateReportFile(getContext(), updateReport);

        ((MainActivity) getActivity()).loadReportsFragment();

        Log.d("Method", "out FormFragment: updateReport()");
    }

    public void setDate()
    {
        date = Calendar.getInstance();
    }

    public void setLocationDetails(final LatLng latLng)
    {
        //Log.d("near", "setNearStreet: ");

        // Request neat street
        RequestQueue queue = VolleySingleton.getInstance(getContext()).getQueue();
        String url = "https://maps.googleapis.com/maps/api/geocode/json?latlng=" + String.valueOf(latLng.latitude) + "," + String.valueOf(latLng.longitude) + "&key=AIzaSyAQw10ndgEutTniHm00lcLXAnZVbBFEweM";



        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new com.android.volley.Response.Listener<JSONObject>() {

                    // Process response
                    @Override
                    public void onResponse(JSONObject response) {

                        try {
                          Log.d("TAG", response.toString(5));
//                            String country = response.getJSONArray("results").getJSONObject(1).getJSONArray("address_components").getJSONObject(5).getString("long_name");
//                            String state = response.getJSONArray("results").getJSONObject(1).getJSONArray("address_components").getJSONObject(4).getString("short_name");
//                            String county = response.getJSONArray("results").getJSONObject(1).getJSONArray("address_components").getJSONObject(3).getString("short_name");
//                            String zipCode = response.getJSONArray("results").getJSONObject(1).getJSONArray("address_components").getJSONObject(6).getString("short_name");
//                            String city = response.getJSONArray("results").getJSONObject(1).getJSONArray("address_components").getJSONObject(2).getString("short_name");
//                            String streetNumber = response.getJSONArray("results").getJSONObject(1).getJSONArray("address_components").getJSONObject(0).getString("short_name");
//                            String streetName = response.getJSONArray("results").getJSONObject(1).getJSONArray("address_components").getJSONObject(1).getString("short_name");


                            ArrayList<String> details = new ArrayList<>();
                            Map map = new HashMap<String, Integer>();

                            for (int i = 0; i < 7; i++)
                            {
                                details.add("");
                            }

                            map.put("country", 0);
                            map.put("administrative_area_level_1", 1);
                            map.put("administrative_area_level_2", 2);
                            map.put("sublocality", 3);
                            map.put("sublocality_level_1", 3);
                            map.put("postal_code", 4);
                            map.put("street_number", 5);
                            map.put("route", 6);

//        this.country = country;
//        this.state = state
//        this.county = county;
//        this.city = city;
//        this.zip = zip;
//        this.streetNumber = streetNumber;
//        this.streetName = streetName;
//        this.latitude = latitude;
//        this.longitude = longitude;

                            JSONArray responseArray = response.getJSONArray("results").getJSONObject(1).getJSONArray("address_components");
                            JSONObject objectHolder;
                            JSONArray typesHolder;
                            String idHolder;
                            String detailHolder;
                            int reponseSize = responseArray.length();
                            for (int i = 0; i < reponseSize; i++)
                            {
                                objectHolder = responseArray.getJSONObject(i);
                                typesHolder = objectHolder.getJSONArray("types");

                                for (int j = 0; j < typesHolder.length(); j++)
                                {
                                    idHolder = typesHolder.getString(j);

                                    if (map.containsKey(idHolder))
                                    {
                                        detailHolder = objectHolder.getString("short_name");
                                        details.set((int)map.get(idHolder), detailHolder);
                                        break;
                                    }
                                }
                            }

                            locationObject = new ReportLocation(details, latLng.latitude, latLng.longitude);


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
        final ImageView image = new ImageView(getContext());
        image.setImageBitmap(ImageBitmap);
        GridLayout.LayoutParams layoutParams = new GridLayout.LayoutParams(new ViewGroup.LayoutParams(dpToPixel(80), dpToPixel(100)));
        layoutParams.setMargins(dpToPixel(3), dpToPixel(3), dpToPixel(3), dpToPixel(3));
        image.setLayoutParams(layoutParams);

        image.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                // find out which image is this and delete from photoArray
                // if the report

                int position = grid.indexOfChild(image);

                if (isUpdate)
                {
                    //figure out the name of the imagefile
                    // add image to the tobedeleted array of the updateReport
                     int photoArraySize = updateReport.getPhotoArray().size();

                     if (position >= photoArraySize)
                     {
                         updateReport.getPhotosToAdd().remove(position - photoArraySize);
                     }
                     else
                     {

                         if( updateReport.getPhotoFileNames().size() == 0) // you added a photo but havent saved
                         {
                             updateReport.getPhotosToAdd().remove(position);
                             updateReport.setPhotosToAdd(null);
                         }
                         else
                         {
                             updateReport.addPhotoToBeDeleted(updateReport.getPhotoFileNames().get(position));
                             updateReport.getPhotoFileNames().remove(position);
                             updateReport.getPhotoArray().remove(position);
                         }

                     }
                     // get the name of the file
                                // get array holding the photos of the report
                    // remove view from grid and update grid
                    // update 

                }
                else
                {
                    photoArray.remove(position);
                }

                grid.removeViewAt(position);
                setPhotoCountText(grid.getChildCount());


                return true;
            }
        });

        grid.addView(image);
    }

    private void setPhotoCountText(int count)
    {
        if (count == 0)
        {
            pictureAttached.setVisibility(View.INVISIBLE);
        }
        else if(count == 1)
        {
            pictureAttached.setText("1 Picture Attached");
        }
        else
        {
            pictureAttached.setText(count + " Picture Attached");
        }
    }


    }
