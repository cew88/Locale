package com.example.locale.fragments;

import static com.example.locale.models.Constants.KEY_OBJECT_ID;
import static com.example.locale.models.Constants.KEY_PLACE_ID;
import static com.example.locale.models.Constants.KEY_PLACE_NAME;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.locale.R;

public class LocationVisitedFragment extends DialogFragment {
    private String mPlaceName;
    private String mPlaceId;
    private String mObjectId;
    private Button mContinueBtn;

    public LocationVisitedFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Get data passed from bundle
        mPlaceName = this.getArguments().getString(KEY_PLACE_NAME);
        mPlaceId = this.getArguments().getString(KEY_PLACE_ID);
        mObjectId = this.getArguments().getString( KEY_OBJECT_ID);

        // Inflate the layout for this fragment
        getDialog().setTitle("visited dialog");
        return inflater.inflate(R.layout.fragment_location_visited, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mContinueBtn = view.findViewById(R.id.btnContinue);
        mContinueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();

                ReviewFragment reviewFragment = new ReviewFragment();
                Bundle locationBundle = new Bundle();
                locationBundle.putString(KEY_PLACE_NAME, mPlaceName);
                locationBundle.putString(KEY_PLACE_ID, mPlaceId);
                locationBundle.putString(KEY_OBJECT_ID, mObjectId);
                reviewFragment.setArguments(locationBundle);
                reviewFragment.show(getActivity().getSupportFragmentManager(), "review dialog");
            }
        });
    }

}