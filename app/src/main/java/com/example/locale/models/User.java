/*
Class represents the Parse User. Saving the Parse User's information in a Java Object reduces the
number of queries made to the Parse database.
*/

package com.example.locale.models;

import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Entity;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.PrimaryKey;
import androidx.room.Query;
import androidx.room.Update;

import com.example.locale.interfaces.OnLocationsLoaded;
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
@Entity
public class User implements Parcelable{
    @ColumnInfo
    private String mFirstName;

    @ColumnInfo
    private String mLastName;

    @ColumnInfo @PrimaryKey @NonNull
    private String mUserName;

    @ColumnInfo
    private String mEmail;

    @ColumnInfo
    private double mLatitude;

    @ColumnInfo
    private double mLongitude;

    @ColumnInfo
    private int mUserPace;

    @ColumnInfo
    private String mInterestsString;

    @ColumnInfo
    private String mVisitedString;

    @ColumnInfo
    private String mNotVisitedString;

    @ColumnInfo
    private String mAllString;

    public User(){}

    public User(ParseUser user, OnLocationsLoaded onLocationsLoaded) throws JSONException, InterruptedException {
        final OnLocationsLoaded mOnLocationsLoaded = onLocationsLoaded;

        // Initialize class variables
        this.mFirstName = user.getString("first_name");
        this.mLastName = user.getString("last_name");
        this.mUserName = user.getString("username");
        this.mEmail = user.getEmail();

        ParseGeoPoint location = user.getParseGeoPoint("location");
        this.mLatitude = location.getLatitude();
        this.mLongitude = location.getLongitude();

        ArrayList<String> mInterests = new ArrayList<String>();
        HashMap<Location, Date> mVisited= new HashMap<Location, Date>(){};
        ArrayList<Location> mNotVisited = new ArrayList<Location>();
        ArrayList<Location> mAll = new ArrayList<Location>();

        // Iterate through the JSON Array of user interests returned by Parse and add to an ArrayList
        JSONArray userInterests = user.getJSONArray("interests");
        for (int i=0; i<userInterests.length(); i++){
            mInterests.add((String) userInterests.get(i));
        }

        this.mInterestsString = Converters.fromStringArrayList(mInterests);

        // Iterate through the JSON Array of not visited landmarks returned by Parse and add to an ArrayList
        JSONArray notVisitedLandmarks = user.getJSONArray("not_visited_landmarks");
        for (int j=0; j<notVisitedLandmarks.length(); j++){
            JSONObject jsonObject = null;
            try {
                jsonObject = (JSONObject) notVisitedLandmarks.get(j);
                String objectId = null;
                objectId = jsonObject.getString("objectId");
                ParseQuery<ParseObject> query = ParseQuery.getQuery("Location");
                query.whereEqualTo("objectId", objectId);
                query.getFirstInBackground(new GetCallback<ParseObject>() {

                    public void done(ParseObject object, ParseException e) {
                        if (e == null) {
                            mNotVisited.add((Location) object);
                            try {
                                if (mNotVisited.size() == notVisitedLandmarks.length()) {
                                    String notVisitedString = Converters.fromLocationArrayList(mNotVisited);
                                    setNotVisitedString(notVisitedString);
                                    mOnLocationsLoaded.updateNotVisited(notVisitedString);
                                }
                            } catch (JSONException ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
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
                            mVisited.put(visitedLocation, dateVisited);

                            if (mVisited.size() == visitedLandmarks.length()){
                                try {
                                    String visitedString = Converters.fromLocationHashMap(mVisited);
                                    setVisitedString(visitedString);
                                    mOnLocationsLoaded.updateVisited(visitedString);
                                } catch (JSONException ex) {
                                    ex.printStackTrace();
                                }
                            }
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
                        if (mAll.size() == allLandmarks.length()){
                            try {
                                String allString = Converters.fromLocationArrayList(mAll);
                                setAllString(allString);
                                mOnLocationsLoaded.updateAll(allString);
                            } catch (JSONException ex) {
                                ex.printStackTrace();
                            }
                        }
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
        dest.writeString(mInterestsString);
        dest.writeString(mNotVisitedString);
        dest.writeString(mAllString);
        dest.writeInt(mUserPace);
    }

    public String getFirstName() {
        return this.mFirstName;
    }

    public void setFirstName(String firstName){
        this.mFirstName = firstName;
    }

    public String getLastName(){
        return this.mLastName;
    }

    public void setLastName(String lastName){
        this.mLastName = lastName;
    }

    public String getUserName(){
        return this.mUserName;
    }

    public void setUserName(String userName){
        this.mUserName = userName;
    }

    public String getEmail(){
        return this.mEmail;
    }

    public void setEmail(String email){
        this.mEmail = email;
    }

    public double getLatitude(){
        return this.mLatitude;
    }

    public void setLatitude(double latitude){
        this.mLatitude = latitude;
    }

    public double getLongitude(){
        return this.mLongitude;
    }

    public void setLongitude(double longitude){
        this.mLongitude = longitude;
    }

    public String getInterestsString() {
        return this.mInterestsString;
    }

    public void setInterestsString(String interests){
        this.mInterestsString = interests;
    }

    public ArrayList<String> getInterests(){
        return Converters.fromStringtoStringArrayList(getInterestsString());
    }

    public String getNotVisitedString(){
        return this.mNotVisitedString;
    }

    public void setNotVisitedString(String notVisited){
        this.mNotVisitedString = notVisited;
    }

    public ArrayList<Location> getNotVisited() throws JSONException {
        if (getNotVisitedString() != null){
            return Converters.fromStringtoLocationArrayList(getNotVisitedString());
        }
        return new ArrayList<Location>();
    }

    public ArrayList<String> getNotVisitedPlaceIds() throws JSONException {
        ArrayList<String> notVisitedPlaceIds = new ArrayList<String>();
        for (Location location : getNotVisited()){
            notVisitedPlaceIds.add(location.getPlaceId());
        }
        return notVisitedPlaceIds;
    }

    public String getVisitedString(){
        return this.mVisitedString;
    }

    public void setVisitedString(String visited){
        this.mVisitedString = visited;
    }

    public HashMap<String, Date> getVisited() throws JSONException {
        if (getVisitedString() != null){
            return Converters.fromStringtoHashMap(getVisitedString());
        }
        return new HashMap<String, Date>();
    }

    public String getAllString() {
        return this.mAllString;
    }

    public void setAllString(String all){
        this.mAllString = all;
    }

    public ArrayList<Location> getAll() throws JSONException {
        if (getAllString() != null) {
            return Converters.fromStringtoLocationArrayList(getAllString());
        }
        return new ArrayList<Location>();
    }

    public int getUserPace() {
        return this.mUserPace;
    }

    public void setUserPace(int userPace){
        this.mUserPace = userPace;
    }

    @Dao
    public interface UserDao {
        @Query("SELECT * FROM User where mUserName = :username")
        public User getByUsername(String username);

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        public Long insertUser(User user);

        @Query("UPDATE user SET mNotVisitedString = :notVisited")
        public void updateNotVisited(String notVisited);

        @Query("UPDATE user SET mVisitedString = :visited")
        public void updateVisited(String visited);

        @Query("UPDATE user SET mAllString = :all")
        public void updateAll(String all);

        @Update
        public void updateUser(User user);

        @Delete
        public void deleteUser(User user);
    }
}
