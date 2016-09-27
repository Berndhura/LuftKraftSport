package wichura.de.camperapp.messages;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

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

    private List<MsgRowItem> rowItems;

    private ListView listView;

    private MessageListViewAdapter adapter;
    private EditText text;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.messages_layout);

        text = (EditText) findViewById(R.id.edit_message);

        final String adId = getIntent().getStringExtra(Constants.AD_ID);
        final String sender = getIntent().getStringExtra(Constants.SENDER_ID);
        final String senderName = getIntent().getStringExtra(Constants.SENDER_NAME);

        final String userId = getUserId();

        if (!isAllMessagesForUser()) {
            final ProgressDialog progressDialog = new ProgressDialog(MessagesActivity.this, R.style.AppTheme);
            progressDialog.setIndeterminate(true);
            progressDialog.setMessage("Loading data...");
            progressDialog.show();
            getMessages(userId, sender, adId, progressDialog);
        }

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
            getSupportActionBar().setTitle("From: " + senderName);
        }

        if (!isAllMessagesForUser()) {
            ImageView newMsgBtn = (ImageView) findViewById(R.id.send_msg_button);
            if (newMsgBtn != null) {
                newMsgBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        sendMessage(adId, userId, sender, text.getText().toString());

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
        } else {
            ImageView newMsgBtn = (ImageView) findViewById(R.id.send_msg_button);
            newMsgBtn.setVisibility(View.GONE);
        }
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(text.getWindowToken(), 0);
    }

    private String getUserId() {
        SharedPreferences settings = getSharedPreferences("UserInfo", 0);
        return settings.getString(Constants.USER_ID, "");
    }

    private boolean isAllMessagesForUser() {
        return getIntent().getBooleanExtra(Constants.MESSAGES_FOR_USER, false);
    }

    private void sendMessage(final String adId, final String ownerId, final String sender, final String message) {
        //send a message  adId, userId, sender
        Log.d("CONAN", "send message: " + "adId: " + adId + "ownerId: " + ownerId + "sender: " + sender);

        if (message.length() == 0)
            return;
        MessageHelper msgHelper = new MessageHelper(getApplicationContext());
        msgHelper.sendMessageRequest(message, adId, ownerId, sender);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        finish();
    }

    private void getMessages(String userId, String sender, String adId, final ProgressDialog pro) {

        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());

        String url = Urls.MAIN_SERVER_URL + Urls.GET_ALL_MESSAGES_FOR_AD + "?userId=" + userId + "&sender=" + sender + "&adId=" + adId;

        JsonArrayRequest getAllAdsInJson = new JsonArrayRequest(Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                pro.dismiss();
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
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), "Missing network connection!\n" + error.toString(), Toast.LENGTH_LONG).show();
            }
        });
        requestQueue.add(getAllAdsInJson);
    }
}

