package com.example.locale.models;

import static com.example.locale.models.Constants.*;

import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.parse.ParseGeoPoint;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
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
            location.setObjectId(jsonObject.getString(KEY_OBJECT_ID));
            location.setName(jsonObject.getString(KEY_PLACE_NAME));
            location.setPlaceId(jsonObject.getString(KEY_PLACE_ID));
            JSONObject typesObjects = new JSONObject(jsonObject.getString(KEY_TYPES));
            JSONArray typesArray = typesObjects.getJSONArray(KEY_VALUES);
            location.setTypes(typesArray);
            location.setCoordinates(new ParseGeoPoint(jsonObject.getDouble(KEY_LATITUDE), jsonObject.getDouble(KEY_LONGITUDE)));
            location.setVicinity(jsonObject.getString(KEY_VICINITY));

            locationArrayList.add(location);
        }
        return locationArrayList;
    }


    @TypeConverter
    public static HashMap<String, byte[]> fromArraytoHashMapStringByte(ArrayList<JSONObject> jsonObjects) throws JSONException {
        HashMap<String, byte[]> locationByteHashMap = new HashMap<String, byte[]>();
        for (int i=0; i<jsonObjects.size(); i++) {
            JSONObject jsonObject = jsonObjects.get(i);
            String byteArrayString = jsonObject.getString(KEY_PHOTO);
            byte[] byteArray = byteArrayString.getBytes();

            locationByteHashMap.put(jsonObject.getString(KEY_PLACE_NAME), byteArray);
        }
            return locationByteHashMap;
    }


    @TypeConverter
    public static String fromLocationArrayList(ArrayList<Location> locationArrayList) throws JSONException {
        JSONArray jsonArray = new JSONArray();

        for (Location location : locationArrayList){
            Gson gson = new Gson();

            JSONObject jsonObject = new JSONObject();
            jsonObject.put(KEY_OBJECT_ID, location.getObjectId());
            jsonObject.put(KEY_PLACE_NAME, location.getName());
            jsonObject.put(KEY_PLACE_ID, location.getPlaceId());
            jsonObject.put(KEY_TYPES, gson.toJson(location.getTypes()));
            jsonObject.put(KEY_LATITUDE, String.valueOf(location.getCoordinates().getLatitude()));
            jsonObject.put(KEY_LONGITUDE, String.valueOf(location.getCoordinates().getLongitude()));
            jsonObject.put(KEY_VICINITY, location.getVicinity());

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