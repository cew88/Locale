package com.example.locale.interfaces;
import com.example.locale.models.Location;
import org.json.JSONException;

// Define an interface to notify the Main Activity that an update to the user information in the
// Parse database has been made
public interface OnLocationVisitedListener {
    public void updateLandmarks();
    public void removeFromNotVisited(Location location) throws JSONException;
}