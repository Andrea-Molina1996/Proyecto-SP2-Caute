package com.molina.andrea.myapplication;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by amoli on 9/6/2017.
 */

class PrefManager {
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    Context _context;

    // shared pref mode
    int PRIVATE_MODE = 0;

    // Shared preferences file name
    private static final String PREF_NAME = "androidhive-welcome";
    private static final String IS_FIRST_TIME_LAUNCH = "IsFirstTimeLaunch";
    private static final String IS_FIRST_TIME_LOG_IN = "IsFirstTimeLogIn";

    public PrefManager(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    //isFirstTimeLaunch() returns true if the app is launched for the first time
    public void setFirstTimeLaunch(boolean isFirstTime) {
        editor.putBoolean(IS_FIRST_TIME_LAUNCH, isFirstTime);
        editor.commit();
    }

    public boolean isFirstTimeLaunch() {
        return pref.getBoolean(IS_FIRST_TIME_LAUNCH, true);
    }

    //isFirstTimeLogIn() returns true if it's the users first time logging in
    public void setFirstTimeLogIn(boolean IsFirstTimeLogIn){
        editor.putBoolean(IS_FIRST_TIME_LOG_IN, IsFirstTimeLogIn);
        editor.commit();
    }

    public boolean isFirstTimeLogIn() {
        return pref.getBoolean(IS_FIRST_TIME_LOG_IN, true);
    }

}
