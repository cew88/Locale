package com.example.locale.adapters;

import static com.example.locale.models.Constants.KEY_DATE_VISITED;
import static com.example.locale.models.Constants.KEY_PLACE_NAME;

import android.content.Context;
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
        private TextView tvDate;

        public ViewHolder(View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvDateLandmarkVisited);
        }

        public void bind(String date) throws JSONException, ParseException {
            tvDate.setText(date);

            mVisitedLandmarksNames = new ArrayList<>();

            for (int i=0; i<mVisitedLandmarks.size(); i++){
                JSONObject landmark = mVisitedLandmarks.get(i);
                String dateValue = landmark.getString(KEY_DATE_VISITED).substring(0, 10);
                if (dateValue.equals(date)){
                    mVisitedLandmarksNames.add(landmark.getString(KEY_PLACE_NAME));
                }
            }

            mVisitedLandmarksAdapter = new VisitedLandmarksAdapter(itemView.getContext(), mVisitedLandmarksNames, mUser);
            mRvVisitedLandmarks = itemView.findViewById(R.id.rvVisitedLandmarks);
            LinearLayoutManager  visitedLinearLayoutManager = new LinearLayoutManager(itemView.getContext(), LinearLayoutManager.HORIZONTAL, false);

            mRvVisitedLandmarks.setLayoutManager(visitedLinearLayoutManager);
            mRvVisitedLandmarks.setAdapter(mVisitedLandmarksAdapter);
        }
    }
}
