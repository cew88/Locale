package com.example.locale.interfaces;

import org.json.JSONException;
import java.io.UnsupportedEncodingException;

public interface AddPhoto {
    public void addPhoto(String object_id, String place_id, String place_name, byte[] bytes) throws JSONException, UnsupportedEncodingException;
}