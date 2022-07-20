/*
Creates the item views for each date that a user visited a location. Each item view contains a
recycler view to allow users to scroll through the locations they visited on a certain day (if they
visited more than one location in a day)
 */


package com.example.locale.adapters;

import static com.example.locale.models.Constants.KEY_DATE_VISITED;
import static com.example.locale.models.Constants.KEY_PLACE_NAME;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.locale.R;
import com.example.locale.models.User;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;

public class DateAdapter extends RecyclerView.Adapter<DateAdapter.ViewHolder> {
    private Context mContext;
    private ArrayList<String> mDates;
    private User mUser;
    private String date;

    private TextView tvDate;

    private ArrayList<JSONObject> mVisitedLandmarks;
    private ArrayList<String> mVisitedLandmarksNames;
    private VisitedLandmarksAdapter mVisitedLandmarksAdapter;
    private RecyclerView mRvVisitedLandmarks;

    public DateAdapter(Context context, ArrayList<String> dates, User user) throws JSONException, ParseException {
        this.mContext = context;
        this.mDates = dates;
        this.mUser = user;
        this.mVisitedLandmarks = mUser.getVisited();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_date, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DateAdapter.ViewHolder holder, int position) {
        date = mDates.get(position);
        try {
            holder.bind(date);
        } catch (JSONException | ParseException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return mDates.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public ViewHolder(View itemView) {
            super(itemView);
            // Find views
            tvDate = itemView.findViewById(R.id.tvDateLandmarkVisited);
        }

        public void bind(String date) throws JSONException, ParseException {
            // Set the text to display the date
            tvDate.setText(date);

            // Get a list of the landmark names
            mVisitedLandmarksNames = new ArrayList<>();
            for (int i=0; i<mVisitedLandmarks.size(); i++){
                JSONObject landmark = mVisitedLandmarks.get(i);
                String dateValue = landmark.getString(KEY_DATE_VISITED).substring(0, 10);
                if (dateValue.equals(date)){
                    mVisitedLandmarksNames.add(landmark.getString(KEY_PLACE_NAME));
                }
            }
            Log.d("Visited Landmarks", String.valueOf(mVisitedLandmarksNames));

            // Recycler view setup: layout manager and the adapter
            mVisitedLandmarksAdapter = new VisitedLandmarksAdapter(itemView.getContext(), mVisitedLandmarksNames, mUser);
            mRvVisitedLandmarks = itemView.findViewById(R.id.rvVisitedLandmarks);
            LinearLayoutManager  visitedLinearLayoutManager = new LinearLayoutManager(itemView.getContext(), LinearLayoutManager.HORIZONTAL, false);

            mRvVisitedLandmarks.setLayoutManager(visitedLinearLayoutManager);
            mRvVisitedLandmarks.setAdapter(mVisitedLandmarksAdapter);
        }
    }
}
