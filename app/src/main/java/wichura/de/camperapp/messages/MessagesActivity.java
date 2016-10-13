package wichura.de.camperapp.messages;

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
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import wichura.de.camperapp.R;
import wichura.de.camperapp.http.MessageHelper;
import wichura.de.camperapp.http.Urls;
import wichura.de.camperapp.mainactivity.Constants;

/**
 * Created by ich on 22.05.2016.
 * Camper App
 */
public class MessagesActivity extends AppCompatActivity {

    private ProgressBar mMessagesProgressBar;
    private List<MsgRowItem> rowItems;
    private ListView listView;
    private MessageListViewAdapter adapter;
    private EditText text;
    private BroadcastReceiver appendChatScreenMsgReceiver;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //in case MessageActivity is open, new message will be added to list
        appendChatScreenMsgReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Bundle b = intent.getExtras();
                if (b != null) {
                    final String idFrom = getIntent().getStringExtra(Constants.ID_FROM);
                    MsgRowItem it = new MsgRowItem(b.getString(Constants.MESSAGE));
                    it.setSender(idFrom);
                    rowItems.add(it);
                    adapter.notifyDataSetChanged();
                    listView.setSelection(listView.getCount() - 1);
                }
            }
        };

        LocalBroadcastManager.getInstance(this).registerReceiver(appendChatScreenMsgReceiver,
                new IntentFilter("appendChatScreenMsg"));

        setContentView(R.layout.messages_layout);

        text = (EditText) findViewById(R.id.edit_message);

        final String adId = getIntent().getStringExtra(Constants.AD_ID);
        final String sender = getIntent().getStringExtra(Constants.SENDER_ID);
        final String senderName = getIntent().getStringExtra(Constants.SENDER_NAME);
        final String idFrom = getIntent().getStringExtra(Constants.ID_FROM);
        final String idTo = getIntent().getStringExtra(Constants.ID_TO);
        final String adPicUrl = getIntent().getStringExtra(Constants.AD_URL);

        Log.d("CONAN", "enter message: " + "adId: " + adId + "sender: " + sender + "idFrom: " + idFrom + "idTo: " + idTo);

        final String userId = getUserId();

        listView = (ListView) findViewById(R.id.message_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.message_toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    finish();
                }
            });
            getSupportActionBar().setTitle(senderName);
            //getSupportActionBar().setLogo(R.drawable.applogo);

            LayoutInflater mInflater = LayoutInflater.from(this);

            // View mCustomView = mInflater.inflate(R.id.message_toolbar, null);

            //  getSupportActionBar().setCustomView(mCustomView);
            // getSupportActionBar().setDisplayShowCustomEnabled(true);

            if (adPicUrl != null) {
                ImageView thumbNail = (ImageView) findViewById(R.id.ad_in_toolbar);
                Picasso.with(getApplicationContext()).load(adPicUrl.toString()).into(thumbNail);
                thumbNail.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
/*                      TODO: open openAdActivity, need all information in intent??? like description, title...
                        TODO: here is the link between message and Ad!!!
                        final Intent intent = new Intent(getApplicationContext(), OpenAdActivity.class);
                        intent.putExtra(Constants.URI, getIntent().getStringExtra(Constants.AD_URL));
                        intent.putExtra(Constants.AD_ID, getIntent().getStringExtra(Constants.AD_ID));
                        intent.putExtra(Constants.USER_ID, getUserId());
                        startActivityForResult(intent, Constants.REQUEST_ID_FOR_MESSAGES);*/
                        Toast.makeText(getApplicationContext(), "Open Ad for details", Toast.LENGTH_SHORT).show();

                    }
                });
            }
        }

        mMessagesProgressBar = (ProgressBar) findViewById(R.id.msg_ProgressBar);

        getMessages(idTo, idFrom, adId, mMessagesProgressBar);

        ImageView newMsgBtn = (ImageView) findViewById(R.id.send_msg_button);
        if (newMsgBtn != null) {
            newMsgBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //always send an answer to chat partner (the other), not yourself
                    String localSender;
                    if (userId.equals(idFrom)) {
                        localSender = idTo;
                    } else {
                        localSender = idFrom;
                    }
                    sendMessage(adId, userId, localSender, text.getText().toString());
                    //add new message to list
                    MsgRowItem it = new MsgRowItem(text.getText().toString());
                    rowItems.add(it);
                    adapter.notifyDataSetChanged();
                    text.setText(null);
                    //TODO add message to list
                    listView.setSelection(listView.getCount() - 1);
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(text.getWindowToken(), 0);
                }
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
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        setInactive();
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
        ed.putString("adId", "");
        ed.commit();
    }

    private void setActive() {
        SharedPreferences sp = getSharedPreferences(Constants.MESSAGE_ACTIVITY, MODE_PRIVATE);
        SharedPreferences.Editor ed = sp.edit();
        ed.putBoolean("active", true);
        ed.putString("adId", getIntent().getStringExtra(Constants.AD_ID));
        Log.d("adId: ", getIntent().getStringExtra(Constants.AD_ID));
        ed.apply();
    }

    private void sendMessage(final String adId, final String ownerId, final String sender, final String message) {
        //send a message  adId, userId, sender
        Log.d("CONAN", "send message: " + "adId: " + adId + "ownerId: " + ownerId + "sender: " + sender);

        if (message.length() == 0)
            return;
        MessageHelper msgHelper = new MessageHelper(getApplicationContext());
        msgHelper.sendMessageRequest(message, adId, ownerId, sender);
    }

    private void getMessages(String userId, String sender, String adId, final ProgressBar progress) {

        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());

        String url = Urls.MAIN_SERVER_URL + Urls.GET_ALL_MESSAGES_FOR_AD + "?userId=" + userId + "&sender=" + sender + "&adId=" + adId;
        Log.d("CONAN", "get message: " + "adId: " + adId + " userId: " + userId + " sender: " + sender);

        JsonArrayRequest getAllAdsInJson = new JsonArrayRequest(Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                progress.setVisibility(ProgressBar.GONE);
                Context context = getApplicationContext();
                try {
                    final Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
                    final JSONArray listOfAllAds = new JSONArray(response.toString());
                    rowItems = new ArrayList<>();
                    for (int i = 0; i < listOfAllAds.length(); i++) {
                        final String title = listOfAllAds.getJSONObject(i).toString();
                        //use RowItem class to get from GSON
                        final MsgRowItem rowItem = gson.fromJson(title, MsgRowItem.class);
                        rowItems.add(rowItem);
                    }
                } catch (final JSONException e) {
                    e.printStackTrace();
                }

                adapter = new MessageListViewAdapter(
                        context, R.layout.list_item, rowItems);
                listView.setAdapter(adapter);
                listView.setSelection(listView.getCount() - 1);
                adapter.notifyDataSetChanged();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(text.getWindowToken(), 0);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), "Missing network connection!\n" + error.toString(), Toast.LENGTH_LONG).show();
            }
        });
        requestQueue.add(getAllAdsInJson);
    }

    private String getUserId() {
        SharedPreferences settings = getSharedPreferences("UserInfo", 0);
        return settings.getString(Constants.USER_ID, "");
    }
}