package de.wichura.lks.controller;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.firebase.messaging.FirebaseMessaging;

import de.wichura.lks.gcm.QuickstartPreferences;
import de.wichura.lks.presentation.MainPresenter;

/**
 * Owns the Firebase Cloud Messaging device-token lifecycle for the host Activity.
 * Fetches the token, hands it to the presenter for backend registration, and
 * listens for the legacy REGISTRATION_COMPLETE broadcast used as a diagnostic.
 */
public class PushTokenController {

    private static final String TAG = "CONAN";

    private final Context context;
    private final MainPresenter presenter;
    private final BroadcastReceiver receiver;
    private boolean registered;

    public PushTokenController(Context context, MainPresenter presenter) {
        this.context = context.getApplicationContext();
        this.presenter = presenter;
        this.receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context ctx, Intent intent) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
                boolean sent = prefs.getBoolean(QuickstartPreferences.SENT_TOKEN_TO_SERVER, false);
                Log.d(TAG, sent ? "Token from Firebase received" : "Did not get a Token from GCM!");
            }
        };
    }

    public void register() {
        if (registered) return;
        LocalBroadcastManager.getInstance(context).registerReceiver(receiver,
                new IntentFilter(QuickstartPreferences.REGISTRATION_COMPLETE));
        registered = true;
    }

    public void unregister() {
        if (!registered) return;
        LocalBroadcastManager.getInstance(context).unregisterReceiver(receiver);
        registered = false;
    }

    public void refreshToken() {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String deviceToken = task.getResult();
                Log.d(TAG, "Token from Firebase: " + deviceToken);
                presenter.sendDeviceTokenToBackEndServer(deviceToken);
            } else {
                Log.d(TAG, "Failed to get Firebase token", task.getException());
            }
        });
    }
}
