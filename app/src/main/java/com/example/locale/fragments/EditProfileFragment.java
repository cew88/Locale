package com.example.locale.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.locale.R;
import com.example.locale.models.User;

public class EditProfileFragment extends Fragment {
    private TextView mFirstName;
    private TextView mLastName;
    private TextView mEmail;

    private User mUser;


    public EditProfileFragment() {
        // Required empty public constructor
    }

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
        return inflater.inflate(R.layout.fragment_edit_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mFirstName = view.findViewById(R.id.etFirstNameEditProf);
        mFirstName.setText(mUser.getFirstName());

        mLastName = view.findViewById(R.id.etLastNameEditProf);
        mLastName.setText(mUser.getLastName());

        mEmail = view.findViewById(R.id.etEmailEditProf);
        mEmail.setText(mUser.getEmail());



    }
}