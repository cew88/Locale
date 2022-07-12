/*
Class represents the Parse User. Saving the Parse User's information in a Java Object reduces the
number of queries made to the Parse database.
*/

package com.example.locale.models;

import static com.example.locale.models.Constants.*;

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
    private String mUserName = "";

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
        this.mFirstName = user.getString(KEY_FIRST_NAME);
        this.mLastName = user.getString(KEY_LAST_NAME);
        this.mUserName = user.getString(KEY_USERNAME);
        this.mEmail = user.getEmail();

        ParseGeoPoint location = user.getParseGeoPoint(KEY_LOCATION);
        this.mLatitude = location.getLatitude();
        this.mLongitude = location.getLongitude();

        ArrayList<String> mInterests = new ArrayList<>();
        HashMap<JSONObject, Date> mVisited= new HashMap<>(){};
        ArrayList<Location> mNotVisited = new ArrayList<>();
        ArrayList<Location> mAll = new ArrayList<>();

        // Iterate through the JSON Array of user interests returned by Parse and add to an ArrayList
        JSONArray userInterests = user.getJSONArray(KEY_INTERESTS);
        for (int i=0; i<userInterests.length(); i++){
            mInterests.add((String) userInterests.get(i));
        }
        this.mInterestsString = Converters.fromStringArrayList(mInterests);

        // Iterate through the JSON Array of not visited landmarks returned by Parse and add to an ArrayList
        JSONArray notVisitedLandmarks = user.getJSONArray(KEY_NOT_VISITED_LANDMARKS);
        for (int j=0; j<notVisitedLandmarks.length(); j++){
            JSONObject jsonObject;
            try {
                jsonObject = (JSONObject) notVisitedLandmarks.get(j);
                String objectId = jsonObject.getString(KEY_OBJECT_ID);
                ParseQuery<ParseObject> query = ParseQuery.getQuery("Location");
                query.whereEqualTo(KEY_OBJECT_ID, objectId);
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
        JSONArray visitedLandmarks = user.getJSONArray(KEY_VISITED_LANDMARKS);
        setVisitedString(String.valueOf(visitedLandmarks));
        mOnLocationsLoaded.updateVisited(String.valueOf(visitedLandmarks));


        // Iterate through the JSON Array of all landmarks returned by Parse and add to an ArrayList
        JSONArray allLandmarks = user.getJSONArray(KEY_ALL_LANDMARKS);
        for (int l=0; l<allLandmarks.length(); l++){
            JSONObject jsonObject = (JSONObject) notVisitedLandmarks.get(l);
            String objectId = jsonObject.getString(KEY_OBJECT_ID);

            ParseQuery<ParseObject> query = ParseQuery.getQuery("Location");
            query.whereEqualTo(KEY_OBJECT_ID, objectId);
            query.getFirstInBackground(new GetCallback<>() {
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
        this.mUserPace = user.getInt(KEY_PACE);
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
        return new ArrayList<>();
    }

    public ArrayList<String> getNotVisitedPlaceIds() throws JSONException {
        ArrayList<String> notVisitedPlaceIds = new ArrayList<>();
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

    public ArrayList<JSONObject> getVisited() throws JSONException, java.text.ParseException {
        if (getVisitedString() != null){
            ArrayList<JSONObject> visitedLandmarks = new ArrayList<>();
            JSONArray jsonArray = new JSONArray(getVisitedString());
            for (int i=0; i<jsonArray.length(); i++){
                visitedLandmarks.add(new JSONObject((String) jsonArray.get(i)));
            }
            return visitedLandmarks;
        }
        return new ArrayList<>();
    }

    public HashMap<String, byte[]> getVisitedPhotos() throws JSONException, java.text.ParseException {
        if (getVisitedString() != null){
            return Converters.fromArraytoHashMapStringByte(getVisited());
        }
        return new HashMap<>();
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
        return new ArrayList<>();
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
        User getByUsername(String username);

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        Long insertUser(User user);

        @Query("UPDATE user SET mNotVisitedString = :notVisited")
        void updateNotVisited(String notVisited);

        @Query("UPDATE user SET mVisitedString = :visited")
        void updateVisited(String visited);

        @Query("UPDATE user SET mAllString = :all")
        void updateAll(String all);

        @Update
        void updateUser(User user);

        @Delete
        void deleteUser(User user);
    }
}
