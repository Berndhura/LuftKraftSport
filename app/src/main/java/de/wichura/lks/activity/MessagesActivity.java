package de.wichura.lks.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import de.wichura.lks.R;
import de.wichura.lks.adapter.MessageListViewAdapter;
import de.wichura.lks.http.Urls;
import de.wichura.lks.mainactivity.Constants;
import de.wichura.lks.models.ArticleDetails;
import de.wichura.lks.models.MsgRowItem;
import de.wichura.lks.presentation.MessagesPresenter;

import static de.wichura.lks.mainactivity.Constants.SHARED_PREFS_USER_INFO;

/**
 * Created by ich on 22.05.2016.
 * Camper App
 */

public class MessagesActivity extends AppCompatActivity {

    public ListView listView;

    private List<MsgRowItem> rowItems;
    private MessageListViewAdapter adapter;
    private EditText text;
    private MessagesPresenter presenter;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        presenter = new MessagesPresenter(this);

        //in case MessageActivity is open, new message will be added to list
        BroadcastReceiver appendChatScreenMsgReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Bundle b = intent.getExtras();
                if (b != null) {
                    final String chatPartner = getIntent().getStringExtra(Constants.CHAT_PARTNER);
                    MsgRowItem it = new MsgRowItem(b.getString(Constants.MESSAGE));
                    it.setSender(chatPartner);
                    rowItems.add(it);
                    adapter.notifyDataSetChanged();
                    listView.setSelection(listView.getCount() - 1);
                }
            }
        };

        LocalBroadcastManager.getInstance(this).registerReceiver(appendChatScreenMsgReceiver,
                new IntentFilter("appendChatScreenMsg"));

        setContentView(R.layout.messages_layout);

        listView = (ListView) findViewById(R.id.message_list);
        text = (EditText) findViewById(R.id.edit_message);

        final Integer articleId = getIntent().getIntExtra(Constants.ARTICLE_ID, 0);
        final String senderName = getIntent().getStringExtra(Constants.SENDER_NAME);
        final String chatPartner = getIntent().getStringExtra(Constants.CHAT_PARTNER);

        Toolbar toolbar = (Toolbar) findViewById(R.id.message_toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            toolbar.setNavigationOnClickListener((view) -> finish());
            getSupportActionBar().setTitle(senderName);
        }

        presenter.loadMessages(getUserToken(), chatPartner, articleId);

        ImageView newMsgBtn = (ImageView) findViewById(R.id.send_msg_button);
        if (newMsgBtn != null) {
            newMsgBtn.setOnClickListener((v) -> {
                sendMessage(text.getText().toString(), articleId, chatPartner);
                //add new message to list
                MsgRowItem it = new MsgRowItem(text.getText().toString());
                rowItems.add(it);
                adapter.notifyDataSetChanged();
                text.setText(null);
                listView.setSelection(listView.getCount() - 1);
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(text.getWindowToken(), 0);
            });
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        setActive();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        setActive();
    }

    @Override
    protected void onStop() {
        super.onStop();
        setInactive();
        presenter.disableSubscription();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        setInactive();
        presenter.disableSubscription();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setActive();
    }

    private void setInactive() {
        SharedPreferences sp = getSharedPreferences(Constants.MESSAGE_ACTIVITY, MODE_PRIVATE);
        SharedPreferences.Editor ed = sp.edit();
        ed.putBoolean("active", false);
        ed.putInt(Constants.ARTICLE_ID, 0);
        ed.apply();
    }

    private void setActive() {
        SharedPreferences sp = getSharedPreferences(Constants.MESSAGE_ACTIVITY, MODE_PRIVATE);
        SharedPreferences.Editor ed = sp.edit();
        ed.putBoolean("active", true);
        ed.putInt(Constants.ARTICLE_ID, getIntent().getIntExtra(Constants.ARTICLE_ID, 0));
        ed.apply();
    }

    private void sendMessage(final String message, final int articleId, final String idTo) {
        if (message.length() == 0)
            return;
        presenter.sendMessage(message, articleId, idTo, getUserToken());
    }

    public void showMessages(List<MsgRowItem> msgRowItems) {
        rowItems = new ArrayList<>();
        rowItems.addAll(msgRowItems);

        adapter = new MessageListViewAdapter(getApplicationContext(), R.layout.list_item, rowItems);
        listView.setAdapter(adapter);
        listView.setSelection(listView.getCount() - 1);
        adapter.notifyDataSetChanged();

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(text.getWindowToken(), 0);
    }

    /*public void enableProgress() {
        progressBar.setVisibility(ProgressBar.VISIBLE);
    }

    public void disableProgress() {
        progressBar.setVisibility(ProgressBar.GONE);
    }*/

    private String getUserId() {
        SharedPreferences settings = getSharedPreferences(SHARED_PREFS_USER_INFO, 0);
        return settings.getString(Constants.USER_ID, "");
    }

    private String getUserToken() {
        SharedPreferences settings = getSharedPreferences(SHARED_PREFS_USER_INFO, 0);
        return settings.getString(Constants.USER_TOKEN, "");
    }

    public void showLinkToAdButton() {
        Button link = (Button) findViewById(R.id.link_to_ad_button);
        link.setOnClickListener((view) -> {
            Integer articleId = getIntent().getIntExtra(Constants.ARTICLE_ID, 0);
            presenter.getAd(articleId);
        });
    }

    public void openAdActivityFor(ArticleDetails rowItem) {
        double lat = rowItem.getLocation().getCoordinates()[0];
        double lng = rowItem.getLocation().getCoordinates()[1];

        final Intent intent = new Intent(getApplicationContext(), OpenAdActivity.class);
        intent.putExtra(Constants.URI, Urls.MAIN_SERVER_URL_V3 + "pictures/" + rowItem.getUrls());
        intent.putExtra(Constants.ARTICLE_ID, rowItem.getId());
        intent.putExtra(Constants.ID, rowItem.getId());
        intent.putExtra(Constants.TITLE, rowItem.getTitle());
        intent.putExtra(Constants.DESCRIPTION, rowItem.getDescription());
        intent.putExtra(Constants.LAT, lat);
        intent.putExtra(Constants.LNG, lng);
        intent.putExtra(Constants.PRICE, rowItem.getPrice());
        intent.putExtra(Constants.DATE, rowItem.getDate());
        intent.putExtra(Constants.VIEWS, rowItem.getViews());
        intent.putExtra(Constants.USER_ID_FROM_AD, rowItem.getUserId());
        intent.putExtra(Constants.USER_ID, getUserId());
        startActivityForResult(intent, Constants.REQUEST_ID_FOR_OPEN_AD);
    }
}