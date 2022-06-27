/*
Class represents the Parse User. Saving the Parse User's information in a Java Object reduces the
number of queries made to the Parse database.
*/

package com.example.locale;

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
import java.util.ArrayList;
import java.util.List;

@Parcel
public class User implements Parcelable{
    private String firstName;
    private String lastName;
    private String userName;
    private String email;
    private double latitude;
    private double longitude;
    private ArrayList<String> interests = new ArrayList<String>();
    private ArrayList<Location> notVisited = new ArrayList<Location>();
    private ArrayList<Location> all = new ArrayList<Location>();
    private int userPace;

    public User(){}

    public User(ParseUser user) throws JSONException {
        this.firstName = user.getString("first_name");
        this.lastName = user.getString("last_name");
        this.userName = user.getString("username");
        this.email = user.getEmail();

        ParseGeoPoint location = user.getParseGeoPoint("location");
        this.latitude = location.getLatitude();
        this.longitude = location.getLongitude();

        JSONArray userInterests = user.getJSONArray("interests");
        for (int i=0; i<userInterests.length(); i++){
            this.interests.add((String) userInterests.get(i));
        }

        JSONArray notVisitedLandmarks = user.getJSONArray("not_visited_landmarks");
        for (int i=0; i<notVisitedLandmarks.length(); i++){
            Location l = new Location();
            JSONObject jsonObject = (JSONObject) notVisitedLandmarks.get(i);
            String objectId = jsonObject.getString("objectId");

            ParseQuery<ParseObject> query = ParseQuery.getQuery("Location");
            query.whereEqualTo("objectId", objectId);
            query.getFirstInBackground(new GetCallback<ParseObject>() {
                public void done(ParseObject object, ParseException e) {
                    if (e == null) {
                        notVisited.add((Location) object);
                    }
                }
            });
        }

        JSONArray allLandmarks = user.getJSONArray("not_visited_landmarks");
        for (int j=0; j<allLandmarks.length(); j++){
            Location l = new Location();
            JSONObject jsonObject = (JSONObject) notVisitedLandmarks.get(j);
            String objectId = jsonObject.getString("objectId");

            ParseQuery<ParseObject> query = ParseQuery.getQuery("Location");
            query.whereEqualTo("objectId", objectId);
            query.getFirstInBackground(new GetCallback<ParseObject>() {
                public void done(ParseObject object, ParseException e) {
                    if (e == null) {
                        all.add((Location) object);
                    }
                }
            });
        }

        this.userPace = user.getInt("pace");
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(android.os.Parcel dest, int flags) {
        dest.writeString(firstName);
        dest.writeString(lastName);
        dest.writeString(userName);
        dest.writeString(email);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeStringList(interests);
        dest.writeTypedList(notVisited);
        dest.writeTypedList(all);
        dest.writeInt(userPace);
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName(){
        return lastName;
    }

    public String getUserName(){
        return userName;
    }

    public String getEmail(){
        return email;
    }

    public double getLatitude(){
        return latitude;
    }

    public double getLongitude(){
        return longitude;
    }

    public ArrayList<String> getInterests() {
        return interests;
    }

    public ArrayList<Location> getNotVisitedLandmarks(){
        return notVisited;
    }

    public ArrayList<Location> getAllLandmarks() {
        return all;
    }

    public int getUserPace() {
        return userPace;
    }
}
