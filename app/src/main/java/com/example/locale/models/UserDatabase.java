package com.example.locale.models;

import androidx.room.RoomDatabase;

@androidx.room.Database(entities = {User.class}, version=4)
public abstract class UserDatabase extends RoomDatabase {
    // Declare data access objects as abstract
    public abstract User.UserDao userDao();

    // Set database name
    public static final String NAME = "UserData";
}
