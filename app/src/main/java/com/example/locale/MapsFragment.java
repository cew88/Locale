package com.example.locale;

import static com.example.locale.MainActivity.TAG;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MapsFragment extends Fragment {
    ParseUser currentUser = ParseUser.getCurrentUser();
    ParseGeoPoint geoPoint = (ParseGeoPoint) currentUser.get("location");

    User user;
    ArrayList<Location> notVisitedLandmarks;
    RecyclerView rvLandmarks;
    ArrayList<Location> landmarks;
    LandmarksAdapter adapter;

    private OnMapReadyCallback callback = new OnMapReadyCallback() {

        @Override
        public void onMapReady(GoogleMap googleMap) {
            if (geoPoint != null){
                LatLng currentLocation = new LatLng(geoPoint.getLatitude(), geoPoint.getLongitude());
                googleMap.addMarker(new MarkerOptions().position(currentLocation).title("Current Location"));
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 12.0f));
            }

            for (int i=0; i<notVisitedLandmarks.size(); i++){
                Location loc = notVisitedLandmarks.get(i);
                LatLng newMarkerLocation = new LatLng(loc.getCoordinates().getLatitude(), loc.getCoordinates().getLongitude());
                googleMap.addMarker(new MarkerOptions().position(newMarkerLocation).title(loc.getString("place_name")).icon(BitmapDescriptorFactory.defaultMarker(96)));
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Get data passed from bundle
        user = this.getArguments().getParcelable("User");
        notVisitedLandmarks = user.getNotVisitedLandmarks();

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

        rvLandmarks = view.findViewById(R.id.rvLandmarks_Maps);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);

        SnapHelper helper = new LinearSnapHelper();
        helper.attachToRecyclerView(rvLandmarks);

        // Initialize the list of landmarks and adapter
        landmarks = new ArrayList<>();
        adapter = new LandmarksAdapter(getContext(), landmarks);
        // Recycler view setup: layout manager and the adapter
        rvLandmarks.setLayoutManager(linearLayoutManager);
        rvLandmarks.setAdapter(adapter);

        landmarks.addAll(notVisitedLandmarks);
    }
}