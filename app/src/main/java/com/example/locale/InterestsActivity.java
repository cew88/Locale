/*
Interests activity allows users to select categories of locations that they are interested in
visiting. Selected items change color on click (color toggles base don whether or not the category
is selected). Submitting these categories saves their interests to the Parse database. These
categories are then used in the query to the Places API to filter nearby locations by user interest.
 */

package com.example.locale;

import androidx.annotation.LongDef;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.codepath.asynchttpclient.AsyncHttpClient;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;
import com.google.android.flexbox.FlexboxLayout;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interests);

        FlexboxLayout flexboxLayout = findViewById(R.id.interestsView);

        // TO DO: SET THE BACKGROUND TINT FOR ALREADY SELECTED INTERESTS IF THIS ACTIVITY IS REUSED
        // ON THE PROFILE PAGE TO ALLOW USERS TO EDIT THEIR INTERESTS
        for (String name : interests.keySet()){
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

        submitBtn = findViewById(R.id.btnSubmit);
        submitBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                currentUser.put("interests", userInterests);
                currentUser.put("pace", userPace);
                currentUser.saveInBackground();
                //navigateToMainActivity();
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
        }

        // When the user clicks on an interest
        TextView apiType = viewId.findViewById(R.id.tvHiddenText);
        if (!viewId.isSelected()){
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

    // Start new intent to navigate to the main activity
    private void navigateToMainActivity() {
        Intent intent = new Intent(InterestsActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}