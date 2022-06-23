package com.example.locale;

import static com.example.locale.MainActivity.TAG;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

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

public class MapsFragment extends Fragment {
    ParseUser currentUser = ParseUser.getCurrentUser();
    ParseGeoPoint geoPoint = (ParseGeoPoint) currentUser.get("location");

    private OnMapReadyCallback callback = new OnMapReadyCallback() {

        @Override
        public void onMapReady(GoogleMap googleMap) {
            if (geoPoint != null){
                LatLng currentLocation = new LatLng(geoPoint.getLatitude(), geoPoint.getLongitude());
                googleMap.addMarker(new MarkerOptions().position(currentLocation).title("Current Location"));
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 12.0f));
            }

            JSONArray notVisitedLandmarks = currentUser.getJSONArray("not_visited_landmarks");

            for (int i=0; i<notVisitedLandmarks.length(); i++){
                try {
                    JSONObject jsonObject = (JSONObject) notVisitedLandmarks.get(i);
                    String objectId = jsonObject.getString("objectId");

                    ParseQuery<ParseObject> query = ParseQuery.getQuery("Location");
                    query.whereEqualTo("objectId", objectId);
                    query.getFirstInBackground(new GetCallback<ParseObject>() {
                        public void done(ParseObject object, ParseException e) {
                            if (e == null) {
                                ParseGeoPoint objGeoPoint = (ParseGeoPoint) object.getParseGeoPoint("coordinates");
                                LatLng newMarkerLocation = new LatLng(objGeoPoint.getLatitude(), objGeoPoint.getLongitude());
                                googleMap.addMarker(new MarkerOptions().position(newMarkerLocation).title(object.getString("place_name")).icon(BitmapDescriptorFactory.defaultMarker(96)));
                                Log.d(TAG, "Add landmark markers!");
                            } else {
                                Log.d(TAG, "Error!");
                            }
                        }
                    });

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_maps, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }
    }
}