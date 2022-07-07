/*
Creates a fragment for the home screen which displays the list of landmarks that the user has not
visited yet.
 */

package com.example.locale.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.locale.Converters;
import com.example.locale.OnLocationsLoaded;
import com.example.locale.adapters.HomeLandmarksAdapter;
import com.example.locale.models.Location;
import com.example.locale.R;
import com.example.locale.models.User;

import org.json.JSONException;

import java.util.ArrayList;

public class HomeFragment extends Fragment {
    private RecyclerView mRvLandmarks;
    private ArrayList<Location> mLandmarks;
    private HomeLandmarksAdapter mAdapter;
    private User mUser;

    // Required empty public constructor
    public HomeFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Get data passed from bundle
        mUser = this.getArguments().getParcelable("User");

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize the list of landmarks and adapter
        mLandmarks = new ArrayList<Location>();
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

        // Create pop up dialog
        LocationVisitedFragment locationVisitedFragment = new LocationVisitedFragment();
        //locationVisitedFragment.show(getFragmentManager(), "visited dialog");
    }
}