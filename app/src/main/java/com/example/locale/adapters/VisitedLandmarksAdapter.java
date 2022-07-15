package com.example.locale.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.locale.R;
import com.example.locale.models.User;

import org.json.JSONException;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;

public class VisitedLandmarksAdapter extends RecyclerView.Adapter<VisitedLandmarksAdapter.ViewHolder> {
    private Context mContext;
    private ArrayList<String> mLocationNames;
    private User mUser;
    private HashMap<String, byte[]> mLocationByteHashMap;

    public VisitedLandmarksAdapter(Context context, ArrayList<String> locationNames, User user) throws JSONException, ParseException {
        this.mContext = context;
        this.mLocationNames = locationNames;
        this.mUser = user;
        this.mLocationByteHashMap = user.getVisitedPhotos();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_visited_landmark, parent, false);
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
        private ImageView ivVisitedLandmarkPhoto;

        public ViewHolder(View itemView) {
            super(itemView);
            tvVisitedLandmarkName = itemView.findViewById(R.id.tvVisitedLandmarkName);
            ivVisitedLandmarkPhoto = itemView.findViewById(R.id.ivVisitedLandmarkPhoto);
        }

        public void bind(String landmarkName) {
            tvVisitedLandmarkName.setText(landmarkName);
            byte[] image = mLocationByteHashMap.get(landmarkName);
            if (image.length != 0){
                byte[] bitmapData = Base64.getDecoder().decode(image);
                Bitmap bitmap = BitmapFactory.decodeByteArray(bitmapData, 0, bitmapData.length);

                RequestOptions requestOptions = new RequestOptions();
                requestOptions = requestOptions.transforms(new CenterCrop(), new RoundedCorners(10));
                Glide.with(mContext).asBitmap().load(bitmap).apply(requestOptions.override(600, 300)).into(ivVisitedLandmarkPhoto);
            }
        }
    }
}

