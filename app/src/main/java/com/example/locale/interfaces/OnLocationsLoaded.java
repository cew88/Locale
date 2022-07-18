package com.example.locale.interfaces;

import com.example.locale.models.User;
import com.parse.ParseUser;

public interface OnLocationsLoaded {
    public void updateNotVisited(String string);
    public void updateVisited(String string);
}