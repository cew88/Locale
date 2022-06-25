package com.example.locale;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class HomeFragment extends Fragment {
    RecyclerView rvLandmarks;
    ArrayList<Location> landmarks;
    LandmarksAdapter adapter;
    User user;

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
        user = this.getArguments().getParcelable("User");

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvLandmarks = view.findViewById(R.id.rvLandmarks);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        // Initialize the list of tweets and adapter
        landmarks = new ArrayList<Location>();
        adapter = new LandmarksAdapter(getContext(), landmarks);
        // Recycler view setup: layout manager and the adapter
        rvLandmarks.setLayoutManager(linearLayoutManager);
        rvLandmarks.setAdapter(adapter);


        ArrayList<Location> notVisitedLandmarks = user.getNotVisitedLandmarks();
        landmarks.addAll(notVisitedLandmarks);
    }
}