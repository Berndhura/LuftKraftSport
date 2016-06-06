package wichura.de.camperapp.messages;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
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

    private TextView newMessage;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.messages_layout);

        final String adId = getIntent().getStringExtra(Constants.AD_ID);
        final String sender = getIntent().getStringExtra(Constants.SENDER_ID);

        SharedPreferences settings = getSharedPreferences("UserInfo", 0);
        final String userId = settings.getString(Constants.USER_ID, "");
        getMessages(userId, sender, adId);

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
        }

        ImageView newMsgBtn = (ImageView) findViewById(R.id.new_msg_button);
        if (newMsgBtn != null) {
            newMsgBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    sendMessage(adId, userId, sender); //TODO parameter ok?
                }
            });
        }

    }


    private void sendMessage(final String adId, final String ownerId, final String sender) {
        //send a message
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        final EditText edittext = new EditText(MessagesActivity.this);
        alert.setTitle("Send a message");
        alert.setView(edittext);
        alert.setPositiveButton("Send", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String message = edittext.getText().toString();
                MessageHelper msgHelper = new MessageHelper(getApplicationContext());
                msgHelper.sendMessageRequest(message, adId, ownerId, sender);
            }
        });
        alert.setNegativeButton("not yet", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //just go away...
            }
        });
        alert.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        finish();
    }

    private void getMessages(String userId, String sender, String adId) {

        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());

        String url = Urls.MAIN_SERVER_URL + Urls.GET_ALL_MESSAGES_FOR_USER + "?userId=" + userId + "&sender=" + sender + "&adId=" + adId;

        JsonArrayRequest getAllAdsInJson = new JsonArrayRequest(Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
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

