/*
Creates a Google Maps fragment to display the user's current location and the location of the
landmarks that the user has not visited yet.
 */


package com.example.locale;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

public class MapsFragment extends Fragment {
    User mUser;
    ArrayList<Location> mNotVisitedLandmarks;
    RecyclerView mRvLandmarks;
    ArrayList<Location> mLandmarks;
    LandmarksAdapter mAdapter;

    private OnMapReadyCallback callback = new OnMapReadyCallback() {

        @Override
        public void onMapReady(GoogleMap googleMap) {
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
        mNotVisitedLandmarks = mUser.getNotVisitedLandmarks();

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
        mAdapter = new LandmarksAdapter(getContext(), mLandmarks);

        // Recycler view setup: layout manager and the adapter
        mRvLandmarks.setLayoutManager(linearLayoutManager);
        mRvLandmarks.setAdapter(mAdapter);

        mLandmarks.addAll(mNotVisitedLandmarks);
    }
}