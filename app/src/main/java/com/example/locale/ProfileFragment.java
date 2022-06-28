/*
Creates a profile fragment that displays the user's first name, last name, username, and a provides
a button for users to log out of the app.
 */

package com.example.locale;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.parse.ParseUser;

public class ProfileFragment extends Fragment {
    private ImageView mIvProfileImage;
    private ImageView mIvLogout;
    private TextView mTvName;
    private TextView mTvUsername;
    User mUser;

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

        // Set views
        mTvName.setText(mUser.getFirstName() + " " + mUser.getLastName());
        mTvUsername.setText(mUser.getUserName());

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