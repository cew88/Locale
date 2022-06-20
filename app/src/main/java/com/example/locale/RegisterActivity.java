/*
Register activity allows users to create a new account  or navigate to the login account activity if
they already have an account. Creating an a new account also accesses the user's current location and
stores it in the Parse database. Successful registration log the user in and allows users to access
the main activity.
 */

package com.example.locale;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

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

import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseUser;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class RegisterActivity extends AppCompatActivity {
    public static final String TAG = "RegisterActivity";
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Hide the action bar on the login screen
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

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

                // Check if the two password fields match
                if (password.equals(passwordConfirm)){
                    try {
                        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                        if (ActivityCompat.checkSelfPermission(RegisterActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                                ActivityCompat.checkSelfPermission(RegisterActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            return;
                        }

                        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        if (location != null){
                            mLatitude = location.getLatitude();
                            mLongitude = location.getLongitude();

                            // Get city name
                            Geocoder gcd = new Geocoder(getBaseContext(), Locale.getDefault());
                            List<Address> addresses;
                            try {
                                addresses = gcd.getFromLocation(mLatitude,
                                        mLongitude, 1);
                                if (addresses.size() > 0) {
                                    System.out.println(addresses.get(0).getLocality());
                                    city = addresses.get(0).getLocality();
                                    Log.d(TAG, city);
                                    Toast.makeText(RegisterActivity.this, city, Toast.LENGTH_SHORT).show();
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        createUser(firstName, lastName, username, email, password, location);
                        navigateToMainActivity();

                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
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

                // TO DO: NAVIGATE TO PAGE OF INTERESTS FOR USERS TO SELECT
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
    }

    private void createUser(String firstName, String lastName, String username, String email, String password, Location location) throws ParseException {
        ParseGeoPoint geoPoint = new ParseGeoPoint(location.getLatitude(), location.getLongitude());

        ParseUser newUser = new ParseUser();
        newUser.put("first_name", firstName);
        newUser.put("last_name", lastName);
        newUser.put("username", username);
        newUser.put("email", email);
        newUser.put("password", password);
        newUser.put("location", geoPoint);
        newUser.signUp();
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}