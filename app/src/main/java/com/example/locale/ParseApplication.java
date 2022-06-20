package com.example.locale;

import android.app.Application;

import com.parse.Parse;

public class ParseApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId("DMDixujYmbRGXM5IXZQFpxqHlWeSVM7qTnZWnhhc")
                .clientKey("slafzxfuRD5bj0T3pJ6NQfZSp1NHaqpPFEJJ3Z1m")
                .server("https://parseapi.back4app.com")
                .build()
        );
    }
}
