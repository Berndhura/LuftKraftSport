package wichura.de.camperapp.util;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import wichura.de.camperapp.mainactivity.Constants;

import static wichura.de.camperapp.mainactivity.Constants.SHARED_PREFS_USER_INFO;

/**
 * Created by ich on 06.11.2016.
 * Camper App
 */

public class Utility {

    private Activity activity;

    public Utility (Activity activity) {
        this.activity = activity;
    }

    /**
     * Returns true if the network is available or about to become available.
     *
     * @param c Context used to get the ConnectivityManager
     * @return true if the network is available
     */
    static public boolean isNetworkAvailable(Context c) {
        ConnectivityManager cm =
                (ConnectivityManager)c.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }

    public String getUserToken() {
        return activity.getSharedPreferences(SHARED_PREFS_USER_INFO, 0).getString(Constants.USER_TOKEN, "");
    }
}
