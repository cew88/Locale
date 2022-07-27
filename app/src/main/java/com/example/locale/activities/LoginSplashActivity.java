package com.example.locale.activities;

import static com.example.locale.models.Constants.KEY_NOT_VISITED_LANDMARKS;
import static com.example.locale.models.Constants.KEY_VISITED_LANDMARKS;
import static com.example.locale.models.Constants.LOGIN_ACTIVITY_TAG;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.locale.R;
import com.example.locale.applications.LocaleApplication;
import com.example.locale.interfaces.OnLocationsLoaded;
import com.example.locale.models.User;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

import org.json.JSONException;
import org.parceler.Parcels;

public class LoginSplashActivity extends AppCompatActivity {
    public static boolean connectedToNetwork;
    private ParseUser mCurrentUser;
    private User mUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_splash);

        // Hide the action bar on the login screen
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        final User.UserDao userDao = ((LocaleApplication)getApplicationContext()).getUserDatabase().userDao();
        OnLocationsLoaded onLocationsLoaded = new OnLocationsLoaded() {
            @Override
            public void updateNotVisited(String notVisitedString) {
                Log.d(LOGIN_ACTIVITY_TAG, "Not Visited Loaded");
                userDao.updateNotVisited(notVisitedString);
                navigateToMainActivity();
            }

            @Override
            public void updateVisited(String visitedString) {
                Log.d(LOGIN_ACTIVITY_TAG, "Visited Loaded");
                userDao.updateVisited(visitedString);
            }
        };

        // Get the currently used logged in user
        mCurrentUser = ParseUser.getCurrentUser();

        // Check if the app is connected to the internet or not
        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
            //The app is connected
            connectedToNetwork = true;

            // If the user is already logged in, skip the log in screen and navigate to the main activity
            if (mCurrentUser != null) {
                // Access user data from the Room Database
                mUser = userDao.getByUsername(mCurrentUser.getUsername());

                // Create a new user with data from Parse
                try {
                    User user = new User(ParseUser.getCurrentUser(), onLocationsLoaded);
                    // If the user is not stored locally but somehow logged in
                    // Insert the user into the local database
                    if (mUser == null) {
                        userDao.insertUser(user);
                    }
                    // If the user is stored locally, update what is in the local database with what is
                    // stored in Parse
                    else {
                        mUser.setVisitedString(String.valueOf(mCurrentUser.getJSONArray(KEY_VISITED_LANDMARKS)));
                        mUser.setNotVisitedString(String.valueOf(mCurrentUser.getJSONArray(KEY_NOT_VISITED_LANDMARKS)));
                        userDao.updateUser(mUser);
                    }
                } catch (JSONException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
            else {
                navigateToLoginActivity();
            }
        }
    }

    private void navigateToLoginActivity() {
        Intent intent = new Intent(LoginSplashActivity.this, LoginActivity.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    private void navigateToMainActivity(){
        Intent intent = new Intent(LoginSplashActivity.this, MainActivity.class);
        intent.putExtra("User", Parcels.wrap(mUser));
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }
}