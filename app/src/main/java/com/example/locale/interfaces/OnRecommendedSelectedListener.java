package com.example.locale.interfaces;

import com.example.locale.models.Location;
import org.json.JSONException;

// Create interface to check when a recommended location is added
public interface OnRecommendedSelectedListener {
    public void updateRecommended(Location location) throws JSONException;
    public void updateNotVisited(Location location) throws JSONException;
}