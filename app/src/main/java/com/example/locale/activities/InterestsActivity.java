/*
Interests activity allows users to select categories of locations that they are interested in
visiting. Selected items change color on click (color toggles base don whether or not the category
is selected). Submitting these categories saves their interests to the Parse database. These
categories are then used in the query to the Places API to filter nearby locations by user interest.
 */

package com.example.locale.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.codepath.asynchttpclient.AsyncHttpClient;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;
import com.example.locale.BuildConfig;
import com.example.locale.R;
import com.example.locale.applications.DatabaseApplication;
import com.example.locale.interfaces.OnLocationsLoaded;
import com.example.locale.models.Location;
import com.example.locale.models.User;
import com.google.android.flexbox.FlexboxLayout;
import com.google.android.libraries.places.api.Places;
import com.parse.GetCallback;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Headers;

public class InterestsActivity extends AppCompatActivity implements View.OnClickListener{
    // Set constants
    public static final String TAG = "InterestsActivity";
    public static final String KEY_AMUSEMENT_PARK= "amusement_park";
    public static final String KEY_AQUARIUM = "aquarium";
    public static final String KEY_ART_GALLERY = "art_gallery";
    public static final String KEY_BAKERY = "bakery";
    public static final String KEY_CAFE = "cafe";
    public static final String KEY_MOVIE_THEATER= "move_theater";
    public static final String KEY_MUSEUEM = "museum";
    public static final String KEY_NIGHT_CLUB = "night_club";
    public static final String KEY_PARK= "park";
    public static final String KEY_RESTAURANT = "restaurant";
    public static final String KEY_SHOPPING_MALL= "shopping_mall";
    public static final String KEY_SPA = "spa";
    public static final String KEY_STADIUM = "stadium";
    public static final String KEY_TOURIST_ATTRACTION = "tourist_attraction";
    public static final String KEY_NAME = "name";
    public static final String KEY_PLACE_ID = "place_id";
    public static final String KEY_TYPES = "types";
    public static final String KEY_VICINITY = "vicinity";
    public static final String KEY_LATITUDE = "lat";
    public static final String KEY_LONGITUDE = "lng";
    public static final String KEY_ALL_LANDMARKS = "all_landmarks";
    public static final String KEY_NOT_VISITED_LANDMARKS = "not_visited_landmarks";
    public static final String KEY_OBJECT_ID = "objectId";
    public static final String KEY_INTERESTS = "interests";
    public static final String KEY_PACE = "pace";
    public static final String KEY_LOCATION = "location";

