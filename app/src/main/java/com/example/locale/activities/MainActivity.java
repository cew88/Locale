/*
Main activity is accessible once users log in or create a new account. Main activity accesses the user's
location from the Parse database and queries the Places API to generate a list of local landmarks.
 */

package com.example.locale.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.codepath.asynchttpclient.AsyncHttpClient;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;
import com.example.locale.BuildConfig;
import com.example.locale.R;
import com.example.locale.adapters.HomeLandmarksAdapter;
import com.example.locale.applications.DatabaseApplication;
import com.example.locale.fragments.HomeFragment;
import com.example.locale.fragments.LocationVisitedFragment;
import com.example.locale.fragments.MapsFragment;
import com.example.locale.fragments.ProfileFragment;
import com.example.locale.fragments.ReviewFragment;
import com.example.locale.interfaces.OnLocationsLoaded;
import com.example.locale.models.Converters;
import com.example.locale.models.Location;
import com.example.locale.models.User;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.parse.ParseObject;
import com.parse.ParseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;

import okhttp3.Headers;

public class MainActivity extends AppCompatActivity implements HomeLandmarksAdapter.OnLocationVisitedListener, ReviewFragment.AddPhoto {
    public static final String TAG = "MainActivity";
    public static final String KEY_NOT_VISITED_LANDMARKS = "not_visited_landmarks";
    public static final String KEY_VISITED_LANDMARKS = "visited_landmarks";

    final FragmentManager mFragmentManager = getSupportFragmentManager();

    // Get the user that is currently logged in
    ParseUser mCurrentUser = ParseUser.getCurrentUser();
    User mUser;
    Bundle mBundle;

    private FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Hide the action bar on the login screen
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        // Register Parse Location subclass
        ParseObject.registerSubclass(Location.class);

        // Access user data from the Room Database
        final User.UserDao userDao = ((DatabaseApplication)getApplicationContext()).getUserDatabase().userDao();
        mUser = userDao.getByUsername(mCurrentUser.getUsername());

