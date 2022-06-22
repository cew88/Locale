/*
Main activity is accessible once users log in or create a new account. Main activity accesses the user's
location from the Parse database and queries the Places API to generate a list of local landmarks.
 */

package com.example.locale;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.codepath.asynchttpclient.AsyncHttpClient;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;
import com.google.android.libraries.places.api.Places;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Headers;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = "MainActivity";
    final FragmentManager fragmentManager = getSupportFragmentManager();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ParseObject.registerSubclass(Location.class);

        // Hide the action bar on the login screen
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        // Set the default fragment as HomeFragment
        fragmentManager.beginTransaction().replace(R.id.flContainer, new HomeFragment()).commit();

        // Initialize the SDK
        Places.initialize(getApplicationContext(), BuildConfig.MAPS_API_KEY);

        ParseUser currentUser = ParseUser.getCurrentUser();
        ParseGeoPoint geoPoint = (ParseGeoPoint) currentUser.get("location");
        if (geoPoint != null){
            double latitude = geoPoint.getLatitude();
            double longitude = geoPoint.getLongitude();

            // TO DO: Add limitations to when the API call is made

            // &type=restaurant
            String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" + latitude +  "%2C" + longitude + "&radius=30000&key=" + BuildConfig.MAPS_API_KEY;
            AsyncHttpClient client = new AsyncHttpClient();
            client.get(url, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Headers headers, JSON json) {
                    JSONObject jsonObject = json.jsonObject;
                    try {
                        JSONArray jsonArray = jsonObject.getJSONArray("results");
                        JSONArray allLandmarks = new JSONArray();

                        for (int i=0; i<jsonArray.length(); i++){
                            JSONObject locationObject = jsonArray.getJSONObject(i);

                            Location newLocation = new Location();
                            newLocation.setName(locationObject.getString("name"));
                            newLocation.setPlaceId(locationObject.getString("place_id"));
                            newLocation.setTypes(locationObject.getJSONArray("types"));
                            newLocation.setVicinity(locationObject.getString("vicinity"));

                            JSONObject coordinates = locationObject.getJSONObject("geometry").getJSONObject("location");
                            double lat = coordinates.getDouble("lat");
                            double lng = coordinates.getDouble("lng");
                            newLocation.setCoordinates(new ParseGeoPoint(lat, lng));
                            allLandmarks.put(newLocation);

                            saveLocation(newLocation, locationObject.getString("place_id"));
                        }
                        // Log.d(TAG, String.valueOf(landmarks));
                        currentUser.put("all_landmarks", allLandmarks);
                        currentUser.put("not_visited_landmarks", allLandmarks);
                        currentUser.saveInBackground();
                        // Log.d(TAG, latitude + "," + longitude);
                        // Log.d(TAG, jsonArray.toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                    Log.d(TAG, "onFailure");
                }
            });
        }
        else {
            // Get the user's location
        }


        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.action_home);
        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment fragment;
                switch (item.getItemId()) {
                    case R.id.action_home:
                        fragment = new HomeFragment();
                        break;
                    case R.id.action_map:
                        fragment = new MapFragment();
                        break;
                    case R.id.action_profile:
                        fragment = new ProfileFragment();
                        break;
                    default:
                        throw new IllegalStateException("Unexpected value: " + item.getItemId());
                }
                fragmentManager.beginTransaction().replace(R.id.flContainer, fragment).commit();
                return true;
            }

        });
    }

    // Save the queried locations to the Location class in the Parse Database
    // Check for duplicate place_id's before adding
    private void saveLocation(Location location, String place_id) throws JSONException {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Location");
        query.whereEqualTo("place_id", place_id);
        query.getFirstInBackground(new GetCallback<ParseObject>() {
            public void done(ParseObject object, ParseException e) {
                if(e == null) {
                    Log.d(TAG,"Object exists!");
                }
                else {
                    if(e.getCode() == ParseException.OBJECT_NOT_FOUND) {
                        Log.d(TAG, "Object does not exist");
                        location.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e != null) {
                                    Log.e(TAG, "Error while saving!", e);
                                    Toast.makeText(MainActivity.this, "Error while saving!", Toast.LENGTH_SHORT).show();
                                }
                                Log.i(TAG, "Location save was successful!");
                            }
                        });
                    }
                    else {
                        Log.d(TAG, "Error!");
                    }
                }
            }
        });
    }
}
