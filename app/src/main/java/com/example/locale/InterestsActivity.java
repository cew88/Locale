/*
Interests activity allows users to select categories of locations that they are interested in
visiting. Selected items change color on click (color toggles base don whether or not the category
is selected). Submitting these categories saves their interests to the Parse database. These
categories are then used in the query to the Places API to filter nearby locations by user interest.
 */

package com.example.locale;

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
    public static final String TAG = "InterestsActivity";
    private Map<String, ArrayList<Integer>> interests = new HashMap<String, ArrayList<Integer>>() {
        {
            put("amusement_park", new ArrayList<Integer>(
                    List.of(R.drawable.ferris_wheel, R.id.amusement_park)));
            put("aquarium", new ArrayList<Integer>(
                    List.of(R.drawable.fish, R.id.aquarium)));
            put("art_gallery", new ArrayList<Integer>(
                    List.of(R.drawable.palette, R.id.art_gallery)));
            put("bakery", new ArrayList<Integer>(
                    List.of(R.drawable.croissant, R.id.bakery)));
            put("cafe", new ArrayList<Integer>(
                    List.of(R.drawable.mug, R.id.cafe)));
            put("movie_theater", new ArrayList<Integer>(
                    List.of(R.drawable.popcorn, R.id.movie_theater)));
            put("museum", new ArrayList<Integer>(
                    List.of(R.drawable.bank, R.id.museum)));
            put("night_club", new ArrayList<Integer>(
                    List.of(R.drawable.glass_cheers, R.id.night_club)));
            put("park", new ArrayList<Integer>(
                    List.of(R.drawable.tree, R.id.park)));
            put("restaurant", new ArrayList<Integer>(
                    List.of(R.drawable.food, R.id.restaurant)));
            put("shopping_mall", new ArrayList<Integer>(
                    List.of(R.drawable.shopping, R.id.shopping_mall)));
            put("spa", new ArrayList<Integer>(
                    List.of(R.drawable.makeup_brush, R.id.spa)));
            put("stadium", new ArrayList<Integer>(
                    List.of(R.drawable.basketball, R.id.stadium)));
            put("tourist_attraction", new ArrayList<Integer>(
                    List.of(R.drawable.tourism, R.id.tourist_attraction)));
        }
    };
    private Button submitBtn;
    private int userPace = 0;
    private View relaxed;
    private View moderate;
    private View intense;
    private ArrayList<String> userInterests = new ArrayList<String>();
    private ParseUser currentUser = ParseUser.getCurrentUser();
    private FlexboxLayout flexboxLayout;
    private double latitude;
    private double longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interests);

        flexboxLayout = findViewById(R.id.interestsView);
        // Initialize the SDK
        Places.initialize(getApplicationContext(), BuildConfig.MAPS_API_KEY);

        // TO DO: SET THE BACKGROUND TINT FOR ALREADY SELECTED INTERESTS IF THIS ACTIVITY IS REUSED
        // ON THE PROFILE PAGE TO ALLOW USERS TO EDIT THEIR INTERESTS
        for (String name : interests.keySet()){
            createNewInterest(name);
        }

        submitBtn = findViewById(R.id.btnSubmit);
        submitBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                // If the user does not click any intensity, set the intensity to 10 by default
                userPace = 10;

                currentUser.put("interests", userInterests);
                currentUser.put("pace", userPace);
                currentUser.saveInBackground();

                // Access the user's saved location
                ParseGeoPoint geoPoint = (ParseGeoPoint) currentUser.get("location");
                // If the user has a saved location, retrieve the latitude and longitude coordinates from
                // the saved location
                if (geoPoint != null){
                    latitude = geoPoint.getLatitude();
                    longitude = geoPoint.getLongitude();

                    // If a user does not select any interests
                    if (userInterests.size() == 0){
                        userInterests.add("tourist_attraction");
                    }
                    queryAPI(latitude, longitude);
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

        relaxed = findViewById(R.id.relaxedView);
        relaxed.setOnClickListener(this);

        moderate = findViewById(R.id.moderateView);
        moderate.setOnClickListener(this);

        intense = findViewById(R.id.intenseView);
        intense.setOnClickListener(this);
    }

    @Override
    // Handle clicks on user interests
    public void onClick(View v) {
        View viewId = findViewById(v.getId());
        // When a user clicks on a pace type
        switch (v.getId()){
            case R.id.relaxedView:
                if (userPace == 5){
                    userPace = 0;
                    viewId.setBackgroundTintList(getColorStateList(R.color.light_gray));
                }
                else {
                    userPace = 5;
                    viewId.setBackgroundTintList(getColorStateList(R.color.dusty_green_light));
                    findViewById(R.id.moderateView).setBackgroundTintList(getColorStateList(R.color.light_gray));
                    findViewById(R.id.intenseView).setBackgroundTintList(getColorStateList(R.color.light_gray));
                }
                return;
            case R.id.moderateView:
                if (userPace == 10){
                    userPace = 0;
                    viewId.setBackgroundTintList(getColorStateList(R.color.light_gray));
                }
                else {
                    userPace = 10;
                    viewId.setBackgroundTintList(getColorStateList(R.color.pale_yellow));
                    findViewById(R.id.relaxedView).setBackgroundTintList(getColorStateList(R.color.light_gray));
                    findViewById(R.id.intenseView).setBackgroundTintList(getColorStateList(R.color.light_gray));
                }
                return;
            case R.id.intenseView:
                if (userPace == 20){
                    userPace = 0;
                    viewId.setBackgroundTintList(getColorStateList(R.color.light_gray));
                }
                else {
                    userPace = 20;
                    viewId.setBackgroundTintList(getColorStateList(R.color.dusty_red));
                    findViewById(R.id.relaxedView).setBackgroundTintList(getColorStateList(R.color.light_gray));
                    findViewById(R.id.moderateView).setBackgroundTintList(getColorStateList(R.color.light_gray));
                }
                return;
            default:
                userPace = 10;
        }

        // When the user clicks on an interest
        TextView apiType = viewId.findViewById(R.id.tvHiddenText);

        // If the view is not already selected and there are less than 5 interests already selected
        if (!viewId.isSelected() && userInterests.size() < 5){
            viewId.setSelected(true);
            userInterests.add((String) apiType.getText());
            viewId.setBackgroundTintList(getColorStateList(R.color.dusty_green_light));
        }
        else {
            viewId.setSelected(false);
            userInterests.remove(apiType.getText());
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
        interestLayout.setId(interests.get(name).get(1));
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
        ivInterestIcon.setImageResource(interests.get(name).get(0));
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

        flexboxLayout.addView(interestLayout);
    }

    // Save the queried locations to the Location class in the Parse Database
    // Check for duplicate place_id's before adding
    private void saveLocation(Location location, String place_id) throws JSONException {
        ParseUser currentUser = ParseUser.getCurrentUser();

        ParseQuery<ParseObject> placeIdQuery = ParseQuery.getQuery("Location");
        placeIdQuery.whereEqualTo("place_id", place_id);
        placeIdQuery.getFirstInBackground(new GetCallback<ParseObject>() {
            public void done(ParseObject object, ParseException e) {
                ParseQuery<ParseObject> objectIdQuery = ParseQuery.getQuery("Location");
                if (object != null){
                    objectIdQuery.whereEqualTo("objectId", object.getObjectId());
                    objectIdQuery.getFirstInBackground(new GetCallback<ParseObject>() {
                        public void done(ParseObject object, ParseException e) {
                            if (e == null) {
                                Log.d(TAG, "Object exists!");
                                currentUser.add("all_landmarks", object);
                                currentUser.add("not_visited_landmarks", object);
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
                        Log.d(TAG, "Error!");
                    }
                }
            }
        });
    }

    // Query the Places API
    private void queryAPI(double latitude, double longitude){
        // Filters landmarks based on the user's selected interests
        for (int i=0; i<userInterests.size(); i++){
            String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" + latitude +  "%2C" + longitude + "&radius=30000&type=" + userInterests.get(i) + "&key=" + BuildConfig.MAPS_API_KEY;
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
                        for (int j=0; j<userPace/userInterests.size(); j++){

                            JSONObject locationObject = jsonArray.getJSONObject(j);

                            // Create a new location object
                            Location newLocation = new Location();
                            newLocation.setName(locationObject.getString("name"));
                            newLocation.setPlaceId(locationObject.getString("place_id"));
                            newLocation.setTypes(locationObject.getJSONArray("types"));
                            newLocation.setVicinity(locationObject.getString("vicinity"));

                            JSONObject coordinates = locationObject.getJSONObject("geometry").getJSONObject("location");
                            double lat = coordinates.getDouble("lat");
                            double lng = coordinates.getDouble("lng");
                            newLocation.setCoordinates(new ParseGeoPoint(lat, lng));

                            //Update the Parse database with the user's nearby landmarks
                            saveLocation(newLocation, locationObject.getString("place_id"));
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
        Intent intent = new Intent(InterestsActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}