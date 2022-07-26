package com.example.locale.activities;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.locale.R;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.RequestPasswordResetCallback;

public class ForgotPasswordActivity extends AppCompatActivity {
    Button mBtnSubmit;
    TextView mEmail;
    TextView mResponse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        // Hide the action bar on the login screen
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        mEmail = findViewById(R.id.etEmailFP);
        mResponse = findViewById(R.id.tvResponse);

        mBtnSubmit = findViewById(R.id.btnForgotPassword);
        mBtnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userEmail = mEmail.getText().toString();
                if (!isEmailValid(userEmail)){
                    mResponse.setText("Please enter a valid email address.");
                    mResponse.setVisibility(View.VISIBLE);
                }
                else {
                    ParseUser.requestPasswordResetInBackground(userEmail, new RequestPasswordResetCallback() {
                        public void done(ParseException e) {
                            if (e == null) {
                                // An email was successfully sent with reset instructions.
                                mResponse.setText("An email was successfully sent with reset instructions!");
                                mResponse.setVisibility(View.VISIBLE);
                            } else {
                                // Something went wrong. Look at the ParseException to see what's up.
                                mResponse.setText("Something went wrong.");
                                mResponse.setVisibility(View.VISIBLE);
                            }
                        }
                    });
                }
            }
        });
    }

    public boolean isEmailValid(CharSequence email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

}