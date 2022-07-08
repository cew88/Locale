package com.example.locale.fragments;

import static android.app.Activity.RESULT_OK;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.locale.R;
import com.example.locale.models.Location;

import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ReviewFragment extends DialogFragment {
    private View vUpload;
    private String mPlaceName;
    private String mPlaceId;
    private String mObjectId;
    private TextView mTvLocationName;
    private ImageView mIvImage;
    private Button mBtnSubmit;
    private LinearLayout mLlUpload;
    private final int GALLERY_REQUEST_CODE = 1000;
    private AddPhoto mAddPhotoListener;
    private byte[] mByteArray;

    public interface AddPhoto {
        public void addPhoto(String object_id, String place_id, String place_name, byte[] bytes) throws JSONException;
    }

    public ReviewFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof AddPhoto){
            mAddPhotoListener = (AddPhoto) context;
        }
        else {
            throw new ClassCastException(context.toString() + " must implement MyListFragment.OnItemSelectedListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Get data passed from bundle
        mPlaceName = this.getArguments().getString("Place Name");
        mPlaceId = this.getArguments().getString("Place Id");
        mObjectId = this.getArguments().getString("Object Id");

        // Inflate the layout for this fragment
        getDialog().setTitle("review dialog");
        return inflater.inflate(R.layout.fragment_location_visited_review, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mTvLocationName = view.findViewById(R.id.tvLocationVisitedName);
        mTvLocationName.setText(mPlaceName);
        Log.d("HERE", mPlaceName);

        mIvImage = view.findViewById(R.id.ivUploadedImage);

        mLlUpload = view.findViewById(R.id.llUpload);

        vUpload = view.findViewById(R.id.vUpload);
        vUpload.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                Intent imageGallery = new Intent(Intent.ACTION_PICK);
                imageGallery.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(imageGallery, GALLERY_REQUEST_CODE);
            }
        });

        mBtnSubmit = view.findViewById(R.id.btnSubmitReview);
        mBtnSubmit.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                try {
                    if (getmByteArray() != null){
                        mAddPhotoListener.addPhoto(mObjectId, mPlaceId, mPlaceName, getmByteArray());
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                dismiss();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == getActivity().RESULT_OK){
            if (requestCode == GALLERY_REQUEST_CODE){
                Uri imageUri = data.getData();
                mLlUpload.setVisibility(View.GONE);
                mIvImage.setVisibility(View.VISIBLE);
                mIvImage.setImageURI(imageUri);
                Bitmap bitmap = null;
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(this.getActivity().getContentResolver(), imageUri);
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    mByteArray = stream.toByteArray();
                    bitmap.recycle();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public byte[] getmByteArray(){
        return mByteArray;
    }
}