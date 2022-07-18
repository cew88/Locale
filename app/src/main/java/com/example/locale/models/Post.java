package com.example.locale.models;

import static com.example.locale.models.Constants.*;

import com.parse.ParseClassName;
import com.parse.ParseObject;

@ParseClassName("Post")
public class Post extends ParseObject {

    public String getObjectId(){
        return getString(KEY_OBJECT_ID);
    }

    public String getUsername() {
        return getString(KEY_USERNAME);
    }

    public void setUsername(String username) {
        put(KEY_USERNAME, username);
    }

    public String getFirstName() {
        return getString(KEY_FIRST_NAME);
    }

    public void setFirstName(String firstName){
        put(KEY_FIRST_NAME, firstName);
    }

    public String getLastName() {
        return getString(KEY_LAST_NAME);
    }

    public void setLastName(String lastName){
        put (KEY_LAST_NAME, lastName);
    }

    public String getPhoto(){
        return getString(KEY_PHOTO);
    }

    public void setPhoto(String photo){
        put(KEY_PHOTO, photo);
    }

    public String getReview(){
        return getString(KEY_REVIEW);
    }

    public void setReview(String review){
        put(KEY_REVIEW, review);
    }

    public String getPlaceId(){
        return getString(KEY_PLACE_ID);
    }

    public void setPlaceId(String placeId){
        put(KEY_PLACE_ID, placeId);
    }

    public String getPlaceName(){
        return getString(KEY_PLACE_NAME);
    }

    public void setPlaceName(String placeName){
        put(KEY_PLACE_NAME, placeName);
    }
}
