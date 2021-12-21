package com.techland360.goldendays.activity;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.ktx.Firebase;
import com.techland360.goldendays.R;
import com.techland360.goldendays.auth.AuthActivity;

public class Settings extends AppCompatActivity {

    TextView cq;
    FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        //Toolbar back button
        Toolbar toolbar;
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Settings");
        toolbar.setNavigationOnClickListener(v -> onBackPressed());




        mAuth = FirebaseAuth.getInstance();


        DatabaseReference qref = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid()).child("selfInfo").child("answer");

        //Change Question Opener
        cq = findViewById(R.id.changeQuestion);
        cq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                qref.setValue(null);
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(Settings.this, AuthActivity.class));
                finish();
            }
        });
        //Change Question logout






    }
}