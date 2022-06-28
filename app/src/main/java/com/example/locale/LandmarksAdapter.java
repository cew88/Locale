/*
Creates the item views for each landmark listed as "unvisited" for each user. Handles marking the
locations as visited.
 */

package com.example.locale;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.parse.ParseUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


public class LandmarksAdapter extends RecyclerView.Adapter<LandmarksAdapter.ViewHolder> {
    public static final String KEY_NOT_VISITED_LANDMARKS = "not_visited_landmarks";
    public static final String KEY_VISITED_LANDMARKS = "visited_landmarks";

    private ParseUser mCurrentUser = ParseUser.getCurrentUser();
    private Context mContext;
    private ArrayList<Location> mLandmarks;
    private OnLocationVisitedListener mLocationVisitedListener;

    // Define an interface to notify the Main Activity that an update to the user information in the
    // Parse database has been made
    public interface OnLocationVisitedListener {
        public void updateLandmarks();
    }

    // Pass in the context and the list of landmarks
    public LandmarksAdapter(Context context, ArrayList<Location> landmarks) {
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
        ImageView ivLandmarkImage;
        TextView tvLandmarkName;
        TextView tvVicinity;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            ivLandmarkImage = itemView.findViewById(R.id.ivLandmarkImage);
            tvLandmarkName = itemView.findViewById(R.id.tvLandmarkName);
            tvVicinity = itemView.findViewById(R.id.tvVicinity);
        }

        public void bind(Location landmark) throws JSONException {
            tvLandmarkName.setText(landmark.getName());
            tvVicinity.setText(landmark.getVicinity());

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    // Toast.makeText(v.getContext(), "Location long clicked!", Toast.LENGTH_SHORT).show();
                    removeFromNotVisited(landmark);
                    try {
                        addToVisited(landmark);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    if (mContext instanceof OnLocationVisitedListener) {
                        mLocationVisitedListener = (OnLocationVisitedListener) mContext;
                        mLocationVisitedListener.updateLandmarks();
                    }
                    else {
                        throw new ClassCastException(mContext.toString());
                    }

                    return true;
                }
            });
        }
    }

    // If a user long clicks on a list item, the following function marks the location as visited
    // in the Parse database by removing the location from the array of not visited landmarks
    public void removeFromNotVisited(Location location) {
        ArrayList<Location> notVisited = mLandmarks;
        notVisited.remove(location);

        // Overwrite what is currently saved under the user's not visited landmarks
        mCurrentUser.put(KEY_NOT_VISITED_LANDMARKS, notVisited);
        mCurrentUser.saveInBackground();

        this.mLandmarks = notVisited;
        notifyDataSetChanged();
    }

    // If a user long clicks on a list item, the following function marks the location as visited
    // in the Parse database by adding the location to an array of visited landmarks; the location
    // is first incorporated into a JSON Object to store the date when the location was visited

    public void addToVisited(Location location) throws JSONException {
        Date currentTime = Calendar.getInstance().getTime();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(location.getObjectId(), currentTime);

        mCurrentUser.add(KEY_VISITED_LANDMARKS, String.valueOf(jsonObject));
        mCurrentUser.saveInBackground();
    }

    public void clear() {
        mLandmarks.clear();
        notifyDataSetChanged();
    }

    public void addAll(List<Location> locationList) {
        mLandmarks.addAll(locationList);
        notifyDataSetChanged();
    }
}

