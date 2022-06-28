/*
Main activity is accessible once users log in or create a new account. Main activity accesses the user's
location from the Parse database and queries the Places API to generate a list of local landmarks.
 */

package com.example.locale;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.parse.ParseObject;
import com.parse.ParseUser;

import org.json.JSONException;

public class MainActivity extends AppCompatActivity implements LandmarksAdapter.OnLocationVisitedListener {
    public static final String TAG = "MainActivity";
    final FragmentManager fragmentManager = getSupportFragmentManager();
    User user;
    Bundle bundle;

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
        bundle = new Bundle();
        try {
            user = new User(currentUser);
            bundle.putParcelable("User", user);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Set the default fragment as HomeFragment
        Fragment defaultFragment = new HomeFragment();
        defaultFragment.setArguments(bundle);
        fragmentManager.beginTransaction().replace(R.id.flContainer, defaultFragment).commit();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.action_home);
        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment fragment;
                switch (item.getItemId()) {
                    case R.id.action_home:
                        Log.d(TAG, String.valueOf(user.getNotVisitedLandmarks().size()));
                        fragment = new HomeFragment();
                        fragment.setArguments(bundle);
                        break;
                    case R.id.action_map:
                        fragment = new MapsFragment();
                        fragment.setArguments(bundle);
                        break;
                    case R.id.action_profile:
                        fragment = new ProfileFragment();
                        fragment.setArguments(bundle);
                        break;
                    default:
                        throw new IllegalStateException("Unexpected value: " + item.getItemId());
                }
                fragmentManager.beginTransaction().replace(R.id.flContainer, fragment).commit();
                return true;
            }
        });
    }

    @Override
    public void updateLandmarks() {
        Log.d(TAG, "updateLandmarks: Location marked as visited!");
        ParseUser currentUser = ParseUser.getCurrentUser();
        try {
            user = new User(currentUser);
            bundle.putParcelable("User", user);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}