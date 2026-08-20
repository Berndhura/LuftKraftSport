package de.wichura.lks.http;

/**
 * Created by Bernd Wichura on 13.11.2015.
 * Luftkraftsport
 */
public class Urls {

    private Urls() {
    }

    public static final String UPLOAD_ERROR_URL = "http://178.254.54.25:9876/api/V3/errors/android/";
    // Local dev backend. Emulator reaches host loopback via 10.0.2.2.
    // For a physical device on the same LAN, change this to your Mac's LAN IP.
    public static final String MAIN_SERVER_URL_V3 = "http://10.0.2.2:3000/api/V3/";
    public static final String GOOGLE_MAPS_URL = "https://maps.googleapis.com/maps/api/geocode/";
}

