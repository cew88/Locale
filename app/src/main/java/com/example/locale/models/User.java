/*
Class represents the Parse User. Saving the Parse User's information in a Java Object reduces the
number of queries made to the Parse database.
*/

package com.example.locale.models;

import android.os.Parcelable;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

@Parcel
public class User implements Parcelable{
    private String mFirstName;
    private String mLastName;
    private String mUserName;
    private String mEmail;
    private double mLatitude;
    private double mLongitude;
    private ArrayList<String> mInterests = new ArrayList<String>();
    private HashMap<String, Date> mVisited= new HashMap<String, Date>(){};
    private ArrayList<Location> mNotVisited = new ArrayList<Location>();
    private ArrayList<Location> mAll = new ArrayList<Location>();
    private int mUserPace;

    public User(){}

    public User(ParseUser user) throws JSONException {
        // Initialize class variables
        this.mFirstName = user.getString("first_name");
        this.mLastName = user.getString("last_name");
        this.mUserName = user.getString("username");
        this.mEmail = user.getEmail();

        ParseGeoPoint location = user.getParseGeoPoint("location");
        this.mLatitude = location.getLatitude();
        this.mLongitude = location.getLongitude();

        // Iterate through the JSON Array of user interests returned by Parse and add to an ArrayList
        JSONArray userInterests = user.getJSONArray("interests");
        for (int i=0; i<userInterests.length(); i++){
            this.mInterests.add((String) userInterests.get(i));
        }

        // Iterate through the JSON Array of not visited landmarks returned by Parse and add to an ArrayList
        JSONArray notVisitedLandmarks = user.getJSONArray("not_visited_landmarks");
        for (int j=0; j<notVisitedLandmarks.length(); j++){
            JSONObject jsonObject = (JSONObject) notVisitedLandmarks.get(j);
            String objectId = jsonObject.getString("objectId");

            ParseQuery<ParseObject> query = ParseQuery.getQuery("Location");
            query.whereEqualTo("objectId", objectId);
            query.getFirstInBackground(new GetCallback<ParseObject>() {
                public void done(ParseObject object, ParseException e) {
                    if (e == null) {
                        mNotVisited.add((Location) object);
                    }
                }
            });
        }

        // Iterate through JSON Array of visited landmarks returned by Parse and add to HashMap
        JSONArray visitedLandmarks = user.getJSONArray("visited_landmarks");
        for (int k=0; k<visitedLandmarks.length(); k++){
            JSONObject jsonObject = new JSONObject((String) visitedLandmarks.get(k));
            String objectId = jsonObject.getString("objectId");

            // Create a new date format in the correct pattern
            SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy");
            try {
                // Convert the date from a String to a Date object
                Date dateVisited = dateFormat.parse(jsonObject.getString("date_visited"));

                // Query Parse to the get Location object from the stored objectId
                ParseQuery<ParseObject> query = ParseQuery.getQuery("Location");
                query.whereEqualTo("objectId", objectId);
                query.getFirstInBackground(new GetCallback<ParseObject>() {
                    public void done(ParseObject object, ParseException e) {
                        if (e == null) {
                            Location visitedLocation = (Location) object;
                            mVisited.put(visitedLocation.getName(), dateVisited);
                        }
                    }
                });
            } catch (java.text.ParseException e) {
                e.printStackTrace();
            }
        }

        // Iterate through the JSON Array of all landmarks returned by Parse and add to an ArrayList
        JSONArray allLandmarks = user.getJSONArray("not_visited_landmarks");
        for (int l=0; l<allLandmarks.length(); l++){
            JSONObject jsonObject = (JSONObject) notVisitedLandmarks.get(l);
            String objectId = jsonObject.getString("objectId");

            ParseQuery<ParseObject> query = ParseQuery.getQuery("Location");
            query.whereEqualTo("objectId", objectId);
            query.getFirstInBackground(new GetCallback<ParseObject>() {
                public void done(ParseObject object, ParseException e) {
                    if (e == null) {
                        mAll.add((Location) object);
                    }
                }
            });
        }

        this.mUserPace = user.getInt("pace");
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(android.os.Parcel dest, int flags) {
        dest.writeString(mFirstName);
        dest.writeString(mLastName);
        dest.writeString(mUserName);
        dest.writeString(mEmail);
        dest.writeDouble(mLatitude);
        dest.writeDouble(mLongitude);
        dest.writeStringList(mInterests);
        dest.writeTypedList(mNotVisited);
        dest.writeTypedList(mAll);
        dest.writeInt(mUserPace);
    }

    public String getFirstName() {
        return mFirstName;
    }

    public String getLastName(){
        return mLastName;
    }

    public String getUserName(){
        return mUserName;
    }

    public String getEmail(){
        return mEmail;
    }

    public double getLatitude(){
        return mLatitude;
    }

    public double getLongitude(){
        return mLongitude;
    }

    public ArrayList<String> getInterests() {
        return mInterests;
    }

    public ArrayList<Location> getNotVisitedLandmarks(){
        return mNotVisited;
    }

    public HashMap<String, Date> getVisitedLandmarks(){
        return mVisited;
    }

    public ArrayList<Location> getAllLandmarks() {
        return mAll;
    }

    public int getUserPace() {
        return mUserPace;
    }
}
