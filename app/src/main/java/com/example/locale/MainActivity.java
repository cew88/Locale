/*
Main activity is accessible once users log in or create a new account. Main activity accesses the user's
location from the Parse database and queries the Places API to generate a list of local landmarks.
 */

package com.example.locale;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.locale.adapters.LandmarksAdapter;
import com.example.locale.fragments.HomeFragment;
import com.example.locale.fragments.MapsFragment;
import com.example.locale.fragments.ProfileFragment;
import com.example.locale.models.Location;
import com.example.locale.models.User;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.parse.ParseObject;
import com.parse.ParseUser;

import org.json.JSONException;

public class MainActivity extends AppCompatActivity implements LandmarksAdapter.OnLocationVisitedListener {
    public static final String TAG = "MainActivity";
    final FragmentManager mFragmentManager = getSupportFragmentManager();
    User mUser;
    Bundle mBundle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Hide the action bar on the login screen
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        ParseObject.registerSubclass(Location.class);

        // Call the Parse database once when the Main activity is opened and pass the data to the
        // Fragments via Bundle
        ParseUser currentUser = ParseUser.getCurrentUser();
        mBundle = new Bundle();
        try {
            mUser = new User(currentUser);
            mBundle.putParcelable("User", mUser);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Set the default fragment as HomeFragment
        Fragment defaultFragment = new HomeFragment();
        defaultFragment.setArguments(mBundle);
        mFragmentManager.beginTransaction().replace(R.id.flContainer, defaultFragment).commit();
        bottomNavigationView.setSelectedItemId(R.id.action_home);

        // Handle clicks on the bottom navigation bar
        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment fragment;
                switch (item.getItemId()) {
                    case R.id.action_home:
                        fragment = new HomeFragment();
                        fragment.setArguments(mBundle);
                        break;
                    case R.id.action_map:
                        fragment = new MapsFragment();
                        fragment.setArguments(mBundle);
                        break;
                    case R.id.action_profile:
                        fragment = new ProfileFragment();
                        fragment.setArguments(mBundle);
                        break;
                    default:
                        throw new IllegalStateException("Unexpected value: " + item.getItemId());
                }
                mFragmentManager.beginTransaction().replace(R.id.flContainer, fragment).commit();
                return true;
            }
        });
    }

    // Query Parse for updated user data in response to marking a location visited in the Landmark adapter
    @Override
    public void updateLandmarks() {
        ParseUser currentUser = ParseUser.getCurrentUser();
        try {
            mUser = new User(currentUser);
            mBundle.putParcelable("User", mUser);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}