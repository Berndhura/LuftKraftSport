package de.wichura.lks.gcm;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import de.wichura.lks.R;
import de.wichura.lks.activity.MessagesActivity;
import de.wichura.lks.activity.OpenAdActivity;
import de.wichura.lks.mainactivity.Constants;

import static de.wichura.lks.mainactivity.Constants.SHARED_PREFS_USER_INFO;
import static de.wichura.lks.mainactivity.Constants.UNREAD_MESSAGES;

/**
 * Created by Bernd Wichura on 14.05.2016.
 * <p>
 * Luftkraftsport
 */
public class MyGcmListenerService extends FirebaseMessagingService {

    private static final String TAG = "CONAN";

    /**
     * Called when message is received.
     *
     * @param message Data bundle containing message data as key/value pairs.
     *                For Set of keys use data.keySet().
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(RemoteMessage message) {

        //{"message":"joMOFO","sender":"109156770575781620767","type":"message","articleId":"4165","adUrl":"4166","name":"john doe"}

        if ("message".equals(message.getData().get("type"))) {

            String messageText = message.getData().get("message");
            String sender = message.getData().get("sender");
            String articleId = message.getData().get("articleId");
            String name = message.getData().get("name");
            Log.v(TAG, "Received: sender: " + sender);
            Log.v(TAG, "Received: Message: " + message);
            Log.v(TAG, "Received: articleId: " + articleId);
            Log.v(TAG, "Received: name: " + name);

            pushToUnreadMessages(articleId, sender);

            updateMessageSymbol();

            if (isMessageActivityActive() && getAdIdFromSharedPref().equals(Integer.parseInt(articleId))) {
                updateChat(messageText, sender);
            } else {
                sendNotification(messageText, sender, Integer.parseInt(articleId), name);
            }

        } else if ("article".equals(message.getData().get("type"))) {
            String messageText = message.getData().get("message");
            String sender = message.getData().get("sender");
            String articleId = message.getData().get("articleId");
            String name = message.getData().get("name");
            openArticle(messageText, Integer.parseInt(articleId), name);
        }
    }

    private void pushToUnreadMessages(String articleId, String sender) {
        SharedPreferences settings = getSharedPreferences(UNREAD_MESSAGES, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(articleId + "," + sender, true);
        editor.apply();
    }
    // [END receive_message]

    private void updateChat(String message, String sender) {
        Intent updateChat = new Intent("appendChatScreenMsg");
        updateChat.putExtra(Constants.MESSAGE, message);
        updateChat.putExtra(Constants.CHAT_PARTNER, sender);
        LocalBroadcastManager.getInstance(this).sendBroadcast(updateChat);
    }

    private void updateMessageSymbol() {
        Intent updateMsgCounter = new Intent("messageReceived");
        LocalBroadcastManager.getInstance(this).sendBroadcast(updateMsgCounter);
    }

    private void openArticle(String message, Integer articleId, String name) {
        Intent intent = new Intent(this, OpenAdActivity.class);
        intent.putExtra(Constants.ID, articleId);
        Log.d("CONAN", "gcm listener article details: " + articleId);
        intent.putExtra(Constants.NOTIFICATION_TYPE, "article");

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.surfing_filled_50)
                .setContentTitle(name + ": ")
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }

    /**
     * Create and show a simple notification containing the received GCM message.
     *
     * @param message GCM message received.
     */
    private void sendNotification(String message, String sender, Integer articleId, String name) {
        Intent intent = new Intent(this, MessagesActivity.class);
        intent.putExtra(Constants.SENDER_ID, sender);
        intent.putExtra(Constants.SENDER_NAME, name);
        intent.putExtra(Constants.CHAT_PARTNER, sender);
        intent.putExtra(Constants.ID_TO, getUserId());
        intent.putExtra(Constants.ARTICLE_ID, articleId);

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.surfing_filled_50)
                .setContentTitle(name + ": ")
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }

    private String getUserId() {
        return getSharedPreferences(SHARED_PREFS_USER_INFO, 0).getString(Constants.USER_ID, "");
    }

    private Integer getAdIdFromSharedPref() {
        return getSharedPreferences(Constants.MESSAGE_ACTIVITY, MODE_PRIVATE).getInt(Constants.ARTICLE_ID, 0);
    }

    private Boolean isMessageActivityActive() {
        return getSharedPreferences(Constants.MESSAGE_ACTIVITY, MODE_PRIVATE).getBoolean("active", false);
    }
}
