package de.wichura.lks.controller;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.ImageView;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import me.leolin.shortcutbadger.ShortcutBadger;

import static de.wichura.lks.mainactivity.Constants.UNREAD_MESSAGES;

/**
 * Owns the in-app message-notification badge: shows the mail button when a
 * "messageReceived" broadcast arrives and reflects the unread-conversation
 * count on the launcher icon via ShortcutBadger.
 */
public class MessageBadgeController {

    private static final String ACTION_MESSAGE_RECEIVED = "messageReceived";

    private final Context context;
    private final ImageView button;
    private final BroadcastReceiver receiver;
    private boolean registered;

    public MessageBadgeController(Context context, ImageView button) {
        this.context = context.getApplicationContext();
        this.button = button;
        this.receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context ctx, Intent intent) {
                button.setVisibility(View.VISIBLE);
                applyLauncherBadge();
            }
        };
    }

    public void register() {
        if (registered) return;
        LocalBroadcastManager.getInstance(context).registerReceiver(receiver,
                new IntentFilter(ACTION_MESSAGE_RECEIVED));
        registered = true;
    }

    public void unregister() {
        if (!registered) return;
        LocalBroadcastManager.getInstance(context).unregisterReceiver(receiver);
        registered = false;
    }

    public void hideButton() {
        button.setVisibility(View.GONE);
    }

    private void applyLauncherBadge() {
        SharedPreferences unread = context.getSharedPreferences(UNREAD_MESSAGES, 0);
        ShortcutBadger.applyCount(context, unread.getAll().size());
    }
}
