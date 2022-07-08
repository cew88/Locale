package com.example.locale.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.locale.R;

import java.util.ArrayList;

public class VisitedLandmarksAdapter extends RecyclerView.Adapter<VisitedLandmarksAdapter.ViewHolder> {
    private Context mContext;
    private ArrayList<String> mLocationNames;

    public VisitedLandmarksAdapter(Context context, ArrayList<String> locationNames){
        this.mContext = context;
        this.mLocationNames = locationNames;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_visited, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VisitedLandmarksAdapter.ViewHolder holder, int position) {
        String locationName = mLocationNames.get(position);
        holder.bind(locationName);
    }

    @Override
    public int getItemCount() {
        return mLocationNames.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private TextView tvVisitedLandmarkName;

        public ViewHolder(View itemView) {
            super(itemView);
            tvVisitedLandmarkName = itemView.findViewById(R.id.tvVisitedLandmarkName);
        }

        public void bind(String landmarkName) {
            tvVisitedLandmarkName.setText(landmarkName);
        }
    }
}