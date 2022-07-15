package com.example.locale.applications;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;

import androidx.room.Room;

import com.example.locale.R;
import com.example.locale.models.Location;
import com.example.locale.models.UserDatabase;
import com.parse.Parse;
import com.parse.ParseObject;

public class LocaleApplication extends Application {
    UserDatabase userDatabase;

    @Override
    public void onCreate() {
        super.onCreate();

        ParseObject.registerSubclass(Location.class);

        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId(getString(R.string.back4app_app_id))
                .clientKey(getString(R.string.back4app_client_key))
                .server(getString(R.string.back4app_server_url))
                .build());

        userDatabase = Room.databaseBuilder(this, UserDatabase.class, UserDatabase.NAME).fallbackToDestructiveMigration().allowMainThreadQueries().build();

        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel channel = new NotificationChannel("notificationChannel", "Notification Channel", importance);
        channel.setDescription("Reminders");

        NotificationManager mNotificationManager = (NotificationManager) this.getSystemService(NOTIFICATION_SERVICE);
        mNotificationManager.createNotificationChannel(channel);
    }

    public UserDatabase getUserDatabase(){
        return userDatabase;
    }
}
