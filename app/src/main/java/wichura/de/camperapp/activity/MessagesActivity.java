package wichura.de.camperapp.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import wichura.de.camperapp.R;
import wichura.de.camperapp.adapter.MessageListViewAdapter;
import wichura.de.camperapp.http.Urls;
import wichura.de.camperapp.mainactivity.Constants;
import wichura.de.camperapp.models.MsgRowItem;
import wichura.de.camperapp.models.RowItem;
import wichura.de.camperapp.presentation.MessagesPresenter;

import static wichura.de.camperapp.mainactivity.Constants.SHARED_PREFS_USER_INFO;

/**
 * Created by ich on 22.05.2016.
 * Camper App
 */

public class MessagesActivity extends AppCompatActivity {

    //public AVLoadingIndicatorView progressBar;
    private List<MsgRowItem> rowItems;
    public ListView listView;
    private MessageListViewAdapter adapter;
    private EditText text;
    private BroadcastReceiver appendChatScreenMsgReceiver;

    private MessagesPresenter presenter;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        presenter = new MessagesPresenter(this);

        //in case MessageActivity is open, new message will be added to list
        appendChatScreenMsgReceiver = new BroadcastReceiver() {
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
        //progressBar = (AVLoadingIndicatorView) findViewById(R.id.progressBar);
        text = (EditText) findViewById(R.id.edit_message);

        final String articleId = getIntent().getStringExtra(Constants.ARTICLE_ID);
        final String senderName = getIntent().getStringExtra(Constants.SENDER_NAME);
        final String chatPartner = getIntent().getStringExtra(Constants.CHAT_PARTNER);

        Toolbar toolbar = (Toolbar) findViewById(R.id.message_toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            toolbar.setNavigationOnClickListener((view) -> finish());
            getSupportActionBar().setTitle(senderName);


            LayoutInflater mInflater = LayoutInflater.from(this);

            // View mCustomView = mInflater.inflate(R.id.message_toolbar, null);

            //  getSupportActionBar().setCustomView(mCustomView);
            // getSupportActionBar().setDisplayShowCustomEnabled(true);

            /*                      TODO: open openAdActivity, need all information in intent??? like description, title...
                        TODO: here is the link between message and Ad!!!
                        final Intent intent = new Intent(getApplicationContext(), OpenAdActivity.class);
                        intent.putExtra(Constants.URI, getIntent().getStringExtra(Constants.AD_URL));
                        intent.putExtra(Constants.AD_ID, getIntent().getStringExtra(Constants.AD_ID));
                        intent.putExtra(Constants.USER_ID, getUserId());
                        startActivityForResult(intent, Constants.REQUEST_ID_FOR_MESSAGES);*/


           /* if (adPicUrl != null) {
                ImageView thumbNail = (ImageView) findViewById(R.id.ad_in_toolbar);
                Picasso.with(getApplicationContext()).load(adPicUrl).into(thumbNail);
                thumbNail.setOnClickListener(v-> Toast.makeText(getApplicationContext(), "Open Ad for details", Toast.LENGTH_SHORT).show());
            }*/
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
        ed.putString(Constants.ARTICLE_ID, "");
        ed.apply();
    }

    private void setActive() {
        SharedPreferences sp = getSharedPreferences(Constants.MESSAGE_ACTIVITY, MODE_PRIVATE);
        SharedPreferences.Editor ed = sp.edit();
        ed.putBoolean("active", true);
        ed.putString(Constants.ARTICLE_ID, getIntent().getStringExtra(Constants.ARTICLE_ID));
        Log.d("articleId: ", getIntent().getStringExtra(Constants.ARTICLE_ID));
        ed.apply();
    }

    private void sendMessage(final String message, final String articleId, final String idTo) {
        if (message.length() == 0)
            return;

        presenter.sendMessage(message, articleId, idTo, getUserToken());
    }

    public void showMessages(List<MsgRowItem> msgRowItems) {
        rowItems = new ArrayList<>();
        for (MsgRowItem e : msgRowItems) {
            rowItems.add(e);
        }

        adapter = new MessageListViewAdapter(getApplicationContext(), R.layout.list_item, rowItems);
        listView.setAdapter(adapter);
        listView.setSelection(listView.getCount() - 1);
        adapter.notifyDataSetChanged();

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(text.getWindowToken(), 0);
    }

    // public void enableProgress() {
    //    progressBar.setVisibility(ProgressBar.VISIBLE);
    //}

    // public void disableProgress() {
    //    progressBar.setVisibility(ProgressBar.GONE);
    // }

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
            String articleId = getIntent().getStringExtra(Constants.ARTICLE_ID);
            presenter.getAd(articleId);
        });
    }

    public void openAdActivityFor(RowItem rowItem) {
        final Intent intent = new Intent(getApplicationContext(), OpenAdActivity.class);
        intent.putExtra(Constants.URI, Urls.MAIN_SERVER_URL_V3 + "pictures/" + rowItem.getUrl());
        intent.putExtra(Constants.ARTICLE_ID, rowItem.getId());
        intent.putExtra(Constants.TITLE, rowItem.getTitle());
        intent.putExtra(Constants.DESCRIPTION, rowItem.getDescription());
        //intent.putExtra(Constants.LOCATION, rowItem.getLocation());
        intent.putExtra(Constants.PHONE, rowItem.getPhone());
        intent.putExtra(Constants.PRICE, rowItem.getPrice());
        intent.putExtra(Constants.DATE, rowItem.getDate());
        intent.putExtra(Constants.VIEWS, rowItem.getViews());
        intent.putExtra(Constants.USER_ID_FROM_AD, rowItem.getUserId());
        intent.putExtra(Constants.USER_ID, getUserId());
        startActivityForResult(intent, Constants.REQUEST_ID_FOR_OPEN_AD);
    }
}