        // Get the user's location
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(MainActivity.this);
        fusedLocationClient.getLastLocation().addOnSuccessListener( new OnSuccessListener<android.location.Location>() {
            @Override
            public void onSuccess(android.location.Location location) {
                // Got last known location. In some rare situations this can be null.
                if (location != null) {
                    // Logic to handle location object
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();

                    // Filters landmarks based on the user's selected interests
                    for (int i=0; i<mUser.getInterests().size(); i++) {
                        String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" + latitude + "%2C" + longitude + "&radius=500&type=" + mUser.getInterests().get(i) + "&key=" + BuildConfig.MAPS_API_KEY;
                        AsyncHttpClient client = new AsyncHttpClient();
                        client.get(url, new JsonHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Headers headers, JSON json) {
                                JSONObject jsonObject = json.jsonObject;
                                try {
                                    JSONArray jsonArray = jsonObject.getJSONArray("results");
                                    for (int j = 0; j < mUser.getUserPace() / mUser.getInterests().size(); j++) {

                                        JSONObject locationObject = jsonArray.getJSONObject(j);
                                        String placeId = locationObject.getString("place_id");

                                        if (mUser.getNotVisitedPlaceIds().contains(placeId)){
                                            for (Location location: mUser.getNotVisited()){
                                                if (location.getPlaceId().equals(placeId)){

                                                    try {
                                                        // Remove the visited landmark from the list of not visited landmarks
                                                        ArrayList<Location> updatedNotVisited = removeFromNotVisited(location);

                                                        // Updated mUser
                                                        updateLandmarks();

                                                        // Create pop up dialog
                                                        LocationVisitedFragment locationVisitedFragment = new LocationVisitedFragment();
                                                        Bundle locationBundle = new Bundle();
                                                        locationBundle.putString("Place Name", location.getName());
                                                        locationBundle.putString("Place Id", location.getPlaceId());
                                                        locationBundle.putString("Object Id", location.getObjectId());

                                                        locationVisitedFragment.setArguments(locationBundle);
                                                        locationVisitedFragment.show(mFragmentManager, "visited dialog");

                                                        // Update the Room local database
                                                        String notVisitedLandmarks = Converters.fromLocationArrayList(updatedNotVisited);
                                                        mUser.setNotVisitedString(notVisitedLandmarks);
                                                        userDao.updateUser(mUser);

                                                        // Recreate the Home fragment so that the visited landmark no longer appears on the screen
                                                        Fragment defaultFragment = new HomeFragment();
                                                        defaultFragment.setArguments(mBundle);
                                                        mFragmentManager.beginTransaction().replace(R.id.flContainer, defaultFragment).commit();

                                                    } catch (JSONException ex) {
                                                        ex.printStackTrace();
                                                    }
                                                }
                                            }
                                        }
                                    }

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                            @Override
                            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                                Log.d(TAG, "onFailure: " + response);
                            }
                        });
                    }
                }
            }
        });


        // Access stored user information when the Main activity is opened and pass the data to the
        // Fragments via Bundle
        mBundle = new Bundle();
        mBundle.putParcelable("User", mUser);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Set the default fragment as HomeFragment
        Fragment defaultFragment = new HomeFragment();
        defaultFragment.setArguments(mBundle);
        mFragmentManager.beginTransaction().replace(R.id.flContainer, defaultFragment).commit();
        bottomNavigationView.setSelectedItemId(R.id.action_home);

        // Handle clicks on the bottom navigation bar
        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment fragment;
                switch (item.getItemId()) {
                    case R.id.action_home:
                        fragment = new HomeFragment();
                        fragment.setArguments(mBundle);
                        break;
                    case R.id.action_map:
                        fragment = new MapsFragment();
                        fragment.setArguments(mBundle);
                        break;
                    case R.id.action_profile:
                        fragment = new ProfileFragment();
                        fragment.setArguments(mBundle);
                        break;
                    default:
                        throw new IllegalStateException("Unexpected value: " + item.getItemId());
                }
                mFragmentManager.beginTransaction().replace(R.id.flContainer, fragment).commit();
                return true;
            }
        });
    }

    // Query Parse for updated user data in response to marking a location visited in the Landmark adapter
    @Override
    public void updateLandmarks() {

        // Get the DAO
        final User.UserDao userDao = ((DatabaseApplication) getApplicationContext()).getUserDatabase().userDao();
        ((DatabaseApplication) getApplicationContext()).getUserDatabase().runInTransaction(new Runnable() {
            @Override
            public void run() {
                try {
                    OnLocationsLoaded onLocationsLoaded = new OnLocationsLoaded() {
                        @Override
                        public void updateNotVisited(String notVisitedString) {
                            Log.d("MainActivity", "Not Visited Loaded");
                            userDao.updateNotVisited(notVisitedString);
                        }

                        @Override
                        public void updateVisited(String visitedString) {
                            Log.d("MainActivity", "Visited Loaded");
                            userDao.updateVisited(visitedString);
                        }

                        @Override
                        public void updateAll(String allString) {
                            Log.d("MainActivity", "All Loaded");
                            userDao.updateAll(allString);
                        }
                    };
                    mUser = new User(ParseUser.getCurrentUser(), onLocationsLoaded);
                    userDao.insertUser(mUser);
                    mBundle.putParcelable("User", mUser);
                } catch (JSONException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    // The following function marks the location as visited in the Parse database by removing the
    // location from the array of not visited landmarks
    public ArrayList<Location> removeFromNotVisited(Location location) throws JSONException {
        ArrayList<Location> updatedNotVisited =  new ArrayList<Location>();
        for (Location notVisitedLocation : mUser.getNotVisited()){
            if (!notVisitedLocation.getPlaceId().equals(location.getPlaceId())){
                updatedNotVisited.add(notVisitedLocation);
            }
        }

        // Overwrite what is currently saved under the user's not visited landmarks
        mCurrentUser.put(KEY_NOT_VISITED_LANDMARKS, updatedNotVisited);
        mCurrentUser.saveInBackground();
        return updatedNotVisited;
    }

    // The following function adds the location to a JSON Array of visited locations
    public void addToVisited(String objectId, String placeId, String placeName, byte[] image) throws JSONException, UnsupportedEncodingException {
        // Check to make sure that the location is not already in the list of visited landmarks
        if (!String.valueOf(mCurrentUser.getJSONArray(KEY_VISITED_LANDMARKS)).contains(placeId)){
            Date currentTime = Calendar.getInstance().getTime();
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(objectId, currentTime);
            jsonObject.put("objectId", objectId);
            jsonObject.put("place_id", placeId);
            jsonObject.put("place_name", placeName);
            jsonObject.put("date_visited", currentTime);

            String encodedImage = Base64.getEncoder().encodeToString(image);
            jsonObject.put("photo", encodedImage);

            mCurrentUser.add(KEY_VISITED_LANDMARKS, String.valueOf(jsonObject));
            mCurrentUser.saveInBackground();
            mUser.setVisitedString(String.valueOf(mCurrentUser.getJSONArray(KEY_VISITED_LANDMARKS)));
            User.UserDao userDao = ((DatabaseApplication)getApplicationContext()).getUserDatabase().userDao();
            userDao.updateUser(mUser);
        }
    }

    @Override
    public void addPhoto(String objectId, String placeId, String placeName, byte[] image) throws JSONException, UnsupportedEncodingException {
        // Add the location to the visited landmarks
        addToVisited(objectId, placeId, placeName, image);

        // Update mUser
        updateLandmarks();
    }
}