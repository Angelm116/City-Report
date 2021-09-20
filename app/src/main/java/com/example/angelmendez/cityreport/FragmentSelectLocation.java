package com.example.angelmendez.cityreport;

import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;


public class FragmentSelectLocation extends Fragment implements OnMapReadyCallback, BackPressSupport {


    private GoogleMap mMap;
    private MapView mMapView;
    private View mView;
    private LatLng locationIn;
    private LatLng locationOut;
    Marker marker;


    private OnFragmentInteractionListener mListener;

    // constructor for the fragment, ignore
    public FragmentSelectLocation() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_change_location, container, false);

        Button button = (Button)mView.findViewById(R.id.button);


        button.setOnClickListener(new View.OnClickListener()
                                  {
                                      @Override
                                      public void onClick(View v) {
                                          marker.remove();
                                          ((MainActivity)getActivity()).confirmNewLocation(locationOut);
                                      }
                                  }
                );


        return mView;
    }

    public boolean onBackPressed() {
        marker.remove();
        ((MainActivity)getActivity()).confirmNewLocation(locationOut);
        return true;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mMapView = (MapView) mView.findViewById(R.id.map);

            mMapView.onCreate(null);
            mMapView.onResume();
            mMapView.getMapAsync(this);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

//        // Add a marker in Sydney and move the camera
//        LatLng currentLocation = ((MainActivity)getActivity()).getLatLng();
//        locationOut = currentLocation;
//        mMap.addMarker(new MarkerOptions().position(currentLocation).title("Current Location").draggable(true));
//
//        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
//            @Override
//            public void onMarkerDragStart(Marker marker) {
//
//            }
//
//            @Override
//            public void onMarkerDrag(Marker marker) {
//
//            }
//
//            @Override
//            public void onMarkerDragEnd(Marker marker) {
//                locationOut = marker.getPosition();
//            }
//        });
//
//        float zoomLevel = 18.0f; //This goes up to 21
//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, zoomLevel));

    }

    public void updateMap(LatLng location){
        // Add a marker in Sydney and move the camera

        //((MainActivity)getActivity()).getLatLng();
        LatLng currentLocation = location;
        locationOut = currentLocation;
        marker = mMap.addMarker(new MarkerOptions().position(currentLocation).title("Current Location").draggable(true));


        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {

            }

            @Override
            public void onMarkerDrag(Marker marker) {

            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                locationOut = marker.getPosition();
            }
        });

        float zoomLevel = 18.0f; //This goes up to 21
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, zoomLevel));
    }



    // basically, we cant get rid of this, ignore it
    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
