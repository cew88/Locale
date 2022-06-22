package com.example.locale;

import static com.example.locale.MainActivity.TAG;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {
    RecyclerView rvLandmarks;
    List<Location> landmarks;
    LandmarksAdapter adapter;
    ParseUser currentUser = ParseUser.getCurrentUser();

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvLandmarks = view.findViewById(R.id.rvLandmarks);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        // Initialize the list of tweets and adapter
        landmarks = new ArrayList<>();
        adapter = new LandmarksAdapter(getContext(), landmarks);
        // Recycler view setup: layout manager and the adapter
        rvLandmarks.setLayoutManager(linearLayoutManager);
        rvLandmarks.setAdapter(adapter);

        JSONArray notVisitedLandmarks = currentUser.getJSONArray("not_visited_landmarks");
        for (int i=0; i<notVisitedLandmarks.length(); i++){
            try {
                Location l = new Location();
                JSONObject jsonObject = (JSONObject) notVisitedLandmarks.get(i);
                String objectId = jsonObject.getString("objectId");

                ParseQuery<ParseObject> query = ParseQuery.getQuery("Location");
                query.whereEqualTo("objectId", objectId);
                query.getFirstInBackground(new GetCallback<ParseObject>() {
                   public void done(ParseObject object, ParseException e) {
                       if (e == null) {
                           Log.d(TAG, "Object exists!");
                           landmarks.add((Location) object);
                           adapter.notifyDataSetChanged();
                           Log.d("HERE", String.valueOf(object.get("place_name")));
                       } else {
                           Log.d(TAG, "Error!");
                       }
                   }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        //landmarks.addAll();


    }
}