package com.techland360.beloved.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;


public class SaveState {
    Context context;
    SharedPreferences sharedPreferences, mSettingsPreferences;


    public SaveState(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences("preferance", Context.MODE_PRIVATE);
        mSettingsPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }


    public boolean isNotificationOn() {
        return mSettingsPreferences.getBoolean("perf_notification", true);
    }


    public boolean darkModeOn() {
        return mSettingsPreferences.getBoolean("pref_dark_mode", false);
    }


}
