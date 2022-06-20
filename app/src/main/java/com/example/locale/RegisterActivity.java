package com.example.locale;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseUser;

public class RegisterActivity extends AppCompatActivity {
    private TextView mFirstName;
    private TextView mLastName;
    private TextView mUsername;
    private TextView mPassword;
    private TextView mPasswordConfirm;
    private Button mCreateAcctBtn;
    private TextView mLogin;

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
        mPassword = findViewById(R.id.etPasswordReg);
        mPasswordConfirm = findViewById(R.id.etPasswordConfirmReg);

        mCreateAcctBtn = findViewById(R.id.btnCreateAcct);
        mCreateAcctBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String firstName = mFirstName.getText().toString();
                String lastName = mLastName.getText().toString();
                String username = mUsername.getText().toString();
                String password = mPassword.getText().toString();
                String passwordConfirm = mPasswordConfirm.getText().toString();

                // Check if the two password fields match
                if (password.equals(passwordConfirm)){
                    try {
                        createUser(firstName, lastName, username, password);
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

    private void createUser(String firstName, String lastName, String username, String password) throws ParseException {
        ParseUser newUser = new ParseUser();
        newUser.put("first_name", firstName);
        newUser.put("last_name", lastName);
        newUser.put("username", username);
        newUser.put("password", password);
        newUser.signUp();
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}