package com.example.locale;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.LongDef;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import okhttp3.Headers;


public class LandmarksAdapter extends RecyclerView.Adapter<LandmarksAdapter.ViewHolder> {
    Context context;
    ArrayList<Location> landmarks;

    // Pass in the context and the list of tweets
    public LandmarksAdapter(Context context, ArrayList<Location> landmarks) {
        this.context = context;
        this.landmarks = landmarks;
    }

    // For each row, inflate the layout
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_landmark, parent, false);
        return new ViewHolder(view);
    }

    // Bind values based on the position of the element
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Get the data at position
        Location landmark = landmarks.get(position);

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
        return landmarks.size();
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
                    Toast.makeText(v.getContext(), "Location long clicked!", Toast.LENGTH_SHORT).show();
                    removeFromNotVisited(landmark);
                    return true;
                }
            });
        }
    }

    // If a user long clicks on a list item, it marks the location as visited in the Parse database
    // by remove the location from the array of not visited landmarks
    public void removeFromNotVisited(Location location) {
        ParseUser currentUser = ParseUser.getCurrentUser();
        ArrayList<Location> notVisited = landmarks;
        notVisited.remove(location);

        // Overwrite what is currently saved under the user's not visited landmarks
        currentUser.put("not_visited_landmarks", notVisited);
        currentUser.saveInBackground();

        this.landmarks = notVisited;
        notifyDataSetChanged();

    }

    public void clear() {
        landmarks.clear();
        notifyDataSetChanged();
    }

    public void addAll(List<Location> locationList) {
        landmarks.addAll(locationList);
        notifyDataSetChanged();
    }
}

