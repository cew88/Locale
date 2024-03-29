/*
Creates the item views for each landmark listed as "unvisited" for each user. Handles marking the
locations as visited.
 */

package com.example.locale.adapters;

import static com.example.locale.activities.LoginSplashActivity.connectedToNetwork;
import static com.example.locale.models.Constants.HOME_FRAGMENT_TAG;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.GranularRoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.locale.BuildConfig;
import com.example.locale.R;
import com.example.locale.interfaces.OnLocationVisitedListener;
import com.example.locale.models.Location;
import com.google.android.gms.common.api.ApiException;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HomeLandmarksAdapter extends RecyclerView.Adapter<HomeLandmarksAdapter.ViewHolder>{

    private Context mContext;
    private ArrayList<Location> mLandmarks;
    private OnLocationVisitedListener mLocationVisitedListener;

    // Pass in the context and the list of landmarks
    public HomeLandmarksAdapter(Context context, ArrayList<Location> landmarks) {
        this.mContext = context;
        this.mLandmarks = landmarks;
    }

    // For each row, inflate the layout
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_landmark, parent, false);
        return new ViewHolder(view);
    }

    // Bind values based on the position of the element
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Get the data at position
        Location landmark = mLandmarks.get(position);

        // Bind the landmark with the view holder
        try {
            holder.bind(landmark);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // Define a viewholder
    @Override
    public int getItemCount() {
        return mLandmarks.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivLandmarkImage;
        private TextView tvLandmarkName;
        private TextView tvVicinity;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            ivLandmarkImage = itemView.findViewById(R.id.ivLandmarkImage);
            tvLandmarkName = itemView.findViewById(R.id.tvLandmarkName);
            tvVicinity = itemView.findViewById(R.id.tvVicinity);
        }

        public void bind(Location landmark) throws JSONException {
            tvLandmarkName.setText(landmark.getName());
            tvVicinity.setText(landmark.getVicinity());

            // Get photo from place ID and display the photo; code below proviedd by Google documentation

            // Define a Place ID
            final String placeId = landmark.getPlaceId();

            if (connectedToNetwork){
                // Specify fields
                // Requests for photos must always have the PHOTO_METADATAS field
                final List<Place.Field> fields = Collections.singletonList(Place.Field.PHOTO_METADATAS);

                // Get a Place object (this example uses fetchPlace(), but you can also use findCurrentPlace())
                final FetchPlaceRequest placeRequest = FetchPlaceRequest.newInstance(placeId, fields);

                Places.initialize(mContext, BuildConfig.MAPS_API_KEY);
                PlacesClient placesClient = Places.createClient(mContext);

                placesClient.fetchPlace(placeRequest).addOnSuccessListener((response) -> {
                    final Place place = response.getPlace();

                    // Get the photo metadata
                    final List<PhotoMetadata> metadata = place.getPhotoMetadatas();
                    if (metadata == null || metadata.isEmpty()) {
                        Log.w(HOME_FRAGMENT_TAG, "No photo metadata.");
                        ivLandmarkImage.setVisibility(View.GONE);
                        return;
                    }
                    final PhotoMetadata photoMetadata = metadata.get(0);

                    // Create a FetchPhotoRequest
                    final FetchPhotoRequest photoRequest = FetchPhotoRequest.builder(photoMetadata)
                            .setMaxWidth(itemView.getMeasuredWidth())
                            .build();
                    placesClient.fetchPhoto(photoRequest).addOnSuccessListener((fetchPhotoResponse) -> {
                        ivLandmarkImage.setVisibility(View.VISIBLE);
                        Bitmap bitmap = fetchPhotoResponse.getBitmap();
                        RequestOptions requestOptions = new RequestOptions();
                        requestOptions = requestOptions.transforms(new CenterCrop(), new GranularRoundedCorners(30, 30, 0, 0));
                        Glide.with(mContext).asBitmap().load(bitmap).apply(requestOptions.override(itemView.getMeasuredWidth(), 400)).into(ivLandmarkImage);
                    }).addOnFailureListener((exception) -> {
                        if (exception instanceof ApiException) {
                            final ApiException apiException = (ApiException) exception;
                            Log.e(HOME_FRAGMENT_TAG, "Place not found: " + exception.getMessage());
                            ivLandmarkImage.setVisibility(View.GONE);
                            final int statusCode = apiException.getStatusCode();
                        }
                    });
                });

                itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        // Remove the landmark that was just marked as visited from the array of not visited locations
                        mLandmarks.remove(landmark);
                        notifyDataSetChanged();

                        if (mContext instanceof OnLocationVisitedListener) {
                            mLocationVisitedListener = (OnLocationVisitedListener) mContext;
                            try {
                                mLocationVisitedListener.removeFromNotVisited(landmark);
                                mLocationVisitedListener.updateLandmarks();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        else {
                            throw new ClassCastException(mContext.toString());
                        }
                        return true;
                    }
                });
            }
        }
    }
}

