/*
Creates a profile fragment that displays the user's first name, last name, username, and a provides
a button for users to log out of the app.
 */

package com.example.locale.fragments;

import static com.example.locale.activities.MainActivity.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.locale.activities.LoginActivity;
import com.example.locale.R;
import com.example.locale.adapters.DateAdapter;
import com.example.locale.models.User;
import com.parse.ParseUser;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.Date;

public class ProfileFragment extends Fragment {
    private ImageView mIvProfileImage;
    private ImageView mIvLogout;
    private TextView mTvName;
    private TextView mTvUsername;

    private TextView mPlacesVisitedCount;
    private TextView mInterests;

    private User mUser;
    private DateAdapter mDateAdapter;
    private RecyclerView mRvDates;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Get data passed from bundle
        mUser = this.getArguments().getParcelable("User");

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Find views
        mIvProfileImage = view.findViewById(R.id.ivProfileImage);
        mTvName = view.findViewById(R.id.tvName);
        mTvUsername = view.findViewById(R.id.tvUsername);
        mPlacesVisitedCount = view.findViewById(R.id.tvPlacesVisitedCount);
        mInterests = view.findViewById(R.id.tvInterestsList);

        // Set the user timeline list
        ArrayList<String> uniqueDates = new ArrayList<>();

        // Only run the following if the user has visited locations
        try {
            for (Date date : mUser.getVisited().values()){
                String dateString = date.toString().substring(0, 10);
                if (!uniqueDates.contains(dateString)){
                    uniqueDates.add(dateString);
                }
                mDateAdapter = new DateAdapter(getContext(), uniqueDates, mUser);
                mRvDates = view.findViewById(R.id.rvDates);
                LinearLayoutManager dateLinearLayoutManager = new LinearLayoutManager(getContext());
                mRvDates.setLayoutManager(dateLinearLayoutManager);
                mRvDates.setAdapter(mDateAdapter);

                mPlacesVisitedCount.setText(String.valueOf(mUser.getVisited().size()));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }


        // Set views
        // TO DO: CHANGE PROFILE IMAGE
        mIvProfileImage.setImageResource(R.drawable.bank);
        mTvName.setText(mUser.getFirstName() + " " + mUser.getLastName());
        mTvUsername.setText(mUser.getUserName());

        String interestsList = mUser.getInterests().get(0).replaceAll("_", " ");
        for (int i=1; i<mUser.getInterests().size(); i++){
            interestsList += ", " + mUser.getInterests().get(i).replaceAll("_", " ");;
        }

        mInterests.setText(interestsList);

        mIvLogout = view.findViewById(R.id.ivLogoutIcon);
        mIvLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ParseUser.logOut();
                // The following line of code should be null
                // ParseUser currentUser = ParseUser.getCurrentUser();
                Intent i = new Intent(getActivity().getApplicationContext(), LoginActivity.class);
                startActivity(i);
            }
        });





    }
}