/*
Interests activity allows users to select categories of locations that they are interested in
visiting. Selected items change color on click (color toggles base don whether or not the category
is selected). Submitting these categories saves their interests to the Parse database. These
categories are then used in the query to the Places API to filter nearby locations by user interest.
 */

package com.example.locale;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.google.android.flexbox.FlexboxLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private ArrayList<String> userInterests = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interests);

        FlexboxLayout flexboxLayout = findViewById(R.id.interestsView);

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

//        submitBtn = findViewById(R.id.btnSubmit);
//        submitBtn.setOnClickListener(new View.OnClickListener(){
//            @Override
//            public void onClick(View v) {
//                try {
//                    saveInterests(userInterests);
//                } catch (ParseException e) {
//                    e.printStackTrace();
//                }
//            }
//        });
    }


    @Override
    public void onClick(View v) {
        View interest = findViewById(v.getId());
        TextView apiType = interest.findViewById(R.id.tvHiddenText);
        if (!interest.isSelected()){
            interest.setSelected(true);
            userInterests.add((String) apiType.getText());
            interest.setBackground(getDrawable(R.drawable.selected_rounded_background));
        }
        else {
            interest.setSelected(false);
            userInterests.remove(apiType.getText());
            interest.setBackground(getDrawable(R.drawable.rounded_background));
        }
    }

    // Save user interests
//    private void saveInterests(ArrayList<String> arrayList) throws ParseException {
//        ParseUser currentUser = ParseUser.getCurrentUser();
//        currentUser.put("interests", arrayList);
//        currentUser.saveInBackground();
//    }
}