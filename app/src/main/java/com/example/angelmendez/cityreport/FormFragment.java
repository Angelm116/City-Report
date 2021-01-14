package com.example.angelmendez.cityreport;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;

import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
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
import java.util.ArrayList;
import java.util.Calendar;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;


public class FormFragment extends Fragment implements PopupMenu.OnMenuItemClickListener{

    MapView mMapView;
    private GoogleMap googleMap;
    TextView pictureAttached;
    ReportObject reportObject;

    String date = null;
    String description = "";
    String category = "";
    String nearStreet = "";
    LatLng location = null;
    ArrayList<Bitmap> photoArray;
    LoadingDialog dialog;
    Marker marker;
    RadioGroup radioGroup;
    EditText descriptionInput;
    NestedScrollView scrollView;
    RadioButton selected;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.fragment_form, container, false);

//        Toolbar toolbar = rootView.findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);

        scrollView = rootView.findViewById(R.id.scrollFeed);

        dialog = new LoadingDialog(getActivity());

        Button cameraButton = (Button) rootView.findViewById(R.id.add_picture_btn);
        Button submitButton = (Button) rootView.findViewById(R.id.submit_btn);

        photoArray = new ArrayList<>();
        pictureAttached = rootView.findViewById(R.id.picture_attached);
        pictureAttached.setVisibility(View.INVISIBLE);

        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//                startActivityForResult(intent, 0);

                PopupMenu popup = new PopupMenu(getActivity().getApplicationContext(), v);
                popup.setOnMenuItemClickListener(FormFragment.this);
                popup.inflate(R.menu.popup_menu);
                popup.show();
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
        final ArrayList<RadioButton> radioButtons = new ArrayList<>();
        radioButtons.add((RadioButton)rootView.findViewById(R.id.category_1));
        radioButtons.add((RadioButton)rootView.findViewById(R.id.category_2));
        radioButtons.add((RadioButton)rootView.findViewById(R.id.category_3));
        radioButtons.add((RadioButton)rootView.findViewById(R.id.category_4));

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

    public void resetForm()
    {
         date = null;
         description = "";
         category = "";
         nearStreet = "";
         location = null;
         photoArray = null;

         scrollView.scrollTo(0, 0);
         descriptionInput.clearFocus();
         descriptionInput.setText("");

        pictureAttached.setVisibility(View.INVISIBLE);
         updateMap(((MainActivity) getActivity()).getLatLng());
    }


    public void saveReport()
    {
        reportObject = new ReportObject(nearStreet, date, photoArray, location, description, category);
        reportObject.saveToFile(getContext());
        Log.d("save", "saveReport: saved");
        radioGroup.clearCheck();
        selected.setTextColor(getResources().getColor(R.color.black));
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
            int hour = cal.get(Calendar.HOUR_OF_DAY);
            int minute = cal.get(Calendar.MINUTE);
            String am_pm = cal.get(Calendar.AM_PM) == Calendar.AM ? "AM" : "PM";

            // sets your textview to e.g. 2012/03/15 for today
            date = (month + "/" + day + "/" + year + "  at " + hour + ":" + minute + " " + am_pm);

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

        if (photoArray == null)
        {
            photoArray = new ArrayList<>();
        }
        if (resultCode != RESULT_CANCELED) {
            switch (requestCode) {
                case 0:
                    if (resultCode == RESULT_OK && data != null) {
                        Bitmap selectedImage = (Bitmap) data.getExtras().get("data");
                        photoArray.add(selectedImage);
                        pictureAttached.setVisibility(View.VISIBLE);
                    }

                    if (photoArray.size() == 1)
                    {
                        pictureAttached.setText("1 Picture Attached");
                    }
                    else
                    {
                        pictureAttached.setText(photoArray.size() + " Pictures Attached");
                    }

                    break;
                case 1:
                    if (resultCode == RESULT_OK && data != null) {
                        Uri selectedImage = data.getData();
                        String[] filePathColumn = {MediaStore.Images.Media.DATA};
                        if (selectedImage != null) {
                            Cursor cursor = (getActivity()).getContentResolver().query(selectedImage,
                                    filePathColumn, null, null, null);
                            if (cursor != null) {
                                cursor.moveToFirst();

                                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                                String picturePath = cursor.getString(columnIndex);
                                cursor.close();

                                photoArray.add(BitmapFactory.decodeFile(picturePath));
                                if (photoArray.size() == 1)
                                {
                                    pictureAttached.setText("1 Picture Attached");
                                }
                                else
                                {
                                    pictureAttached.setText(photoArray.size() + " Picture Attached");
                                }
                                pictureAttached.setVisibility(View.VISIBLE);
                            }
                        }

                    }
                    break;
            }
        }
    }

//        photoArray = (Bitmap)data.getExtras().get("data");
//
//        //imageView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
//
//
//        pictureAttached.setVisibility(View.VISIBLE);


    }
