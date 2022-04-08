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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;


public class FragmentForm extends Fragment implements PopupMenu.OnMenuItemClickListener, BackPressSupport{



    // Map related variables
    MapView mMapView;
    Marker marker;

    // Report Fields
    Calendar date = null;
    LatLng markerLocation = null;
    ReportLocation locationObject;
    ReportObject currentReport;
    boolean isUpdate;

    //RadioGroup variables
    RadioGroup radioGroup;
    HashMap<String, RadioButton> categoryLink; //given the title of a category, find the radiobutton object
    RadioButton selected;
    ArrayList<RadioButton> radioButtons;

    // other views
    EditText descriptionInput;
    TextView pictureAttached;
    GridLayout grid;
    Button submitButton;
    Button addPictureButton;
    NestedScrollView scrollView;
    LoadingDialog dialog;

    View rootView;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View root = inflater.inflate(R.layout.fragment_form, container, false);

        // Load views
        this.rootView = root;
        scrollView = rootView.findViewById(R.id.scrollFeed);
        grid = rootView.findViewById(R.id.grid);
        addPictureButton = (Button) rootView.findViewById(R.id.add_picture_btn);
        submitButton = (Button) rootView.findViewById(R.id.submit_btn);
        pictureAttached = rootView.findViewById(R.id.picture_attached);
        radioGroup = rootView.findViewById(R.id.radioGroup);
        descriptionInput = rootView.findViewById(R.id.description_input);
        dialog = new LoadingDialog(getActivity());

        currentReport = new ReportObject(getContext());


        // Make the attached pictures counter invisible until there are attached pictures
        pictureAttached.setVisibility(View.INVISIBLE);


