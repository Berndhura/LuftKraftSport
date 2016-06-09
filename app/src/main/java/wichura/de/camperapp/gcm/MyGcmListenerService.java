package wichura.de.camperapp.gcm;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;

import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;

import wichura.de.camperapp.R;
import wichura.de.camperapp.mainactivity.Constants;
import wichura.de.camperapp.messages.MessagesActivity;

/**
 * Created by Bernd Wichura on 14.05.2016.
 * Camper App
 */
public class MyGcmListenerService  extends GcmListenerService {

    private static final String TAG = "CONAN";

    /**
     * Called when message is received.
     *
     * @param from SenderID of the sender.
     * @param data Data bundle containing message data as key/value pairs.
     *             For Set of keys use data.keySet().
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(String from, Bundle data) {
        String message = data.getString("message");
        String sender = data.getString("sender");
        String adId = data.getString("adId");
        String name = data.getString("name");
        Log.d(TAG, "Received: sender: " + sender);
        Log.d(TAG, "Received: Message: " + message);
        Log.d(TAG, "Received: ADID: " + adId);
        Log.d(TAG, "Received: name: " + name);

        if (from.startsWith("/topics/")) {
            // message received from some topic.
        } else {
            // normal downstream message.
        }

        // [START_EXCLUDE]
        /**
         * Production applications would usually process the message here.
         * Eg: - Syncing with server.
         *     - Store message in local database.
         *     - Update UI.
         */

        /**
         * In some cases it may be useful to show a notification indicating to the user
         * that a message was received.
         */
        sendNotification(message, sender, adId, name);
        // [END_EXCLUDE]
    }
    // [END receive_message]

    /**
     * Create and show a simple notification containing the received GCM message.
     *
     * @param message GCM message received.
     */
    private void sendNotification(String message, String sender, String adId, String name) {
        Intent intent = new Intent(this, MessagesActivity.class);
        intent.putExtra(Constants.SENDER_ID, sender);
        intent.putExtra(Constants.AD_ID, adId);
        intent.putExtra(Constants.MESSAGES_FOR_USER, false);

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.applogo)
                .setContentTitle(name + ": ")
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }
}
