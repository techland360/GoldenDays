package com.techland360.goldendays.helper;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

public class Bondhu extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}
