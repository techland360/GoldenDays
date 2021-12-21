package com.techland360.goldendays;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.techland360.goldendays.activity.ChangeQuestionPage;
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
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();

        String contactid = getIntent().getStringExtra("contactid");

        if (contactid != null) {
            Intent intent = new Intent(SplashScreen.this, FriendsProfile.class);
            intent.putExtra("unique_id", contactid);
            startActivity(intent);
            finish();
        } else {
            Handler handler = new Handler();
            handler.postDelayed(() -> {
                if (mAuth.getCurrentUser() != null) {

                    DatabaseReference qref = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid()).child("selfInfo").child("answer");
                    qref.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            Log.d("TAG", "onDataChange: "+snapshot);
                            if (snapshot.getValue()!=null) {
                                startActivity(new Intent(SplashScreen.this, Homepage.class));
                            } else {
                                startActivity(new Intent(SplashScreen.this, ChangeQuestionPage.class));
                            }
                            finish();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });


                } else {
                    startActivity(new Intent(SplashScreen.this, AuthActivity.class));
                    finish();
                }

            }, 600); //seconds to go to new activity
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