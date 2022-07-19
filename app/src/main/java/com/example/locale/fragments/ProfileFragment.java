/*
Creates a profile fragment that displays the user's first name, last name, username, and a provides
a button for users to log out of the app.
 */

package com.example.locale.fragments;

import static com.example.locale.models.Constants.KEY_DATE_VISITED;
import static com.example.locale.models.Constants.PROFILE_FRAGMENT_TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.locale.R;
import com.example.locale.activities.LoginActivity;
import com.example.locale.adapters.DateAdapter;
import com.example.locale.models.User;
import com.parse.ParseUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;

public class ProfileFragment extends Fragment {
    private ImageView mIvProfileImage;
    private Button mBtnLogout;
    private Button mBtnEditProfile;
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
            // Log.d("Profile Fragment", String.valueOf(mUser.getVisited()));
            for (int i=0; i<mUser.getVisited().size(); i++){
                JSONObject jsonObject = mUser.getVisited().get(i);
                String dateString = jsonObject.getString(KEY_DATE_VISITED).substring(0, 10);
                if (!uniqueDates.contains(dateString)){
                    uniqueDates.add(dateString);
                }
            }

            mDateAdapter = new DateAdapter(getContext(), uniqueDates, mUser);
            mRvDates = view.findViewById(R.id.rvDates);
            LinearLayoutManager dateLinearLayoutManager = new LinearLayoutManager(getContext());
            mRvDates.setLayoutManager(dateLinearLayoutManager);
            mRvDates.setAdapter(mDateAdapter);

            mPlacesVisitedCount.setText(String.valueOf(mUser.getVisited().size()));
        } catch (JSONException | ParseException e) {
            e.printStackTrace();
        }

        // Set views
        // TO DO: CHANGE PROFILE IMAGE
        mIvProfileImage.setImageResource(R.drawable.user);
        mTvName.setText(mUser.getFirstName() + " " + mUser.getLastName());
        mTvUsername.setText(mUser.getUserName());

        if (mUser.getInterests() == null){
            String interestsList = mUser.getInterests().get(0).replaceAll("_", " ");
            for (int i=1; i<mUser.getInterests().size(); i++){
                interestsList += ", " + mUser.getInterests().get(i).replaceAll("_", " ");;
            }
            mInterests.setText(interestsList);
        }

        mBtnLogout = view.findViewById(R.id.btnLogout);
        mBtnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ParseUser.logOut();
                // The following line of code should be null
                 ParseUser currentUser = ParseUser.getCurrentUser();
                Log.d(PROFILE_FRAGMENT_TAG, "Current user should be null: " + currentUser);
                Intent i = new Intent(getActivity().getApplicationContext(), LoginActivity.class);
                startActivity(i);
                getActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });

        mBtnEditProfile = view.findViewById(R.id.btnEditProfile);
        mBtnEditProfile.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putParcelable("User", mUser);
                FragmentManager mFragmentManager = getParentFragmentManager();
                Fragment fragment = new EditProfileFragment();
                fragment.setArguments(bundle);
                mFragmentManager.beginTransaction().replace(R.id.flContainer, fragment).commit();
            }
        });
    }
}