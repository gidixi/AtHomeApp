package com.gds.athomeapp.Helpers;

import android.content.Context;
import android.content.SharedPreferences;

public class mySharedPreferences {

    private static final String SERVICE = "ServiceRunning";
    private static final String PREFERENCES_FILE = "ServicePrefs";

    private SharedPreferences sharedPreferences;

    public mySharedPreferences(Context context) {
        sharedPreferences = context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
    }

    public void putServiceRunning(boolean stat) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(SERVICE, stat);
        editor.apply();
    }

    public Boolean getServiceRunning() {
        return sharedPreferences.getBoolean(SERVICE,false);
    }
}
