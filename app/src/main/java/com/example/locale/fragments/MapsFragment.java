/*
Creates a Google Maps fragment to display the user's current location and the location of the
landmarks that the user has not visited yet.
 */


package com.example.locale.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import com.example.locale.adapters.MapLandmarksAdapter;
import com.example.locale.models.Location;
import com.example.locale.R;
import com.example.locale.models.User;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;

import java.util.ArrayList;

public class MapsFragment extends Fragment implements MapLandmarksAdapter.OnLocationClickedListener {
    User mUser;
    ArrayList<Location> mNotVisitedLandmarks;
    RecyclerView mRvLandmarks;
    ArrayList<Location> mLandmarks;
    MapLandmarksAdapter mAdapter;
    LatLng markerLocation;
    GoogleMap mGoogleMap;

    private OnMapReadyCallback callback = new OnMapReadyCallback() {

        @Override
        public void onMapReady(GoogleMap googleMap) {
            mGoogleMap = googleMap;
            LatLng currentLocation = new LatLng(mUser.getLatitude(), mUser.getLongitude());
            googleMap.addMarker(new MarkerOptions().position(currentLocation).title("Current Location"));
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 12.0f));

            for (int i=0; i<mNotVisitedLandmarks.size(); i++){
                Location loc = mNotVisitedLandmarks.get(i);
                LatLng newMarkerLocation = new LatLng(loc.getCoordinates().getLatitude(), loc.getCoordinates().getLongitude());
                googleMap.addMarker(new MarkerOptions().position(newMarkerLocation).title(loc.getString("place_name")).icon(BitmapDescriptorFactory.defaultMarker(96)));
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Get data passed from bundle
        mUser = this.getArguments().getParcelable("User");

        try {
            mNotVisitedLandmarks = mUser.getNotVisited();
        } catch (JSONException e) {
            e.printStackTrace();
        }


        return inflater.inflate(R.layout.fragment_maps, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }

        mRvLandmarks = view.findViewById(R.id.rvLandmarks_Maps);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);

        // Addition for the horizontal scrolling UI
        // If a list item is "halfway" on the screen, the SnapHelper "snaps" it into place so that
        // the item is centered on the screen
        SnapHelper helper = new LinearSnapHelper();
        helper.attachToRecyclerView(mRvLandmarks);

        // Initialize the list of landmarks and adapter
        mLandmarks = new ArrayList<>();
        mAdapter = new MapLandmarksAdapter(getContext(), mLandmarks, this);

        // Recycler view setup: layout manager and the adapter
        mRvLandmarks.setLayoutManager(linearLayoutManager);
        mRvLandmarks.setAdapter(mAdapter);

        mLandmarks.addAll(mNotVisitedLandmarks);
    }

    @Override
    public void zoomInOnMarkers(double latitude, double longitude) {
        markerLocation = new LatLng(latitude, longitude);

        // Construct a CameraPosition focusing on the marker location and animate the camera to that position.
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(markerLocation )    // Sets the center of the map to the marker location
                .zoom(20)                   // Sets the zoom
                .bearing(0)                 // Sets the orientation of the camera to north
                .tilt(0)                    // Sets the tilt of the camera to 0 degrees
                .build();                   // Creates a CameraPosition from the builder
        mGoogleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }
}