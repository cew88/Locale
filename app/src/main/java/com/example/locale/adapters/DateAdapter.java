package com.example.locale.adapters;

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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class DateAdapter extends RecyclerView.Adapter<DateAdapter.ViewHolder> {
    private Context mContext;
    private ArrayList<String> mDates;
    private User mUser;
    private String date;

    private HashMap<String, Date> mVisitedLandmarks;;
    private ArrayList<String> mVisitedLandmarksNames;
    private VisitedLandmarksAdapter mVisitedLandmarksAdapter;
    private RecyclerView mRvVisitedLandmarks;

    public DateAdapter(Context context, ArrayList<String> dates, User user){
        this.mContext = context;
        this.mDates = dates;
        this.mUser = user;
        this.mVisitedLandmarks = mUser.getVisitedLandmarks();
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
        holder.bind(date);
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

        public void bind(String date) {
            tvDate.setText(date);

            mVisitedLandmarksNames = new ArrayList<String>();
            for (String landmarkName: mVisitedLandmarks.keySet()){

                Date dateValue = mVisitedLandmarks.get(landmarkName);
                if (dateValue.toString().substring(0, 10).equals(date)){
                    mVisitedLandmarksNames.add(landmarkName);
                }
            }

            mVisitedLandmarksAdapter = new VisitedLandmarksAdapter(itemView.getContext(), mVisitedLandmarksNames);
            mRvVisitedLandmarks = itemView.findViewById(R.id.rvVisitedLandmarks);
            LinearLayoutManager  visitedLinearLayoutManager = new LinearLayoutManager(itemView.getContext(), LinearLayoutManager.HORIZONTAL, false);

            mRvVisitedLandmarks.setLayoutManager(visitedLinearLayoutManager);
            mRvVisitedLandmarks.setAdapter(mVisitedLandmarksAdapter);
        }
    }
}
