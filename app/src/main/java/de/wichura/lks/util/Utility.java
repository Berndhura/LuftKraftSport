package de.wichura.lks.util;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import de.wichura.lks.mainactivity.Constants;

import static de.wichura.lks.mainactivity.Constants.SHARED_PREFS_USER_INFO;

/**
 * Created by Bernd Wichura on 06.11.2016.
 * Luftkraftsport
 */

public class Utility {

    private Activity activity;

    public Utility(Activity activity) {
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
                (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }

    public static String getPriceString(Float price) {
        return price.toString().split("\\.")[0] + " €";
    }

    public static String getPriceWithoutEuro(Float price) {
        return price.toString().split("\\.")[0];
    }

    //todo activity ist nicht gesetzt, funktioniert das hier, wo wird das genutzt
    public String getUserToken() {
        return activity.getSharedPreferences(SHARED_PREFS_USER_INFO, 0).getString(Constants.USER_TOKEN, "");
    }
}
