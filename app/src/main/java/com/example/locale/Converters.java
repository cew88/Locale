package com.example.locale;

import android.util.Log;

import androidx.room.TypeConverter;

import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;
import com.example.locale.models.Location;
import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.parse.GetCallback;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class Converters {
    @TypeConverter
    public static ArrayList<String> fromStringtoStringArrayList(String value) {
        Type listType = new TypeToken<ArrayList<String>>() {}.getType();
        return new Gson().fromJson(value, listType);
    }

    @TypeConverter
    public static ArrayList<Location> fromStringtoLocationArrayList(String value) throws JSONException {
        ArrayList<Location> locationArrayList = new ArrayList<Location>();

        JSONArray jsonArray = new JSONArray(value);
        for (int i=0; i<jsonArray.length(); i++){
            JSONObject jsonObject = jsonArray.getJSONObject(i);

            Location location = new Location();
            location.setObjectId(jsonObject.getString("objectId"));
            location.setName(jsonObject.getString("place_name"));
            location.setPlaceId(jsonObject.getString("place_id"));
            JSONObject typesObjects = new JSONObject(jsonObject.getString("types"));
            JSONArray typesArray = typesObjects.getJSONArray("values");
            location.setTypes(typesArray);
            location.setCoordinates(new ParseGeoPoint(jsonObject.getDouble("latitude"), jsonObject.getDouble("longitude")));
            location.setVicinity(jsonObject.getString("vicinity"));
            locationArrayList.add(location);
        }
        return locationArrayList;
    }

    @TypeConverter
    public static HashMap<String, Date> fromStringtoHashMap(String value) throws JSONException {
        HashMap<String, Date> locationDateHashMap = new HashMap<String, Date>();

        JSONArray jsonArray = new JSONArray(value);
        for (int i=0; i<jsonArray.length(); i++){
            JSONObject jsonObject = jsonArray.getJSONObject(i);

//            Location location = new Location();
//            location.setObjectId(jsonObject.getString("objectId"));
//            location.setName(jsonObject.getString("place_name"));
//            location.setPlaceId(jsonObject.getString("place_id"));
//            JSONObject typesObjects = new JSONObject(jsonObject.getString("types"));
//            JSONArray typesArray = typesObjects.getJSONArray("values");
//            location.setTypes(typesArray);
//            location.setCoordinates(new ParseGeoPoint(jsonObject.getDouble("latitude"), jsonObject.getDouble("longitude")));
//            location.setVicinity(jsonObject.getString("vicinity"));


            SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy");
            try {
                // Convert the date from a String to a Date object
                Date dateVisited = dateFormat.parse(jsonObject.getString("date_visited"));
                locationDateHashMap.put(jsonObject.getString("place_name"), dateVisited);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        return locationDateHashMap;
    }


    @TypeConverter
    public static String fromLocationArrayList(ArrayList<Location> locationArrayList) throws JSONException {
        JSONArray jsonArray = new JSONArray();

        for (Location location : locationArrayList){
            Gson gson = new Gson();

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("objectId", location.getObjectId());
            jsonObject.put("place_name", location.getName());
            jsonObject.put("place_id", location.getPlaceId());
            jsonObject.put("types", gson.toJson(location.getTypes()));
            jsonObject.put("latitude", String.valueOf(location.getCoordinates().getLatitude()));
            jsonObject.put("longitude", String.valueOf(location.getCoordinates().getLongitude()));
            jsonObject.put("vicinity", location.getVicinity());

            jsonArray.put(jsonObject);
        }
        return String.valueOf(jsonArray);
    }

    @TypeConverter
    public static String fromLocationHashMap(HashMap<Location, Date> locationDateHashMap) throws JSONException {
        JSONArray jsonArray = new JSONArray();

        for (Location location : locationDateHashMap.keySet()){
            Gson gson = new Gson();

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("objectId", location.getObjectId());
            jsonObject.put("place_name", location.getName());
            jsonObject.put("place_id", location.getPlaceId());
            jsonObject.put("types", gson.toJson(location.getTypes()));
            jsonObject.put("latitude", String.valueOf(location.getCoordinates().getLatitude()));
            jsonObject.put("longitude", String.valueOf(location.getCoordinates().getLongitude()));
            jsonObject.put("vicinity", location.getVicinity());
            jsonObject.put("date_visited", locationDateHashMap.get(location));

            jsonArray.put(jsonObject);
        }

        return String.valueOf(jsonArray);
    }

    @TypeConverter
    public static String fromStringArrayList(ArrayList<String> list) {
        Gson gson = new Gson();
        String json = gson.toJson(list);
        return json;
    }
}