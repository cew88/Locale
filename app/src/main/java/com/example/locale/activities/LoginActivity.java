/*
Login activity allows users to log in to their account or navigate to a create account activity if
they do not already have an account. Successful log in allows users to access the main activity.
 */

package com.example.locale.activities;

import static com.example.locale.models.Constants.*;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.example.locale.R;
import com.example.locale.applications.LocaleApplication;
import com.example.locale.models.User;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

public class LoginActivity extends AppCompatActivity {
    private TextView mUsername;
    private TextView mPassword;
    private Button mLoginBtn;
    private TextView mForgetPassword;
    private TextView mCreateAccount;
    private ParseUser mCurrentUser = ParseUser.getCurrentUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Hide the action bar on the login screen
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        // If the user is already logged in, skip the log in screen and navigate to the main activity
        if (mCurrentUser != null){
            // Access user data from the Room Database
            // final User.UserDao userDao = ((DatabaseApplication)getApplicationContext()).getUserDatabase().userDao();
            // User mUser = userDao.getByUsername(mCurrentUser.getUsername());

            navigateToMainActivity();
        }

        // Launch pop up checking if the user has been logged in
        ActivityResultLauncher<String[]> locationPermissionRequest =
            registerForActivityResult(new ActivityResultContracts
                            .RequestMultiplePermissions(), result -> {
                        Boolean fineLocationGranted = result.getOrDefault(
                                Manifest.permission.ACCESS_FINE_LOCATION, false);
                        Boolean coarseLocationGranted = result.getOrDefault(
                                Manifest.permission.ACCESS_COARSE_LOCATION,false);
                        if (fineLocationGranted != null && fineLocationGranted) {
                            // Precise location access granted.
                        } else if (coarseLocationGranted != null && coarseLocationGranted) {
                            // Only approximate location access granted.
                        } else {
                            // No location access granted.
                        }
                    }
            );

        // Before you perform the actual permission request, check whether your app
        // already has the permissions, and whether your app needs to show a permission
        // rationale dialog. For more details, see Request permissions.
        locationPermissionRequest.launch(new String[] {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        });

        mUsername = findViewById(R.id.etUsernameLogin);
        mPassword = findViewById(R.id.etPasswordLogin);

        // Handle what happens when the text prompting users to create an account is clicked
        mCreateAccount = findViewById(R.id.tvNoAccount);
        mCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        // Handle what happens when the login button is clicked
        mLoginBtn = findViewById(R.id.btnLogin);
        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = mUsername.getText().toString();
                String password = mPassword.getText().toString();
                Log.d(LOGIN_ACTIVITY_TAG, "Attempting to log in user: " + username);

                ParseUser.logInInBackground(username, password, new LogInCallback() {
                    @Override
                    public void done(ParseUser user, ParseException e) {
                        if (e != null) {
                            Log.e(LOGIN_ACTIVITY_TAG, "Error with logging in user", e);
                            Toast.makeText(LoginActivity.this, "Invalid username/password", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // Access user data from the Room Database
                        final User.UserDao userDao = ((LocaleApplication)getApplicationContext()).getUserDatabase().userDao();
                        User mUser = userDao.getByUsername(user.getUsername());

                        // If the user is null but exists in Parse, create a new user entry in the local database
                        if (mUser == null) {
                            Toast.makeText(LoginActivity.this, "User not found locally", Toast.LENGTH_SHORT).show();
//                            try {
//                                OnLocationsLoaded onLocationsLoaded = new OnLocationsLoaded() {
//                                    @Override
//                                    public void updateNotVisited(String notVisitedString) {
//                                        Log.d(LOGIN_ACTIVITY_TAG, "Not Visited Loaded");
//                                        userDao.updateNotVisited(notVisitedString);
//                                    }
//
//                                    @Override
//                                    public void updateVisited(String visitedString) {
//                                        Log.d(LOGIN_ACTIVITY_TAG, "Visited Loaded");
//                                        userDao.updateVisited(visitedString);
//                                    }
//
//                                    @Override
//                                    public void updateAll(String allString) {
//                                        Log.d(LOGIN_ACTIVITY_TAG, "All Loaded");
//                                        userDao.updateAll(allString);
//                                    }
//                                };
//                                User newUser = new User(ParseUser.getCurrentUser(), onLocationsLoaded);
//                                userDao.insertUser(newUser);
//                            } catch (JSONException | InterruptedException exception) {
//                                exception.printStackTrace();
//                            }
                        }
                         navigateToMainActivity();
                        // Toast.makeText(LoginActivity.this, "Login Successful!", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }
}