    private Map<String, ArrayList<Integer>> mInterests = new HashMap<String, ArrayList<Integer>>() {
        {
            put(KEY_AMUSEMENT_PARK, new ArrayList<Integer>(
                    List.of(R.drawable.ferris_wheel, R.id.amusement_park)));
            put(KEY_AQUARIUM, new ArrayList<Integer>(
                    List.of(R.drawable.fish, R.id.aquarium)));
            put(KEY_ART_GALLERY, new ArrayList<Integer>(
                    List.of(R.drawable.palette, R.id.art_gallery)));
            put(KEY_BAKERY, new ArrayList<Integer>(
                    List.of(R.drawable.croissant, R.id.bakery)));
            put(KEY_CAFE, new ArrayList<Integer>(
                    List.of(R.drawable.mug, R.id.cafe)));
            put(KEY_MOVIE_THEATER, new ArrayList<Integer>(
                    List.of(R.drawable.popcorn, R.id.movie_theater)));
            put(KEY_MUSEUEM, new ArrayList<Integer>(
                    List.of(R.drawable.bank, R.id.museum)));
            put(KEY_NIGHT_CLUB, new ArrayList<Integer>(
                    List.of(R.drawable.glass_cheers, R.id.night_club)));
            put(KEY_PARK, new ArrayList<Integer>(
                    List.of(R.drawable.tree, R.id.park)));
            put(KEY_RESTAURANT, new ArrayList<Integer>(
                    List.of(R.drawable.food, R.id.restaurant)));
            put(KEY_SHOPPING_MALL, new ArrayList<Integer>(
                    List.of(R.drawable.shopping, R.id.shopping_mall)));
            put(KEY_SPA, new ArrayList<Integer>(
                    List.of(R.drawable.makeup_brush, R.id.spa)));
            put(KEY_STADIUM, new ArrayList<Integer>(
                    List.of(R.drawable.basketball, R.id.stadium)));
            put(KEY_TOURIST_ATTRACTION, new ArrayList<Integer>(
                    List.of(R.drawable.tourism, R.id.tourist_attraction)));
        }
    };
    private Button mSubmitBtn;
    private int mUserPace = 0;
    private View mRelaxed;
    private View mModerate;
    private View mIntense;
    private ArrayList<String> mUserInterests = new ArrayList<>();
    private ParseUser mCurrentUser = ParseUser.getCurrentUser();
    private FlexboxLayout mFlexboxLayout;
    private double mLatitude;
    private double mLongitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interests);

        mFlexboxLayout = findViewById(R.id.interestsView);
        // Initialize the SDK
        Places.initialize(getApplicationContext(), BuildConfig.MAPS_API_KEY);

        // TO DO: SET THE BACKGROUND TINT FOR ALREADY SELECTED INTERESTS IF THIS ACTIVITY IS REUSED
        // ON THE PROFILE PAGE TO ALLOW USERS TO EDIT THEIR INTERESTS
        for (String name : mInterests.keySet()){
            createNewInterest(name);
        }

        mSubmitBtn = findViewById(R.id.btnSubmit);
        mSubmitBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                // If the user does not click any intensity, set the intensity to 10 by default
                mUserPace = 10;

                mCurrentUser.put(KEY_INTERESTS, mUserInterests);
                mCurrentUser.put(KEY_PACE, mUserPace);
                mCurrentUser.saveInBackground();

                // Access the user's saved location
                ParseGeoPoint geoPoint = (ParseGeoPoint) mCurrentUser.get(KEY_LOCATION);
                // If the user has a saved location, retrieve the latitude and longitude coordinates from
                // the saved location
                if (geoPoint != null){
                    mLatitude = geoPoint.getLatitude();
                    mLongitude = geoPoint.getLongitude();

                    // If a user does not select any interests
                    if (mUserInterests.size() == 0){
                        mUserInterests.add(KEY_TOURIST_ATTRACTION);
                    }
                    queryAPI(mLatitude, mLongitude);
                }

                // Create a handler to delay the start of the Main Activity
                // Prevents navigating to the Home Fragment when the locations have not been stored
                // to the Parse database yet
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        navigateToMainActivity();
                    }
                }, 1000);

            }
        });

        mRelaxed = findViewById(R.id.relaxedView);
        mRelaxed.setOnClickListener(this);

        mModerate = findViewById(R.id.moderateView);
        mModerate.setOnClickListener(this);

        mIntense = findViewById(R.id.intenseView);
        mIntense.setOnClickListener(this);
    }

    @Override
    // Handle clicks on user interests
    public void onClick(View v) {
        View viewId = findViewById(v.getId());
        // When a user clicks on a pace type
        switch (v.getId()){
            case R.id.relaxedView:
                if (mUserPace == 5){
                    mUserPace = 0;
                    viewId.setBackgroundTintList(getColorStateList(R.color.light_gray));
                }
                else {
                    mUserPace = 5;
                    viewId.setBackgroundTintList(getColorStateList(R.color.dusty_green_light));
                    findViewById(R.id.moderateView).setBackgroundTintList(getColorStateList(R.color.light_gray));
                    findViewById(R.id.intenseView).setBackgroundTintList(getColorStateList(R.color.light_gray));
                }
                return;
            case R.id.moderateView:
                if (mUserPace == 10){
                    mUserPace = 0;
                    viewId.setBackgroundTintList(getColorStateList(R.color.light_gray));
                }
                else {
                    mUserPace = 10;
                    viewId.setBackgroundTintList(getColorStateList(R.color.pale_yellow));
                    findViewById(R.id.relaxedView).setBackgroundTintList(getColorStateList(R.color.light_gray));
                    findViewById(R.id.intenseView).setBackgroundTintList(getColorStateList(R.color.light_gray));
                }
                return;
            case R.id.intenseView:
                if (mUserPace == 20){
                    mUserPace = 0;
                    viewId.setBackgroundTintList(getColorStateList(R.color.light_gray));
                }
                else {
                    mUserPace = 20;
                    viewId.setBackgroundTintList(getColorStateList(R.color.dusty_red));
                    findViewById(R.id.relaxedView).setBackgroundTintList(getColorStateList(R.color.light_gray));
                    findViewById(R.id.moderateView).setBackgroundTintList(getColorStateList(R.color.light_gray));
                }
                return;
            default:
                mUserPace = 10;
        }

        // When the user clicks on an interest
        TextView apiType = viewId.findViewById(R.id.tvHiddenText);

        // If the view is not already selected and there are less than 5 interests already selected
        if (!viewId.isSelected() && mUserInterests.size() < 5){
            viewId.setSelected(true);
            mUserInterests.add((String) apiType.getText());
            viewId.setBackgroundTintList(getColorStateList(R.color.dusty_green_light));
        }
        else {
            viewId.setSelected(false);
            mUserInterests.remove(apiType.getText());
            viewId.setBackgroundTintList(getColorStateList(R.color.light_gray));
        }
    }

    // Add new interest to the screen
    private void createNewInterest(String name){
        // Get the name of the landmark and capitalize letters
        String interestName = name.replaceAll("_", " ");
        interestName = interestName.substring(0, 1).toUpperCase() + interestName.substring(1).toLowerCase();
        for (int i=0; i<interestName.length(); i++){
            if (interestName.charAt(i) == ' '){
                interestName = interestName.substring(0, i+1) + interestName.substring(i+1, i+2).toUpperCase() + interestName.substring(i+2).toLowerCase();
            }
        }

        // Create a new interest layout
        LinearLayout interestLayout = new LinearLayout(InterestsActivity.this);
        interestLayout.setId(mInterests.get(name).get(1));
        interestLayout.setOnClickListener(this);
        interestLayout.setSelected(false);
        interestLayout.setOrientation(LinearLayout.HORIZONTAL);
        interestLayout.setBackground(getDrawable(R.drawable.rounded_background));
        interestLayout.setPadding(10, 10, 10, 10);
        // Set the margins for the interest layout
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(10, 10, 10, 10);
        interestLayout.setLayoutParams(layoutParams);

        // Create image view for the icon
        ImageView ivInterestIcon = new ImageView(InterestsActivity.this);
        ivInterestIcon.setImageResource(mInterests.get(name).get(0));
        interestLayout.addView(ivInterestIcon);

        // Create text view for the interest layout
        TextView tvInterest = new TextView(InterestsActivity.this);
        tvInterest.setText(interestName);
        // Set margins for the text view
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        textParams.setMargins(10, 10, 10, 10);
        tvInterest.setLayoutParams(textParams);
        interestLayout.addView(tvInterest);

        // Create hidden text view for the interest layout that will be used to make the API query
        TextView tvHiddenText = new TextView(InterestsActivity.this);
        tvHiddenText.setId(R.id.tvHiddenText);
        tvHiddenText.setText(name);
        tvHiddenText.setVisibility(View.GONE);
        interestLayout.addView(tvHiddenText);

        mFlexboxLayout.addView(interestLayout);
    }

    // Save the queried locations to the Location class in the Parse Database
    // Check for duplicate place_id's before adding
    private void saveLocation(Location location, String place_id) throws JSONException {
        ParseUser currentUser = ParseUser.getCurrentUser();
        ParseQuery<ParseObject> placeIdQuery = ParseQuery.getQuery("Location");
        placeIdQuery.whereEqualTo(KEY_PLACE_ID, place_id);
        placeIdQuery.getFirstInBackground(new GetCallback<ParseObject>() {
            public void done(ParseObject object, ParseException e) {
                ParseQuery<ParseObject> objectIdQuery = ParseQuery.getQuery("Location");
                if (object != null){
                    objectIdQuery.whereEqualTo(KEY_OBJECT_ID, object.getObjectId());
                    objectIdQuery.getFirstInBackground(new GetCallback<ParseObject>() {
                        public void done(ParseObject object, ParseException e) {
                            if (e == null) {
                                Log.d(TAG, "Object exists!");
                                currentUser.add(KEY_ALL_LANDMARKS, object);
                                currentUser.add(KEY_NOT_VISITED_LANDMARKS, object);
                                currentUser.saveInBackground();
                            } else {
                                Log.d(TAG, "Error!");
                            }
                        }
                    });
                }

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
                                    Toast.makeText(InterestsActivity.this, "Error while saving!", Toast.LENGTH_SHORT).show();
                                }
                                Log.i(TAG, "Location save was successful!");
                            }
                        });
                    }
                    else {
                        Log.d(TAG, "Error: " + e.getCode());
                    }
                }
            }
        });
    }

    // Query the Places API
    private void queryAPI(double latitude, double longitude){

        // Filters landmarks based on the user's selected interests
        for (int i=0; i<mUserInterests.size(); i++){
            String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" + latitude +  "%2C" + longitude + "&radius=30000&type=" + mUserInterests.get(i) + "&key=" + BuildConfig.MAPS_API_KEY;
            AsyncHttpClient client = new AsyncHttpClient();
            client.get(url, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Headers headers, JSON json) {
                    JSONObject jsonObject = json.jsonObject;
                    try {
                        JSONArray jsonArray = jsonObject.getJSONArray("results");

                        // Add landmarks based on the user's pace
                        // Add 5 landmarks if the user selects "relaxed", add 10 landmarks if the user
                        // selects "moderate", add 20 landmarks if the user selects "intense"; if the
                        // user does not select an intensity, the app default adds 10 landmarks for the
                        // user
                        for (int j=0; j<mUserPace/mUserInterests.size(); j++) {

                            JSONObject locationObject = jsonArray.getJSONObject(j);

                            // Create a new location object
                            Location newLocation = new Location();
                            newLocation.setName(locationObject.getString(KEY_NAME));
                            newLocation.setPlaceId(locationObject.getString(KEY_PLACE_ID));
                            newLocation.setTypes(locationObject.getJSONArray(KEY_TYPES));
                            newLocation.setVicinity(locationObject.getString(KEY_VICINITY));

                            JSONObject coordinates = locationObject.getJSONObject("geometry").getJSONObject("location");
                            double lat = coordinates.getDouble(KEY_LATITUDE);
                            double lng = coordinates.getDouble(KEY_LONGITUDE);
                            newLocation.setCoordinates(new ParseGeoPoint(lat, lng));

                            //Update the Parse database with the user's nearby landmarks
                            saveLocation(newLocation, locationObject.getString(KEY_PLACE_ID));
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

    // Start new intent to navigate to the main activity
    private void navigateToMainActivity() {
        // Get the DAO
        final User.UserDao userDao = ((DatabaseApplication) getApplicationContext()).getUserDatabase().userDao();
        ((DatabaseApplication) getApplicationContext()).getUserDatabase().runInTransaction(new Runnable() {
            @Override
            public void run() {
                try {
                    OnLocationsLoaded onLocationsLoaded = new OnLocationsLoaded() {
                        @Override
                        public void updateNotVisited(String string) {
                            Log.d("InterestsActivity", "Not Visited Loaded");
                            userDao.updateNotVisited(string);
                        }

                        @Override
                        public void updateVisited(String string) {
                            Log.d("InterestsActivity", "Visited Loaded");
                            userDao.updateVisited(string);
                        }

                        @Override
                        public void updateAll(String string) {
                            Log.d("InterestsActivity", "All Loaded");
                            userDao.updateAll(string);

                            Intent intent = new Intent(InterestsActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    };
                    User newUser = new User(ParseUser.getCurrentUser(), onLocationsLoaded);
                    userDao.insertUser(newUser);
                } catch (JSONException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}