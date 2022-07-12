package com.example.locale.models;

import android.util.Log;

import androidx.annotation.LongDef;
import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.parse.ParseGeoPoint;

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
    public static HashMap<String, byte[]> fromArraytoHashMapStringByte(ArrayList<JSONObject> jsonObjects) throws JSONException {
        HashMap<String, byte[]> locationByteHashMap = new HashMap<String, byte[]>();
        for (int i=0; i<jsonObjects.size(); i++) {
            JSONObject jsonObject = jsonObjects.get(i);
            String byteArrayString = jsonObject.getString("photo");
            byte[] byteArray = byteArrayString.getBytes();

            locationByteHashMap.put(jsonObject.getString("place_name"), byteArray);
        }
            return locationByteHashMap;
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
    public static String fromStringArrayList(ArrayList<String> list) {
        Gson gson = new Gson();
        String json = gson.toJson(list);
        return json;
    }
}