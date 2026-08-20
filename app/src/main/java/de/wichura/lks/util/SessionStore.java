package de.wichura.lks.util;

import android.content.Context;
import android.content.SharedPreferences;

import de.wichura.lks.mainactivity.Constants;

import static de.wichura.lks.mainactivity.Constants.SHARED_PREFS_USER_INFO;

/**
 * Single source of truth for the current user's session data (id, name, token,
 * profile picture URL, auth type). Wraps the {@code SHARED_PREFS_USER_INFO}
 * preferences file so callers stop dealing with string keys directly.
 */
public class SessionStore {

    private final SharedPreferences prefs;

    public SessionStore(Context context) {
        this.prefs = context.getApplicationContext()
                .getSharedPreferences(SHARED_PREFS_USER_INFO, 0);
    }

    public String getUserId() {
        return prefs.getString(Constants.USER_ID, "");
    }

    public String getUserName() {
        return prefs.getString(Constants.USER_NAME, "");
    }

    public String getUserToken() {
        return prefs.getString(Constants.USER_TOKEN, "");
    }

    public String getUserProfilePicture() {
        return prefs.getString(Constants.USER_PICTURE, "");
    }

    public String getUserType() {
        return prefs.getString(Constants.USER_TYPE, "");
    }

    public boolean isLoggedIn() {
        return !getUserId().isEmpty();
    }

    public void setUserType(String userType) {
        prefs.edit().putString(Constants.USER_TYPE, userType).apply();
    }

    /** Writes any non-null field to preferences. Nulls are ignored. */
    public void updateUser(String name, String userId, String userToken) {
        SharedPreferences.Editor editor = prefs.edit();
        if (name != null) editor.putString(Constants.USER_NAME, name);
        if (userId != null) editor.putString(Constants.USER_ID, userId);
        if (userToken != null) editor.putString(Constants.USER_TOKEN, userToken);
        editor.apply();
    }
}
