/*
Register activity allows users to create a new account or navigate to the login account activity if
they already have an account. Creating an a new account also accesses the user's current location and
stores it in the Parse database. Successful registration log the user in and allows users to access
the main activity.
 */

package com.example.locale.activities;

import static com.example.locale.models.Constants.*;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.locale.R;
import com.example.locale.models.User;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class RegisterActivity extends AppCompatActivity {
    private TextView mFirstName;
    private TextView mLastName;
    private TextView mUsername;
    private TextView mEmail;
    private TextView mPassword;
    private TextView mPasswordConfirm;
    private Button mCreateAcctBtn;
    private TextView mLogin;

    private double mLatitude;
    private double mLongitude;
    private String city;

    private FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Hide the action bar on the login screen
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        // Find layout IDs
        mFirstName = findViewById(R.id.etFirstNameReg);
        mLastName = findViewById(R.id.etLastNameReg);
        mUsername = findViewById(R.id.etUsernameReg);
        mEmail = findViewById(R.id.etEmailReg);
        mPassword = findViewById(R.id.etPasswordReg);
        mPasswordConfirm = findViewById(R.id.etPasswordConfirmReg);

        mCreateAcctBtn = findViewById(R.id.btnCreateAcct);
        mCreateAcctBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String firstName = mFirstName.getText().toString();
                String lastName = mLastName.getText().toString();
                String username = mUsername.getText().toString();
                String email = mEmail.getText().toString();
                String password = mPassword.getText().toString();
                String passwordConfirm = mPasswordConfirm.getText().toString();

                // Check to make sure no fields are empty
                if (firstName.equals("") || lastName.equals("") || username.equals("") ||
                        email.equals("") || password.equals("") || passwordConfirm.equals("")){
                    Toast.makeText(RegisterActivity.this, "Please fill out all fields!", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Check if the two password fields match
                if (password.equals(passwordConfirm)){
                    LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                    if (ActivityCompat.checkSelfPermission(RegisterActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                            ActivityCompat.checkSelfPermission(RegisterActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(RegisterActivity.this, "Permission not granted", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Get the user's location and create an account if the location is not null
                    fusedLocationClient = LocationServices.getFusedLocationProviderClient(RegisterActivity.this);
                    fusedLocationClient.getLastLocation().addOnSuccessListener( new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                // Logic to handle location object
                                mLatitude = location.getLatitude();
                                mLongitude = location.getLongitude();

                                // Get city name
                                Geocoder gcd = new Geocoder(getBaseContext(), Locale.getDefault());
                                List<Address> addresses;
                                try {
                                    addresses = gcd.getFromLocation(mLatitude, mLongitude, 1);
                                    if (addresses.size() > 0) {
                                        System.out.println(addresses.get(0).getLocality());
                                        city = addresses.get(0).getLocality();
                                        Log.d(REGISTER_ACTIVITY_TAG, city);
                                        Toast.makeText(RegisterActivity.this, city, Toast.LENGTH_SHORT).show();
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                try {
                                    createUser(firstName, lastName, username, email, password, location);
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            }
                            else {
                                try {
                                    createUser(firstName, lastName, username, email, password);
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });
                }
                else {
                    Toast.makeText(RegisterActivity.this, "Passwords don't match!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        mLogin = findViewById(R.id.tvHasAccount);
        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
    }

    // Create user account without location
    private void createUser(String firstName, String lastName, String username, String email, String password) throws ParseException {
        ParseUser newUser = new ParseUser();
        newUser.put(KEY_FIRST_NAME, firstName);
        newUser.put(KEY_LAST_NAME, lastName);
        newUser.put(KEY_USERNAME, username);
        newUser.put(KEY_EMAIL, email);
        newUser.put(KEY_PASSWORD, password);
        newUser.signUpInBackground(new SignUpCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null){
                    Log.d(REGISTER_ACTIVITY_TAG, "New Parse user created (with no location)!");
                    navigateToInterestsActivity();
                } else {
                    switch(e.getCode()){
                        case ParseException.USERNAME_TAKEN:
                            Toast.makeText(RegisterActivity.this, "Username Taken!", Toast.LENGTH_SHORT).show();
                            break;
                        case ParseException.EMAIL_TAKEN:
                            Toast.makeText(RegisterActivity.this, "Email Taken!", Toast.LENGTH_SHORT).show();
                        default:
                            Toast.makeText(RegisterActivity.this, "Could not make account", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    // Create user account with location
    private void createUser(String firstName, String lastName, String username, String email, String password, Location location) throws ParseException {
        ParseGeoPoint geoPoint = new ParseGeoPoint(location.getLatitude(), location.getLongitude());

        ParseUser newUser = new ParseUser();
        newUser.put(KEY_FIRST_NAME, firstName);
        newUser.put(KEY_LAST_NAME, lastName);
        newUser.put(KEY_USERNAME, username);
        newUser.put(KEY_EMAIL, email);
        newUser.put(KEY_PASSWORD, password);
        newUser.put(KEY_LOCATION, geoPoint);
        newUser.signUpInBackground(new SignUpCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null){
                    Log.d(REGISTER_ACTIVITY_TAG, "New Parse user created (with location)!");
                    navigateToInterestsActivity();
                } else {
                    switch(e.getCode()){
                        case ParseException.USERNAME_TAKEN:
                            Toast.makeText(RegisterActivity.this, "Username Taken!", Toast.LENGTH_SHORT).show();
                            break;
                        case ParseException.EMAIL_TAKEN:
                            Toast.makeText(RegisterActivity.this, "Email Taken!", Toast.LENGTH_SHORT).show();
                        default:
                            Toast.makeText(RegisterActivity.this, "Could not make account", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    // Start new intent to navigate to the interests activity
    private void navigateToInterestsActivity(){
        Intent intent = new Intent(RegisterActivity.this, InterestsActivity.class);
        startActivity(intent);
        finish();
    }
}