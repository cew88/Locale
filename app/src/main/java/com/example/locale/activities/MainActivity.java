/*
Main activity is accessible once users log in or create a new account. Main activity accesses the user's
location from the Parse database and queries the Places API to generate a list of local landmarks.
 */

package com.example.locale.activities;

import static com.example.locale.models.Constants.*;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.icu.util.LocaleData;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.codepath.asynchttpclient.AsyncHttpClient;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;
import com.example.locale.BuildConfig;
import com.example.locale.R;
import com.example.locale.adapters.HomeLandmarksAdapter;
import com.example.locale.adapters.RecommendedLandmarksAdapter;
import com.example.locale.applications.LocaleApplication;
import com.example.locale.fragments.HomeFragment;
import com.example.locale.fragments.LocationVisitedFragment;
import com.example.locale.fragments.MapsFragment;
import com.example.locale.fragments.ProfileFragment;
import com.example.locale.fragments.ReviewFragment;
import com.example.locale.interfaces.OnLocationsLoaded;
import com.example.locale.models.Converters;
import com.example.locale.models.Location;
import com.example.locale.models.Post;
import com.example.locale.models.User;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.parse.GetCallback;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import okhttp3.Headers;

public class MainActivity extends AppCompatActivity implements HomeLandmarksAdapter.OnLocationVisitedListener, ReviewFragment.AddPhoto, RecommendedLandmarksAdapter.OnRecommendedSelectedListener {
    final FragmentManager mFragmentManager = getSupportFragmentManager();
    // Get the user that is currently logged in
    ParseUser mCurrentUser = ParseUser.getCurrentUser();
    User mUser;

    Bundle mBundle;
    private BottomNavigationView bottomNavigationView;

    private FusedLocationProviderClient fusedLocationClient;

    HashMap<Location, Double> locationRanking = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Hide the action bar on the login screen
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        // Register Parse Location subclass
        ParseObject.registerSubclass(Location.class);
        ParseObject.registerSubclass(Post.class);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Access user data from the Room Database
        final User.UserDao userDao = ((LocaleApplication)getApplicationContext()).getUserDatabase().userDao();
        mUser = userDao.getByUsername(mCurrentUser.getUsername());

        // Clear previous recommendations
        mUser.setRecommendedString("");
        userDao.updateUser(mUser);

        // A boolean value is passed from the Interests Activity to the Main Activity when a user
        // has created their account. If there is no extra or the user did not just create their account
        // check the user's location against not visited landmarks

