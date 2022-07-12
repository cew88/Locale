/*
Creates the item views for each landmark listed as "unvisited" for each user. Handles marking the
locations as visited.
 */

package com.example.locale.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.locale.R;
import com.example.locale.models.Location;
import com.parse.ParseUser;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;


public class MapLandmarksAdapter extends RecyclerView.Adapter<MapLandmarksAdapter.ViewHolder> {

    private ParseUser mCurrentUser = ParseUser.getCurrentUser();
    private Context mContext;
    private ArrayList<Location> mLandmarks;
    private OnLocationClickedListener mLocationClickedListener;

    // Define an interface to notify the Main Activity that an update to the user information in the
    // Parse database has been made
    public interface OnLocationVisitedListener {
        public void updateLandmarks();
    }

    public interface OnLocationClickedListener {
        public void zoomInOnMarkers(double latitude, double longitude);
    }

    // Pass in the context and the list of landmarks
    public MapLandmarksAdapter(Context context, ArrayList<Location> landmarks) {
        this.mContext = context;
        this.mLandmarks = landmarks;
    }

    public MapLandmarksAdapter(Context context, ArrayList<Location> landmarks, OnLocationClickedListener locationClickedListener) {
        this.mContext = context;
        this.mLandmarks = landmarks;
        this.mLocationClickedListener = locationClickedListener;
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
        private TextView tvLandmarkName;
        private TextView tvVicinity;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvLandmarkName = itemView.findViewById(R.id.tvLandmarkName);
            tvVicinity = itemView.findViewById(R.id.tvVicinity);
        }

        public void bind(Location landmark) throws JSONException {
            tvLandmarkName.setText(landmark.getName());
            tvVicinity.setText(landmark.getVicinity());

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mLocationClickedListener.zoomInOnMarkers(landmark.getCoordinates().getLatitude(), landmark.getCoordinates().getLongitude());
                }
            });
        }
    }


    // If a user long clicks on a list item, the following function marks the location as visited
    // in the Parse database by adding the location to an array of visited landmarks; the location
    // is first incorporated into a JSON Object to store the date when the location was visited

    public void clear() {
        mLandmarks.clear();
        notifyDataSetChanged();
    }

    public void addAll(List<Location> locationList) {
        mLandmarks.addAll(locationList);
        notifyDataSetChanged();
    }
}

