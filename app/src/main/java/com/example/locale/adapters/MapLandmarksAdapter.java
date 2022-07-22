/*
Creates the item views for each landmark listed as "unvisited" for each user. Handles marking the
locations as visited.
 */

package com.example.locale.adapters;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.locale.R;
import com.example.locale.interfaces.OnLocationClickedListener;
import com.example.locale.models.Location;
import org.json.JSONException;
import java.util.ArrayList;


public class MapLandmarksAdapter extends RecyclerView.Adapter<MapLandmarksAdapter.ViewHolder> {
    private Context mContext;
    private ArrayList<Location> mLandmarks;
    private OnLocationClickedListener mLocationClickedListener;

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

    @Override
    public int getItemCount() {
        return mLandmarks.size();
    }

    // Define a viewholder
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
}

