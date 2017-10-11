package de.wichura.lks.util;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Base64;
import android.util.Log;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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
        if (price==null) {
            return "0";
        }
        else {
            return price.toString().split("\\.")[0] + " €";
        }
    }

    public static String getPriceWithoutEuro(Float price) {
        return price.toString().split("\\.")[0];
    }

    public String getUserToken() {
        return activity.getSharedPreferences(SHARED_PREFS_USER_INFO, 0).getString(Constants.USER_TOKEN, "");
    }

    public void setUserPreferences(String name, String userId, String userToken) {
        SharedPreferences settings = activity.getSharedPreferences(SHARED_PREFS_USER_INFO, 0);
        SharedPreferences.Editor editor = settings.edit();
        if (name != null) editor.putString(Constants.USER_NAME, name);
        if (userId != null) editor.putString(Constants.USER_ID, userId);
        if (userToken != null) editor.putString(Constants.USER_TOKEN, userToken);
        editor.apply();
    }

    public static final String hashStringMd5(final String s) {

        final String MD5 = "MD5";
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest
                    .getInstance(MD5);
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                String h = Integer.toHexString(0xFF & aMessageDigest);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    public String computeSHAHash(String password) {

        String sha1Hash="";

        MessageDigest mdSha1 = null;
        try {
            mdSha1 = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e1) {
            Log.e("CONAN", "Error initializing SHA1 message digest");
        }
        try {
            mdSha1.update(password.getBytes("ASCII"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        byte[] data = mdSha1.digest();
        try {
            sha1Hash = convertToHex(data);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return sha1Hash;
    }

    private static String convertToHex(byte[] data) throws java.io.IOException {

        int NO_OPTIONS = 0;

        StringBuffer sb = new StringBuffer();
        String hex;
        hex = Base64.encodeToString(data, 0, data.length, NO_OPTIONS);
        sb.append(hex);
        return sb.toString();
    }
}
