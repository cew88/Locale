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
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

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
import com.example.locale.fragments.PostFragment;
import com.example.locale.fragments.ProfileFragment;
import com.example.locale.fragments.ReviewFragment;
import com.example.locale.interfaces.AddPhoto;
import com.example.locale.interfaces.OnLocationVisitedListener;
import com.example.locale.interfaces.OnLocationsLoaded;
import com.example.locale.interfaces.OnRecommendedSelectedListener;
import com.example.locale.models.Converters;
import com.example.locale.models.Location;
import com.example.locale.models.Post;
import com.example.locale.models.User;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.parse.GetCallback;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
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

public class MainActivity extends AppCompatActivity implements OnLocationVisitedListener, AddPhoto, OnRecommendedSelectedListener {
    final FragmentManager mFragmentManager = getSupportFragmentManager();
    // Get the user that is currently logged in
    ParseUser mCurrentUser = ParseUser.getCurrentUser();
    User mUser;

    public static boolean showOfflineBanner = true;
    public static boolean showOfflineBannerPosts = true;

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
        User loggedInUser = (User) Parcels.unwrap(getIntent().getParcelableExtra("User"));
        if (loggedInUser != null){
            mUser = userDao.getByUsername(loggedInUser.getUserName());

            // Clear previous recommendations
            mUser.setRecommendedString("");
            userDao.updateUser(mUser);

            // A boolean value is passed from the Interests Activity to the Main Activity when a user
            // has created their account. If there is no extra or the user did not just create their account
            // check the user's location against not visited landmarks

            if (LoginSplashActivity.connectedToNetwork && !(getIntent().hasExtra("Just Registered"))) {
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
                                        // Get the JSON Object returned by the API call
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
                                            // Get the JSON Object returned by the API call
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
                                                        // If the location is stored in the Parse database already
                                                        // Check get ranking information from the Parse database
                                                        if (object != null){
                                                            Location existingLocation = (Location) object;
                                                            int totalVisited = existingLocation.getVisitedCount();
                                                            double averageRating = existingLocation.getTotalRating()/totalVisited;
                                                            if (Double.isNaN(averageRating)){
                                                                averageRating = 0;
                                                            }
                                                            double rank = (totalVisited * 0.5) + (averageRating * 0.5);
                                                            try {
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

                                                        // If the location is not already stored in Parse
                                                        // Add the location to Parse and add the location to the mapping a rank of 0
                                                        if(e == null) {
                                                            Log.d(INTERESTS_ACTIVITY_TAG,"Object exists!");
                                                        }
                                                        else {
                                                            if (e.getCode() == com.parse.ParseException.OBJECT_NOT_FOUND) {
                                                                Log.d(MAIN_ACTIVITY_TAG, "Object does not exist");
                                                                // Create a new location object
                                                                Location newLocation = new Location();
                                                                try {
                                                                    newLocation.setName(locationObject.getString(KEY_NAME_GOOGLE));
                                                                    newLocation.setPlaceId(locationObject.getString(KEY_PLACE_ID));
                                                                    newLocation.setTypes(locationObject.getJSONArray(KEY_TYPES));
                                                                    newLocation.setVicinity(locationObject.getString(KEY_VICINITY));
                                                                    JSONObject coordinates = locationObject.getJSONObject(KEY_GEOMETRY).getJSONObject(KEY_LOCATION);
                                                                    double lat = coordinates.getDouble(KEY_LAT);
                                                                    double lng = coordinates.getDouble(KEY_LNG);
                                                                    newLocation.setCoordinates(new ParseGeoPoint(lat, lng));
                                                                    newLocation.setVisitedCount(0);
                                                                    newLocation.setTotalRating(0);

                                                                    // Save the location to Parse
                                                                    newLocation.saveInBackground(new SaveCallback() {
                                                                        @Override
                                                                        public void done(com.parse.ParseException e) {
                                                                            if (e == null) {
                                                                                locationRanking.put(newLocation, 0.0);
                                                                                Log.i(MAIN_ACTIVITY_TAG, "Location save was successful!");
                                                                            } else {
                                                                                Log.e(MAIN_ACTIVITY_TAG, "Error while saving!", e);
                                                                            }
                                                                        }
                                                                    });

                                                                } catch (JSONException ex) {
                                                                    ex.printStackTrace();
                                                                }

                                                            } else {
                                                                Log.d(MAIN_ACTIVITY_TAG, "Error: " + e.getCode());
                                                            }
                                                        }
                                                        // When the last location is checked, call addRecommended
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

            Fragment defaultFragment = new HomeFragment();
            defaultFragment.setArguments(mBundle);
            mFragmentManager.beginTransaction().replace(R.id.flContainer, defaultFragment).commit();

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
                        case R.id.action_explore:
                            fragment = new PostFragment();
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
        else {
            // If the user is null, return to the log out page
            Intent i = new Intent(this, LoginActivity.class);
            startActivity(i);
            this.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }
    }

    // The following is called when the app is closed
    @Override
    protected void onStop() {
        super.onStop();

        if (mUser != null){
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

                // Create an array of potential notification title prompts
                String[] notificationPrompts = {"It's been a while...", "Explore someplace new!", "Get your adventure on!", "Looking for adventure?" , "Looking for some place new?"};
                // Randomly select a notification title prompt
                int promptIndex = new Random().nextInt(notificationPrompts.length);

                // Randomly select a location from the list of user's landmarks to visit
                ArrayList<Location> notVisited = mUser.getNotVisited();
                if (!notVisited.isEmpty()){
                    int locationIndex = new Random().nextInt(notVisited.size());
                    // If the user has not yet visited a location, send a notification
                    if (visitedLandmarks.isEmpty()){
                        createNotification(notificationPrompts[promptIndex], "Check out " + notVisited.get(locationIndex).getName() + "!");
                    }
                    // If it has been two days since the last location was added as visited
                    else if (latestDate != null){
                        long diff = currentTime.getTime() - latestDate.getTime();

                        if (diff >=  172800000) {
                            createNotification(notificationPrompts[promptIndex], "Check out " + mUser.getNotVisited().get(locationIndex).getName() + "!");
                        }
                    }
                }
            } catch (JSONException | ParseException e) {
                e.printStackTrace();
            }
        }
    }

    // The following function creates a push notification
    private void createNotification(String title, String body){
        int NOTIFICATION_ID = 3;
        Intent notifyIntent = new Intent(this, LoginSplashActivity.class);
        // Unique requestID to differentiate between various notifications with same id
        int requestID = (int) System.currentTimeMillis();
        // Cancel old intent and create new one
        int flags = PendingIntent.FLAG_IMMUTABLE;
        // Create a pending intent to open the app on click
        PendingIntent pendingIntent = PendingIntent.getActivity(this, requestID, notifyIntent, flags);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, "notificationChannel")
                .setSmallIcon(R.drawable.marker)
                .setContentTitle(title)
                .setContentText(body)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
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

    @Override
    // The following function adds the location to the visited landmarks
    public void addPhoto(String objectId, String placeId, String placeName, byte[] image) throws JSONException, UnsupportedEncodingException {
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

    // The following function adds the location to a list of recommended landmarks in Parse and Room
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
                Log.d("Recommended Location", dictKey.getName());
                if (locationRanking.get(dictKey) == maxValue){

                    // Check to make sure that the recommended location is not already included with the user's landmarks
                    boolean inVisited;
                    boolean inNotVisited;
                    boolean inRecommended;

                    // Check to make sure that the recommended location is not already included with the user's landmarks
                    String visitedString = mUser.getVisitedString();
                    String notVisitedString = mUser.getNotVisitedString();
                    String recommendedString = mUser.getRecommendedString();

                    if (visitedString != null){ inVisited = visitedString.contains(dictKey.getPlaceId());}
                    else { inVisited = false; }

                    if (notVisitedString != null) { inNotVisited = notVisitedString.contains(dictKey.getPlaceId()); }
                    else { inNotVisited = false; }

                    if (recommendedString != null) { inRecommended = recommendedString.contains(dictKey.getPlaceId()); }
                    else { inRecommended = false; }

                    // If the location is not in the user's visited, not visited, or already in their recommended, add the location
                    if (!(inVisited || inNotVisited || inRecommended)){
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
    // The following function updates the list of recommended landmarks in Parse and Room after a user
    // clicks the "+" sign on the recommended landmark item view
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
    // The following function updates the list of not visited landmarks in Parse and Room after a user
    // clicks the "+" sign on the recommended landmark item view
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
}