/*
Creates a fragment for the home screen which displays the list of landmarks that the user has not
visited yet.
 */

package com.example.locale.fragments;

import static com.example.locale.activities.LoginActivity.connectedToNetwork;
import static com.example.locale.activities.MainActivity.showOfflineBanner;
import static com.example.locale.models.Constants.KEY_AMUSEMENT_PARK;
import static com.example.locale.models.Constants.KEY_AQUARIUM;
import static com.example.locale.models.Constants.KEY_ART_GALLERY;
import static com.example.locale.models.Constants.KEY_BAKERY;
import static com.example.locale.models.Constants.KEY_CAFE;
import static com.example.locale.models.Constants.KEY_MOVIE_THEATER;
import static com.example.locale.models.Constants.KEY_MUSEUM;
import static com.example.locale.models.Constants.KEY_NIGHT_CLUB;
import static com.example.locale.models.Constants.KEY_PARK;
import static com.example.locale.models.Constants.KEY_RESTAURANT;
import static com.example.locale.models.Constants.KEY_SHOPPING_MALL;
import static com.example.locale.models.Constants.KEY_SPA;
import static com.example.locale.models.Constants.KEY_STADIUM;
import static com.example.locale.models.Constants.KEY_TOURIST_ATTRACTION;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.locale.R;
import com.example.locale.adapters.HomeLandmarksAdapter;
import com.example.locale.adapters.RecommendedLandmarksAdapter;
import com.example.locale.models.Location;
import com.example.locale.models.User;

