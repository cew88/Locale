package com.example.locale.fragments;

import static com.example.locale.models.Constants.*;

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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.locale.R;
import com.example.locale.models.Converters;
import com.example.locale.models.Location;
import com.example.locale.models.Post;
import com.example.locale.models.User;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Base64;

public class ReviewFragment extends DialogFragment {
    private View vUpload;
    private User mUser;
    private String mPlaceName;
    private String mPlaceId;
    private String mObjectId;
    private TextView mTvLocationName;
    private RatingBar mRatingBar;
    private TextView mTvReview;
    private ImageView mIvImage;
    private Button mBtnSubmit;
    private LinearLayout mLlUpload;
    private final int GALLERY_REQUEST_CODE = 1000;
    private AddPhoto mAddPhotoListener;
    private byte[] mByteArray;

    public interface AddPhoto {
        public void addPhoto(String object_id, String place_id, String place_name, byte[] bytes) throws JSONException, UnsupportedEncodingException;
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
            throw new ClassCastException(context + " must implement MyListFragment.OnItemSelectedListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Get data passed from bundle
        mUser = this.getArguments().getParcelable("User");
        mPlaceName = this.getArguments().getString(KEY_PLACE_NAME);
        mPlaceId = this.getArguments().getString(KEY_PLACE_ID);
        mObjectId = this.getArguments().getString(KEY_OBJECT_ID);

        // Inflate the layout for this fragment
        getDialog().setTitle("review dialog");
        return inflater.inflate(R.layout.fragment_location_visited_review, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mTvLocationName = view.findViewById(R.id.tvLocationVisitedName);
        mTvLocationName.setText(mPlaceName);

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
                // Save photos and reviews to the Parse location object
                ParseQuery<ParseObject> query = ParseQuery.getQuery("Location");
                query.whereEqualTo(KEY_OBJECT_ID, mObjectId);
                query.getFirstInBackground(new GetCallback<ParseObject>() {
                    public void done(ParseObject object, ParseException parseException) {
                        if (parseException == null) {
                            Location location = (Location) object;

                            // Get the user rating for the location
                            mRatingBar = view.findViewById(R.id.ratingBar);
                            double rating = mRatingBar.getRating();
                            location.setTotalRating(location.getTotalRating() + rating);

                            // Increase the visited count for the location
                            location.setVisitedCount(location.getVisitedCount()+1);

                            // Create a new post
                            Post newPost = new Post();
                            newPost.setUsername(mUser.getUserName());
                            newPost.setPlaceId(mPlaceId);
                            newPost.setPlaceName(mPlaceName);

                            try {
                                mAddPhotoListener.addPhoto(mObjectId, mPlaceId, mPlaceName, mByteArray);

                                if (mByteArray != null){
                                    location.add(KEY_PHOTOS_LIST, mByteArray);

                                    String encodedImage = Base64.getEncoder().encodeToString(mByteArray);
                                    newPost.setPhoto(encodedImage);
                                }
                            } catch (JSONException | UnsupportedEncodingException exception) {
                                exception.printStackTrace();
                            }

                            mTvReview = view.findViewById(R.id.etReview);
                            String review = mTvReview.getText().toString();

                            if (!review.isEmpty()){
                                location.add(KEY_REVIEWS_LIST, review);
                                newPost.setReview(review);
                            }

                            newPost.saveInBackground();
                        }
                        object.saveInBackground();
                    }
                });

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

}