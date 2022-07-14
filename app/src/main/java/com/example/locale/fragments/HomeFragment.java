/*
Creates a fragment for the home screen which displays the list of landmarks that the user has not
visited yet.
 */

package com.example.locale.fragments;

import static com.example.locale.models.Constants.KEY_OBJECT_ID;
import static com.example.locale.models.Constants.KEY_PLACE_ID;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.locale.adapters.HomeLandmarksAdapter;
import com.example.locale.models.Converters;
import com.example.locale.models.Location;
import com.example.locale.R;
import com.example.locale.models.User;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class HomeFragment extends Fragment {
    private RecyclerView mRvLandmarks;
    private RecyclerView mRvRecommendedLandmarks;
    private TextView mRecommendedDescription;
    private ArrayList<Location> mLandmarks;
    private ArrayList<Location> mRecommended;
    private HomeLandmarksAdapter mAdapter;
    private HomeLandmarksAdapter mRecommendedAdapter;
    private User mUser;
    private ArrayList<Location> mRecommendedLandmarks;

    public interface OnRecommendedListener {
        public void updateRecommended();
    }

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

        // Initialize the list of landmarks and adapter
        mLandmarks = new ArrayList<>();
        mAdapter = new HomeLandmarksAdapter(getContext(), mLandmarks);

        // Recycler view setup: layout manager and the adapter
        mRvLandmarks = view.findViewById(R.id.rvLandmarks);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        mRvLandmarks.setLayoutManager(linearLayoutManager);
        mRvLandmarks.setAdapter(mAdapter);

        try {
            mLandmarks.addAll(mUser.getNotVisited());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Initialize the list of landmarks and adapter
        mRecommended = new ArrayList<>();
        mRecommendedAdapter = new HomeLandmarksAdapter(getContext(), mRecommended);

        // Recycler view setup: layout manager and the adapter
        mRvRecommendedLandmarks = view.findViewById(R.id.rvRecommendedLandmarks);
        LinearLayoutManager linearLayoutManagerR = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        mRvRecommendedLandmarks.setLayoutManager(linearLayoutManagerR);

        mRvRecommendedLandmarks.setAdapter(mRecommendedAdapter);

        mRecommendedDescription = view.findViewById(R.id.tvRecommendedLandmark);

        try {
            if (mUser.getRecommended() != null) {
               try {
                   mRecommendedDescription.setVisibility(View.VISIBLE);
                   mRvRecommendedLandmarks.setVisibility(View.VISIBLE);
                   mRecommended.addAll(mUser.getRecommended());
               } catch (JSONException e) {
                   e.printStackTrace();
               }
           }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}