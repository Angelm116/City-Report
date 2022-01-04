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

import java.util.Map;


public class FragmentSelectLocation extends Fragment implements BackPressSupport {


    private GoogleMap mMap;
    private MapView mMapView;
    private LatLng newLocation;
    Marker marker;


    private OnFragmentInteractionListener mListener;


    // Inflates the layout for this fragment
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View mView = inflater.inflate(R.layout.fragment_change_location, container, false);

        return mView;
    }

    // Gets called after the layout is fully inflated
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Button to select new location
        Button button = (Button) view.findViewById(R.id.button);

        // When the button is clicked, remove the marker and confirm the new location.
        button.setOnClickListener(new View.OnClickListener() {
                                      @Override
                                      public void onClick(View v) {
                                          marker.remove();
                                          ((MainActivity)getActivity()).confirmNewLocation(newLocation);
                                      }
                                  }
        );


        // View that contains the map.
        mMapView = (MapView) view.findViewById(R.id.fragment_change_MapView);
        mMapView.onCreate(null);
        mMapView.onResume();

        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mMap = googleMap;
            }
        });
    }


    // This function executes when the back button is pressed
    public boolean onBackPressed() {
        marker.remove();
        ((MainActivity)getActivity()).confirmNewLocation(newLocation);
        return true;
    }

    // This function gets called whenever the user
    public void updateMap(LatLng currentLocation) {

        // Move the camera to the current location and set the zoom to 18 units
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 18.0f));

        // Add a marker to the current position
        marker = mMap.addMarker(new MarkerOptions().position(currentLocation).title("Current Location").draggable(true));

        // This gets executed whenever the marker gets dragged in the map
        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {
            }

            @Override
            public void onMarkerDrag(Marker marker) {
            }

            // set newLocation to the location where the marker was dropped
            @Override
            public void onMarkerDragEnd(Marker marker) {
                newLocation = marker.getPosition();
            }
        });

        // In case the user never changes the location
        newLocation = currentLocation;
    }



    // TODO research this
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