import org.json.JSONArray;
import org.json.JSONException;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeFragment extends Fragment {
    private RecyclerView mRvLandmarks;
    private RecyclerView mRvRecommendedLandmarks;

    private ArrayList<Location> mLandmarks;
    private ArrayList<Location> mRecommended;

    private HomeLandmarksAdapter mAdapter;
    private RecommendedLandmarksAdapter mRecommendedAdapter;
    private ArrayAdapter mDropdownAdapter;

    private User mUser;
    private ConstraintLayout mOfflineBanner;
    private TextView mDismiss;
    private TextView mRecommendedDescription;
    private AutoCompleteTextView mTvAutocomplete;

    private String mTypeToFilterBy;

    // Required empty public constructor
    public HomeFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Get data passed from bundle
        mUser = this.getArguments().getParcelable("User");

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Allow users to filter landmarks by their interests
        Map<String, String> interestsMap = new HashMap<>() {
            {
                put("Amusement Park", KEY_AMUSEMENT_PARK);
                put("Aquarium", KEY_AQUARIUM);
                put("Art Gallery", KEY_ART_GALLERY);
                put("Bakery", KEY_BAKERY);
                put("Cafe", KEY_CAFE);
                put("Movie Theater", KEY_MOVIE_THEATER);
                put("Museum", KEY_MUSEUM);
                put("Night Club", KEY_NIGHT_CLUB);
                put("Park", KEY_PARK);
                put("Restaurant", KEY_RESTAURANT);
                put("Shopping Mall", KEY_SHOPPING_MALL);
                put("Spa", KEY_SPA);
                put("Stadium", KEY_STADIUM);
                put("Tourist Attraction", KEY_TOURIST_ATTRACTION);
            }
        };

        ArrayList<String> interests = new ArrayList<>();
        for (String key : interestsMap.keySet()){
            interests.add(key);
        }

        mDropdownAdapter = new ArrayAdapter<String>(getContext(), R.layout.item_dropdown, interests);
        mTvAutocomplete = view.findViewById(R.id.autoCompleteTextView);
        mTvAutocomplete.setAdapter(mDropdownAdapter);

        mTvAutocomplete.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mTypeToFilterBy = mTvAutocomplete.getText().toString();

                try {
                    mLandmarks.clear();
                    for (Location landmark : mUser.getNotVisited()){
                        JSONArray landmarkTypes = landmark.getTypes();
                        if (String.valueOf(landmarkTypes).contains(interestsMap.get(mTypeToFilterBy))){
                            mLandmarks.add(landmark);
                        }
                    }
                    mAdapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        // Initialize the list of not visited landmarks and adapter
        mLandmarks = new ArrayList<>();
        mAdapter = new HomeLandmarksAdapter(getContext(), mLandmarks);

        // Recycler view setup: layout manager and the adapter
        mRvLandmarks = view.findViewById(R.id.rvLandmarks);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        mRvLandmarks.setLayoutManager(linearLayoutManager);
        mRvLandmarks.setAdapter(mAdapter);

        try {
            if (mTypeToFilterBy == null){
                mLandmarks.addAll(mUser.getNotVisited());
            }
            else {
                for (Location landmark : mUser.getNotVisited()){
                    JSONArray landmarkTypes = landmark.getTypes();

                    if (String.valueOf(landmarkTypes).contains(mTypeToFilterBy)){
                        mLandmarks.add(landmark);
                    }
                }

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Initialize the list of recommended landmarks and adapter
        mRecommended = new ArrayList<>();
        mRecommendedAdapter = new RecommendedLandmarksAdapter(getContext(), mRecommended);

        // Recycler view setup: layout manager and the adapter
        mRvRecommendedLandmarks = view.findViewById(R.id.rvRecommendedLandmarks);
        LinearLayoutManager linearLayoutManagerR = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        mRvRecommendedLandmarks.setLayoutManager(linearLayoutManagerR);

        mRvRecommendedLandmarks.setAdapter(mRecommendedAdapter);
        mRecommendedDescription = view.findViewById(R.id.tvRecommendedLandmark);

        try {
            if (!(mUser.getRecommended().isEmpty())) {
               mRecommendedDescription.setVisibility(View.VISIBLE);
               mRvRecommendedLandmarks.setVisibility(View.VISIBLE);
               mRecommended.addAll(mUser.getRecommended());

               // Adjust the size of mRvLandmarks if there are recommended landmarks displayed on the screen
               View parent = (View) view.getParent();
               ViewTreeObserver viewTreeObserver = mRvRecommendedLandmarks.getViewTreeObserver();
               if (viewTreeObserver.isAlive()) {
                   viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                       @Override
                       public void onGlobalLayout() {

                           int height = parent.getHeight() - mRvRecommendedLandmarks.getHeight() - 175;
                           ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) mRvLandmarks.getLayoutParams();
                           params.height = height;
                           mRvLandmarks.setLayoutParams(params);
                       }
                   });
               }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Reset the height of mRvLandmarks so that the recycler view starts below the text
        View parent = (View) view.getParent();
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) mRvLandmarks.getLayoutParams();
        params.height = parent.getHeight() - 80;
        mRvLandmarks.setLayoutParams(params);

        // Set the height of mRvLandmarks when the offline banner is shown and when the offline banner
        // is dismissed
        mOfflineBanner = view.findViewById(R.id.clOfflineBanner);
        mDismiss = view.findViewById(R.id.tvDismiss);
        if (!connectedToNetwork && showOfflineBanner){
            mOfflineBanner.setVisibility(View.VISIBLE);

            // Adjust the size of mRvLandmarks if there are recommended landmarks displayed on the screen
            ViewTreeObserver viewTreeObserver = mOfflineBanner.getViewTreeObserver();
            if (viewTreeObserver.isAlive()){
                viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {

                        int height = parent.getHeight() - mOfflineBanner.getHeight() - 110;
                        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) mRvLandmarks.getLayoutParams();
                        params.height = height;
                        mRvLandmarks.setLayoutParams(params);
                    }
                });
            }
        }

        mDismiss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOfflineBanner.setVisibility(View.GONE);
                showOfflineBanner = false;

                ViewTreeObserver viewTreeObserver = mOfflineBanner.getViewTreeObserver();
                if (viewTreeObserver.isAlive()){
                    viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {

                            int height = parent.getHeight() + mOfflineBanner.getHeight() - 320;
                            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) mRvLandmarks.getLayoutParams();
                            params.height = height;
                            mRvLandmarks.setLayoutParams(params);
                        }
                    });
                }
            }
        });
    }

}