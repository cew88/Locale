package com.example.locale;

import android.app.Application;

import androidx.room.Room;

import com.example.locale.models.UserDatabase;

public class DatabaseApplication extends ParseApplication {
    UserDatabase userDatabase;

    @Override
    public void onCreate() {
        super.onCreate();

        userDatabase = Room.databaseBuilder(this, UserDatabase.class, UserDatabase.NAME).fallbackToDestructiveMigration().allowMainThreadQueries().build();
    }

    public UserDatabase getUserDatabase(){
        return userDatabase;
    }
}
