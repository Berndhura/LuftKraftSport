package de.wichura.lks.mainactivity;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.util.Log;

import de.wichura.lks.util.GoogleApiHelper;

/**
 * Created by Bernd Wichura on 25.02.2017.
 * Luftkraftsport
 */

public class MainApp extends Application {

    private GoogleApiHelper googleApiHelper;
    private static MainApp mInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("CONAN", "onCreate in MainApp");

        mInstance = this;
        googleApiHelper = new GoogleApiHelper(mInstance);

        createNotificationChannels();

    }

    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "channelId",
                    "Nachrichten",
                    NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Eingehende Nachrichten und Benachrichtigungen");
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    public static synchronized MainApp getInstance() {
        return mInstance;
    }

    public GoogleApiHelper getGoogleApiHelperInstance() {
        return this.googleApiHelper;
    }
    public static GoogleApiHelper getGoogleApiHelper() {
        return getInstance().getGoogleApiHelperInstance();
    }
}