        // Listener for addPictureButton
        // Any time the addPictureButton is clicked, display the menu with the options to add pictures
        addPictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                PopupMenu popup = new PopupMenu(getActivity().getApplicationContext(), v);
                popup.setOnMenuItemClickListener(FragmentForm.this);
                popup.inflate(R.menu.form_menu_add_photo);
                popup.show();
            }
        });


        // Listener for submitButton
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isUpdate)
                {
                    updateReport(); //update report
                }
                else
                {
                    saveReport(); // save report
                }
            }
        });


        // Listener for text changes in the the description input
        descriptionInput.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            // Whenever the user changes the text, override the current value of description with the changes
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                currentReport.setDescription(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }

        });


        // Focus listener for description
        // This ensures that if the description input is not in focus, the keyboard is hidden
        descriptionInput.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    InputMethodManager inputMethodManager =(InputMethodManager)getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        });


        // Listener for changes in which radio button is checked with the category Radio Group
        // Changes the color of the text in the radio button when its checked
        radioGroup.setOnCheckedChangeListener(
                new RadioGroup.OnCheckedChangeListener() {

                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId) {

                        RadioButton radioButton = rootView.findViewById(checkedId);

                        if (radioButton != null && radioButton.isChecked())
                        {

                            selected = radioButton;

                            currentReport.setCategory(radioButton.getText().toString());

                            // Set the color of the checked radio button to white
                            radioButton.setTextColor(getResources().getColor(R.color.white));

                            // Set the color of all the other radioButtons to black
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


        // Set up radio buttons
        // Create programmatic representations of the radio buttons
        radioButtons = new ArrayList<>();
        radioButtons.add((RadioButton)rootView.findViewById(R.id.category_1));
        radioButtons.add((RadioButton)rootView.findViewById(R.id.category_2));
        radioButtons.add((RadioButton)rootView.findViewById(R.id.category_3));
        radioButtons.add((RadioButton)rootView.findViewById(R.id.category_4));

        // Use a HashMap to link each radio group with their category, so that
        // given a category, we can identify the corresponding radio button
        categoryLink = new HashMap<>();
        for (int i = 0; i < 4; i++){
            categoryLink.put(radioButtons.get(i).getText().toString(), radioButtons.get(i));
        }


        // Map setup
        mMapView = (MapView) rootView.findViewById(R.id.fragment_form_MapView);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume(); // needed to get the map to display immediately

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }


        return rootView;
    }


    // This function populates the form with the information of a given report
    public void populateForm(ReportObject report)
    {

        isUpdate = true;
        currentReport = report;

        // Set map marker to point to the report's location
        updateMap(currentReport.getLatLng());

        // Check the radio button that matches the category of the report
        RadioButton select = categoryLink.get(currentReport.getCategory());
        radioGroup.check(select.getId());

        // Set the description text to the description of the report
        descriptionInput.setText(currentReport.getDescription());


        // Fill the grid with the report's photos
        if (currentReport.getPhotoArray() != null) {

            int photoArraySize = currentReport.getPhotoArray().size();
            setPhotoCountText(photoArraySize);

            for (int i = 0; i < photoArraySize; i++) {
                addPhotoToGrid(currentReport.getPhotoArray().get(i));
            }
        }

        // Change title of submit button to update
        submitButton.setText("Update");

    }

    // This function resets the form and loads the home fragment when the user clicks the back button
    public boolean onBackPressed() {

        resetForm();
        ((MainActivity) getActivity()).loadHomeFragment();
        return true;
    }


    // This function resets the form to its original state
    public void resetForm()
    {
        // Reset variables
         isUpdate = false;
         date = null;
         locationObject = null;
         markerLocation = null;

         scrollView.scrollTo(0, 0);
         descriptionInput.clearFocus();
         descriptionInput.setText("");
         grid.removeAllViews();
         pictureAttached.setVisibility(View.INVISIBLE);
         submitButton.setText("Submit");

         currentReport = new ReportObject(getContext());

        // Uncheck all radio buttons
        radioGroup.clearCheck();
        if (selected != null) // if one was selected, reset its text color to black
        {
            selected.setTextColor(getResources().getColor(R.color.black));
        }

        // Update the map with the user's current location
        updateMap(((MainActivity) getActivity()).getUserLastKnownLocation());

    }

    // This function saves the report to file storage
    public void saveReport()
    {
        currentReport.setDate(Calendar.getInstance());

        // Check that the user selected a category
        if (selected == null) { // Tell user to select a category

            Toast.makeText(getActivity(), "Must Select a Category",
                    Toast.LENGTH_LONG).show();
        }
        else { // Proceed with saving the report

            date = Calendar.getInstance();

            currentReport.setDate(Calendar.getInstance());
            currentReport.uploadToServer(getContext());
            //currentReport.saveToFile();
            ReportObject.saveToFile(currentReport);

            // Load home fragment
            ((MainActivity) getActivity()).loadHomeFragment();
        }
    }


    public void updateReport()
    {
        Log.d("PHOTOLENGTH", "" + currentReport.getPhotoArray().size());
        // update file with new info.
        ReportObject.saveToFile(currentReport);

        // load home fragment
        ((MainActivity) getActivity()).loadHomeFragment();
    }


    public void updateMap(final LatLng markerLocation) {

        mMapView.getMapAsync(new OnMapReadyCallback() {

            @Override
            public void onMapReady(GoogleMap mMap) {

                GoogleMap googleMap = mMap;
                FragmentForm.this.markerLocation = markerLocation;

                if (marker != null)
                {
                    marker.remove();
                }

                marker = googleMap.addMarker(new MarkerOptions().position(FragmentForm.this.markerLocation).title("Current Location").draggable(false));

                float zoomLevel = 18.0f; //This goes up to 21
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(FragmentForm.this.markerLocation, zoomLevel));

                googleMap.getUiSettings().setAllGesturesEnabled(false);
                googleMap.getUiSettings().setMapToolbarEnabled(false);
            }
        });


    }

    // Menu that appears when you click add photo, lets you choose between camera or gallery
    @Override
    public boolean onMenuItemClick(MenuItem item) {
        Toast.makeText(getActivity().getApplicationContext(), "Selected Item: " + item.getTitle(), Toast.LENGTH_SHORT).show();
        switch (item.getItemId()) {
            case R.id.from_camera: // Choose from camera
                Intent takePicture = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(takePicture, 0);
                return true;
            case R.id.from_gallery: // Choose from gallery
                Intent pickPhoto = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(pickPhoto, 1);//one can be replaced with any action code
                return true;
            default:
                return false;
        }
    }

    // This function gets triggered when the intents of the add-photo-menu finish their processes
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        // if currentReport's photoarray is null, initalize it
        if(currentReport.getPhotoArray() == null)
        {
            currentReport.setPhotoArray(new ArrayList<Bitmap>());
        }

        // Add the new photo to currentReport's photoArray
        if (resultCode != RESULT_CANCELED) {
            switch (requestCode) {
                case 0:  // from camera

                    if (resultCode == RESULT_OK && data != null) {

                        Bitmap selectedImage = (Bitmap) data.getExtras().get("data");
                        currentReport.getPhotoArray().add(selectedImage);
                    }

                    break;
                case 1: // from gallery
                    if (resultCode == RESULT_OK && data != null) {
                        Uri selectedImage = data.getData();
                        if (selectedImage != null) {
                            try {

                                currentReport.getPhotoArray().add(MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), selectedImage));

                            } catch (IOException e) {

                            }
                        }
                    }
                    break;
            }

            //Set the photoCountText to the new count
            setPhotoCountText(currentReport.getPhotoArray().size());
            // Add the photo to the grid
            addPhotoToGrid(currentReport.getPhotoArray().get(currentReport.getPhotoArray().size() - 1));

        }
    }

    // This functions calculates px from dp
    private int dpToPixel(float dp) {
        DisplayMetrics metrics = this.getResources().getDisplayMetrics();
        int px = (int)(dp * (metrics.densityDpi/160f));
        return px;
    }


    // This function add a photo to the grid of a report and sets the onclick listener for the photo to be deleted if it is long pressed.
    private void addPhotoToGrid(Bitmap ImageBitmap)
    {
        //Create imageview and set the passed bitmap as its image
        final ImageView image = new ImageView(getContext());
        image.setImageBitmap(ImageBitmap);
        GridLayout.LayoutParams layoutParams = new GridLayout.LayoutParams(new ViewGroup.LayoutParams(dpToPixel(80), dpToPixel(100)));

        // set the dimensions of the imageview
        layoutParams.setMargins(dpToPixel(3), dpToPixel(3), dpToPixel(3), dpToPixel(3));
        image.setLayoutParams(layoutParams);

        // set OnLongClickListner for the imageview that DELETES IMAGES FROM GRID AND REPORTOBJECT
        image.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                // get position of the photo to be deleted
                int position = grid.indexOfChild(image);

                // delete from current report object
                currentReport.getPhotoArray().remove(position);

                // delete from grid
                grid.removeViewAt(position);

                setPhotoCountText(grid.getChildCount());

                return true;
            }
        });

        // Add imageview to the grid
        grid.addView(image);
    }

    // This function sets the attached photo counter
    private void setPhotoCountText(int count)
    {
        if (count == 0) // if the count is zero, dont show text
        {
            pictureAttached.setVisibility(View.INVISIBLE);
        }
        else if(count == 1)
        {
            pictureAttached.setVisibility(View.VISIBLE);
            pictureAttached.setText("1 Picture Attached");
        }
        else
        {
            pictureAttached.setVisibility(View.VISIBLE);
            pictureAttached.setText(count + " Pictures Attached");
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


    }
