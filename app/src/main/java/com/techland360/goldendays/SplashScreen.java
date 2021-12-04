package com.techland360.goldendays;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.firebase.auth.FirebaseAuth;
import com.techland360.goldendays.activity.FriendsProfile;
import com.techland360.goldendays.activity.Homepage;
import com.techland360.goldendays.auth.AuthActivity;
import com.techland360.goldendays.helper.SaveState;

public class SplashScreen extends AppCompatActivity {
    FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        mAuth = FirebaseAuth.getInstance();



        String contactid = getIntent().getStringExtra("contactid");

        if (contactid !=null){
            Intent intent = new Intent(SplashScreen.this, FriendsProfile.class);
            intent.putExtra("unique_id", contactid);
            startActivity(intent);
            finish();
        }else {
            Handler handler = new Handler();
            handler.postDelayed(() -> {
                if (mAuth.getCurrentUser() != null) {
                    startActivity(new Intent(SplashScreen.this, Homepage.class));
                    finish();
                }else {
                    startActivity(new Intent(SplashScreen.this, AuthActivity.class));
                    finish();
                }
            },600); //seconds to go to new activity
        }


    }

    @Override
    protected void onStart() {
        super.onStart();
        SaveState saveState = new SaveState(SplashScreen.this);
        if (saveState.darkModeOn()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        }

    }
}