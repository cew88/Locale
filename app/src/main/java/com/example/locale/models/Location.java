package com.example.locale.models;


import static com.example.locale.models.Constants.*;
import com.parse.ParseClassName;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;

import org.json.JSONArray;


@ParseClassName("Location")
public class Location extends ParseObject {

    // Empty constructor needed by Parceler library
    public Location(){}

    public String getName(){
        return getString(KEY_PLACE_NAME);
    }

    public void setName(String name){
        put(KEY_PLACE_NAME, name);
    }

    public String getPlaceId(){
        return getString(KEY_PLACE_ID);
    }

    public void setPlaceId(String placeId){
        put(KEY_PLACE_ID, placeId);
    }

    public JSONArray getTypes(){
        return getJSONArray(KEY_TYPES);
    }

    public void setTypes(JSONArray types){
        put(KEY_TYPES, types);
    }

    public String getVicinity(){
        return getString(KEY_VICINITY);
    }

    public void setVicinity(String vicinity){
        put(KEY_VICINITY, vicinity);
    }

    public ParseGeoPoint getCoordinates(){
        return getParseGeoPoint(KEY_COORDINATES);
    }

    public void setCoordinates(ParseGeoPoint geoPoint){
        put(KEY_COORDINATES, geoPoint);
    }
}

