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

    public Double getLastLat() {
        return Double.longBitsToDouble(activity.getSharedPreferences(SHARED_PREFS_LOCATION, 0).getLong(Constants.LAT, 0));
    }

    public Double getLastLng() {
        return Double.longBitsToDouble(activity.getSharedPreferences(SHARED_PREFS_LOCATION, 0).getLong(Constants.LNG, 0));
    }

    public void setLastLocationName(String locationName) {
        SharedPreferences settings = activity.getSharedPreferences(SHARED_PREFS_LOCATION, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(Constants.LAST_LOCATION_NAME, locationName);
        editor.apply();
    }

    public void setLastLocationCoordinates(Double lat, Double lng) {
        SharedPreferences settings = activity.getSharedPreferences(SHARED_PREFS_LOCATION, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putLong(Constants.LAT, Double.doubleToRawLongBits(lat));
        editor.putLong(Constants.LNG, Double.doubleToRawLongBits(lng));
        editor.apply();
    }

}