        if (!(getIntent().hasExtra("Just Registered"))) {
            // Get the user's location
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(MainActivity.this);
            fusedLocationClient.getLastLocation().addOnSuccessListener( new OnSuccessListener<>() {
                @Override
                public void onSuccess(android.location.Location location) {
                    // Got last known location; in some rare situations this can be null
                    // Check if the current location matches any locations that are in the user's list to visit
                    if (location != null) {
                        // Logic to handle location object
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();

                        // Filters landmarks based on the user's selected interests
                        for (int i=0; i<mUser.getInterests().size(); i++) {
                            String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" + latitude + "%2C" + longitude + "&radius=50&type=" + mUser.getInterests().get(i) + "&key=" + BuildConfig.MAPS_API_KEY;
                            AsyncHttpClient client = new AsyncHttpClient();
                            client.get(url, new JsonHttpResponseHandler() {
                                @Override
                                public void onSuccess(int statusCode, Headers headers, JSON json) {
                                    JSONObject jsonObject = json.jsonObject;

                                    try {
                                        JSONArray jsonArray = jsonObject.getJSONArray("results");
                                        for (int j = 0; j < jsonArray.length(); j++) {

                                            JSONObject locationObject = jsonArray.getJSONObject(j);
                                            String placeId = locationObject.getString(KEY_PLACE_ID);

                                            if (mUser.getNotVisitedPlaceIds().contains(placeId)){
                                                for (Location location: mUser.getNotVisited()){
                                                    if (location.getPlaceId().equals(placeId)){

                                                        try {
                                                            // Remove the visited landmark from the list of not visited landmarks
                                                            removeFromNotVisited(location);

                                                            // Updated mUser
                                                            updateLandmarks();

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
                                    Log.d(MAIN_ACTIVITY_TAG, "onFailure: " + response);
                                }
                            });
                        }

                        // Generate list of recommended landmarks
                        Set<String> locationsAdded = new HashSet<>();
                        ArrayList<String> mUserInterests = mUser.getInterests();
                        for (int i=0; i<mUserInterests.size(); i++){
                            String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" + latitude +  "%2C" + longitude + "&radius=1500&type=" + mUserInterests.get(i) + "&key=" + BuildConfig.MAPS_API_KEY;
                            AsyncHttpClient client = new AsyncHttpClient();
                            client.get(url, new JsonHttpResponseHandler() {
                                @Override
                                public void onSuccess(int statusCode, Headers headers, JSON json) {
                                    try {
                                        JSONObject jsonObject = json.jsonObject;
                                        JSONArray jsonArray = jsonObject.getJSONArray("results");
                                        for (int j = 0; j < jsonArray.length(); j++) {
                                            JSONObject locationObject = jsonArray.getJSONObject(j);
                                            String placeId = locationObject.getString(KEY_PLACE_ID);
                                            // Rank locations based on other user's recommendations
                                            ParseQuery<ParseObject> placeIdQuery = ParseQuery.getQuery("Location");
                                            placeIdQuery.whereEqualTo(KEY_PLACE_ID, placeId);
                                            int finalJ = j;
                                            placeIdQuery.getFirstInBackground(new GetCallback<ParseObject>() {
                                                @Override
                                                public void done(ParseObject object, com.parse.ParseException e) {
                                                    if (object != null){
                                                        Location existingLocation = (Location) object;
                                                        int totalVisited = existingLocation.getVisitedCount();
                                                        double averageRating = existingLocation.getTotalRating()/totalVisited;
                                                        if (Double.isNaN(averageRating)){
                                                            averageRating = 0;
                                                        }
                                                        double rank = (totalVisited * 0.5) + (averageRating * 0.5);
                                                        try {
//                                                            Log.d("Location name", existingLocation.getName());
//                                                            Log.d("Location rating", String.valueOf(existingLocation.getTotalRating()));
//                                                            Log.d("Location visited count", String.valueOf(existingLocation.getVisitedCount()));
                                                            // Avoid duplicate location entries
                                                            if (locationsAdded.isEmpty()){
                                                                locationsAdded.add(locationObject.getString(KEY_PLACE_ID));
                                                                if (!Double.isNaN(rank)){
                                                                    locationRanking.put(existingLocation, rank);
                                                                }
                                                            }
                                                            else {
                                                                if (!locationsAdded.contains(locationObject.getString(KEY_PLACE_ID))){
                                                                    locationsAdded.add(locationObject.getString(KEY_PLACE_ID));
                                                                    if (!Double.isNaN(rank)){
                                                                        locationRanking.put(existingLocation, rank);
                                                                    }
                                                                }
                                                            }
                                                        } catch (JSONException jsonException) {
                                                            jsonException.printStackTrace();
                                                        }

                                                    }

                                                    if (finalJ == jsonArray.length()-1){
                                                        try {
                                                            Log.d(MAIN_ACTIVITY_TAG, "addToRecommended called!");
                                                            addRecommended(locationRanking);
                                                        } catch (JSONException ex) {
                                                            ex.printStackTrace();
                                                        }
                                                    }
                                                }
                                            });
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }

                                @Override
                                public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                                    Log.d(MAIN_ACTIVITY_TAG, "onFailure: " + response);
                                }
                            });
                        }
                    }
                }
            });
        }

        // Access stored user information when the Main activity is opened and pass the data to the
        // Fragments via Bundle
        mBundle = new Bundle();
        mBundle.putParcelable("User", mUser);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

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
        final User.UserDao userDao = ((LocaleApplication) getApplicationContext()).getUserDatabase().userDao();
        ((LocaleApplication) getApplicationContext()).getUserDatabase().runInTransaction(new Runnable() {
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
    @Override
    public void removeFromNotVisited(Location location) throws JSONException {
        // Create pop up dialog
        LocationVisitedFragment locationVisitedFragment = new LocationVisitedFragment();
        Bundle locationBundle = new Bundle();
        locationBundle.putParcelable("User", mUser);
        locationBundle.putString(KEY_PLACE_NAME, location.getName());
        locationBundle.putString(KEY_PLACE_ID, location.getPlaceId());
        locationBundle.putString(KEY_OBJECT_ID, location.getObjectId());

        locationVisitedFragment.setArguments(locationBundle);
        locationVisitedFragment.show(mFragmentManager, "visited dialog");

        // Update the list of not visited landmarks
        ArrayList<Location> updatedNotVisited =  new ArrayList<>();
        for (Location notVisitedLocation : mUser.getNotVisited()){
            if (!notVisitedLocation.getPlaceId().equals(location.getPlaceId())){
                updatedNotVisited.add(notVisitedLocation);
            }
        }

        // Overwrite what is currently saved under the user's not visited landmarks
        mCurrentUser.put(KEY_NOT_VISITED_LANDMARKS, updatedNotVisited);
        mCurrentUser.saveInBackground();

        // Update the Room local database
        String notVisitedLandmarks = Converters.fromLocationArrayList(updatedNotVisited);
        mUser.setNotVisitedString(notVisitedLandmarks);
        User.UserDao userDao = ((LocaleApplication)getApplicationContext()).getUserDatabase().userDao();
        userDao.updateUser(mUser);
        mBundle.putParcelable("User", mUser);
    }

    // The following function adds the location to a JSON Array of visited locations
    public void addToVisited(String objectId, String placeId, String placeName, byte[] image) throws JSONException {
        // Check to make sure that the location is not already in the list of visited landmarks
        if (!String.valueOf(mCurrentUser.getJSONArray(KEY_VISITED_LANDMARKS)).contains(placeId)){
            Date currentTime = Calendar.getInstance().getTime();
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(objectId, currentTime);
            jsonObject.put(KEY_OBJECT_ID, objectId);
            jsonObject.put(KEY_PLACE_ID, placeId);
            jsonObject.put(KEY_PLACE_NAME, placeName);
            jsonObject.put(KEY_DATE_VISITED, currentTime);

            if (image == null){
                jsonObject.put(KEY_PHOTO, "");
            }
            else {
                String encodedImage = Base64.getEncoder().encodeToString(image);
                jsonObject.put(KEY_PHOTO, encodedImage);
            }

            mCurrentUser.add(KEY_VISITED_LANDMARKS, String.valueOf(jsonObject));
            mCurrentUser.saveInBackground();
            mUser.setVisitedString(String.valueOf(mCurrentUser.getJSONArray(KEY_VISITED_LANDMARKS)));
            User.UserDao userDao = ((LocaleApplication)getApplicationContext()).getUserDatabase().userDao();
            userDao.updateUser(mUser);
            mBundle.putParcelable("User", mUser);
        }
    }

    @Override
    public void addPhoto(String objectId, String placeId, String placeName, byte[] image) throws JSONException, UnsupportedEncodingException {
        // Add the location to the visited landmarks
        addToVisited(objectId, placeId, placeName, image);
    }

    public void addRecommended(HashMap<Location, Double> locationRanking) throws JSONException {
        Log.d(MAIN_ACTIVITY_TAG, "Adding recommended locations");

        ArrayList<Location> recLoc =  new ArrayList<>();
        // If there are locations near the current location that have a ranking
        if (!locationRanking.isEmpty()){
            Log.d(MAIN_ACTIVITY_TAG, "Location ranking is not empty");
            // Get the maximum ranking score
            double maxValue = Collections.max(locationRanking.values());
//            Log.d("Maximum value", String.valueOf(maxValue));
            // Iterate through the mapping and find the locations with the highest rankings
            for (Location dictKey : locationRanking.keySet()){
//                Log.d("Recommended Location", dictKey.getName());
                if (locationRanking.get(dictKey) == maxValue){

                    // Check to make sure that the recommended location is not already included with the user's landmarks
                    boolean inAll;
                    boolean inNotVisited;
                    boolean inRecommended;

                    String allString = mUser.getAllString();
                    String notVisitedString = mUser.getNotVisitedString();
                    String recommendedString = mUser.getRecommendedString();

                    if (allString != null){ inAll = allString.contains(dictKey.getPlaceId());}
                    else { inAll = false; }

                    if (notVisitedString != null) { inNotVisited = notVisitedString.contains(dictKey.getPlaceId()); }
                    else { inNotVisited = false; }

                    if (recommendedString != null) { inRecommended = recommendedString.contains(dictKey.getPlaceId()); }
                    else { inRecommended = false; }


//                    boolean inAll = mUser.getAllString().contains(dictKey.getPlaceId());
//                    boolean inNotVisited = mUser.getNotVisitedString().contains(dictKey.getPlaceId());
//                    boolean inRecommended = mUser.getRecommendedString().contains(dictKey.getPlaceId());

//                    Log.d("Location is already in all", String.valueOf(inAll));
//                    Log.d("Location is already in notVisited", String.valueOf(inNotVisited));
//                    Log.d("Location is already in recommended", String.valueOf(inRecommended));

                    // Check to make sure that the recommended location is not already included with the user's landmarks
                    if (!(inAll || inNotVisited || inRecommended)){
                        recLoc.add(dictKey);

                        mCurrentUser.add(KEY_RECOMMENDED_LANDMARKS, dictKey);
                        mCurrentUser.saveInBackground();
                    }
                }
            }

            // Update the Room local database
            String notVisitedLandmarks = Converters.fromLocationArrayList(recLoc);
            mUser.setRecommendedString(notVisitedLandmarks);
            User.UserDao userDao = ((LocaleApplication)getApplicationContext()).getUserDatabase().userDao();
            userDao.updateUser(mUser);
            mBundle.putParcelable("User", mUser);
        }

        // Set the default fragment as HomeFragment
        Fragment defaultFragment = new HomeFragment();
        defaultFragment.setArguments(mBundle);
        mFragmentManager.beginTransaction().replace(R.id.flContainer, defaultFragment).commit();
        bottomNavigationView.setSelectedItemId(R.id.action_home);
    }

    @Override
    public void updateRecommended(Location location) throws JSONException {
        // Update the list of not visited landmarks
        ArrayList<Location> updatedRecommended =  new ArrayList<>();
        for (Location recommendedLocation : mUser.getRecommended()){

            if (!recommendedLocation.getPlaceId().equals(location.getPlaceId())){
                updatedRecommended.add(recommendedLocation);
            }
        }

        // Overwrite what is currently saved under the user's recommended
        mCurrentUser.put(KEY_RECOMMENDED_LANDMARKS, updatedRecommended);
        mCurrentUser.saveInBackground();

        // Update the Room local database
        String recommendedLandmarks = Converters.fromLocationArrayList(updatedRecommended);
        mUser.setRecommendedString(recommendedLandmarks);
        User.UserDao userDao = ((LocaleApplication)getApplicationContext()).getUserDatabase().userDao();
        userDao.updateUser(mUser);
        mBundle.putParcelable("User", mUser);
    }

    @Override
    public void updateNotVisited(Location location) throws JSONException {
        // Update the list of not visited landmarks
        ArrayList<Location> updatedNotVisited =  new ArrayList<>();
        for (Location recommendedLocation : mUser.getNotVisited()){
            updatedNotVisited.add(recommendedLocation);
        }
        updatedNotVisited.add(location);

        // Overwrite what is currently saved under the user's not visited landmarks
        mCurrentUser.put(KEY_NOT_VISITED_LANDMARKS, updatedNotVisited);
        mCurrentUser.saveInBackground();

        // Update the Room local database
        String notVisitedLandmarks = Converters.fromLocationArrayList(updatedNotVisited);
        mUser.setNotVisitedString(notVisitedLandmarks);
        User.UserDao userDao = ((LocaleApplication)getApplicationContext()).getUserDatabase().userDao();
        userDao.updateUser(mUser);
        mBundle.putParcelable("User", mUser);
    }

    @Override
    public void updateAll(Location location) throws JSONException {
        ArrayList<Location> updatedAll = new ArrayList<>();
        for (Location recommendedLocation : mUser.getNotVisited()){
            updatedAll.add(recommendedLocation);
        }
        updatedAll.add(location);

        // Overwrite what is currently saved under the user's all landmarks
        mCurrentUser.put(KEY_ALL_LANDMARKS, updatedAll);
        mCurrentUser.saveInBackground();

        // Update the Room local database
        String allLandmarks = Converters.fromLocationArrayList(updatedAll);
        mUser.setAllString(allLandmarks);
        User.UserDao userDao = ((LocaleApplication)getApplicationContext()).getUserDatabase().userDao();
        userDao.updateUser(mUser);
        mBundle.putParcelable("User", mUser);
    }

    @Override
    protected void onStop() {
        super.onStop();

        Date currentTime = Calendar.getInstance().getTime();

        // Iterate through the visited landmarks and check when the user last visited a location
        try {
            ArrayList<JSONObject> visitedLandmarks = mUser.getVisited();
            Date latestDate = null;
            for (JSONObject jsonObject : visitedLandmarks){
                // Create a new date format in the correct pattern
                SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy");
                // Convert the date from a String to a Date object
                Date dateVisited = dateFormat.parse(jsonObject.getString("date_visited"));

                if (latestDate == null){
                    latestDate = dateVisited;
                }
                else {
                    if (dateVisited.compareTo(latestDate) > 0){
                        latestDate = dateVisited;
                    }
                }
            }

            long diff = currentTime.getDay() - latestDate.getDay();
            // If it has been two days since the last location was added as visited
            if (diff > 2){
                createNotification("It's been a while...", "Find someplace new to visit and get your adventure on!");
            }

            // If the user has not visited a landmark yet
            if (visitedLandmarks.isEmpty()){
                int locationIndex = (int) (Math.random() * mUser.getNotVisited().size());
                createNotification("Looking for adventure?", "Check out " + mUser.getNotVisited().get(locationIndex).getName() + "!");
            }

        } catch (JSONException | ParseException e) {
            e.printStackTrace();
        }
    }

    private void createNotification(String title, String body){
        int NOTIFICATION_ID = 3;

        Intent notifyIntent = new Intent(this, MainActivity.class);
        int requestID = (int) System.currentTimeMillis(); // Unique requestID to differentiate between various notifications with same id
        int flags = PendingIntent.FLAG_CANCEL_CURRENT; // Cancel old intent and create new one
        PendingIntent pendingIntent = PendingIntent.getActivity(this, requestID, notifyIntent, flags);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, "notificationChannel")
                .setSmallIcon(R.drawable.marker)
                .setContentTitle(title)
                .setContentText(body)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }
}