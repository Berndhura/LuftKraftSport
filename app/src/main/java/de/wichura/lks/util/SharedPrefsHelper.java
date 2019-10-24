package de.wichura.lks.util;

import android.app.Activity;
import android.content.SharedPreferences;

import de.wichura.lks.mainactivity.Constants;

import static de.wichura.lks.mainactivity.Constants.SHARED_PREFS_LOCATION;
import static de.wichura.lks.mainactivity.Constants.SHARED_PREFS_USER_INFO;

public class SharedPrefsHelper {

    private Activity activity;

    public SharedPrefsHelper(Activity activity) {
        this.activity = activity;
    }

    public String getUserToken() {
        return activity.getSharedPreferences(SHARED_PREFS_USER_INFO, 0).getString(Constants.USER_TOKEN, "");
    }

    public String getLastLocationName() {
        return activity.getSharedPreferences(SHARED_PREFS_LOCATION, 0).getString(Constants.LAST_LOCATION_NAME, "");
    }

    public Float getLastLat() {
        return activity.getSharedPreferences(SHARED_PREFS_LOCATION, 0).getFloat(Constants.LAST_LAT, 0.0f);
    }

    public Float getLastLng() {
        return activity.getSharedPreferences(SHARED_PREFS_LOCATION, 0).getFloat(Constants.LAST_LNG, 0.0f);
    }

    public void setLastLocationName(String locationName) {
        SharedPreferences settings = activity.getSharedPreferences(SHARED_PREFS_LOCATION, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(Constants.LAST_LOCATION_NAME, locationName);
        editor.apply();
    }

    public void setLastLocationCoordinates(Float lat, Float lng) {
        SharedPreferences settings = activity.getSharedPreferences(SHARED_PREFS_LOCATION, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putFloat(Constants.LAST_LAT, lat);
        editor.putFloat(Constants.LAST_LNG, lng);
        editor.apply();
    }

}
