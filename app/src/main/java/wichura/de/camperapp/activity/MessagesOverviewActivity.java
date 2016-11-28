package wichura.de.camperapp.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
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

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import wichura.de.camperapp.R;
import wichura.de.camperapp.http.Urls;
import wichura.de.camperapp.mainactivity.Constants;
import wichura.de.camperapp.models.GroupedMsgItem;
import wichura.de.camperapp.adapter.MsgOverviewAdapter;

import static wichura.de.camperapp.mainactivity.Constants.SHARED_PREFS_USER_INFO;

/**
 * Created by ich on 20.06.2016.
 * Camper App
 */
public class MessagesOverviewActivity extends AppCompatActivity {

    private List<GroupedMsgItem> rowItems;

    private MsgOverviewAdapter adapter;

    private ListView listView;

    private ProgressBar mMessagesProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.message_overview_layout);

        Toolbar toolbar = (Toolbar) findViewById(R.id.msg_overview_toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            toolbar.setNavigationOnClickListener((view) -> finish());
        }

        mMessagesProgressBar = (ProgressBar) findViewById(R.id.msg_overview_ProgressBar);

        listView = (ListView) findViewById(R.id.message_overview_list);

        getMessagesForUser(getUserId(), mMessagesProgressBar);
    }

    private void getMessagesForUser(String userId, final ProgressBar dlg) {
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());

        String url = Urls.MAIN_SERVER_URL + Urls.GET_ALL_MESSAGES_FOR_USER + "?userId=" + userId;

        JsonArrayRequest getAllAdsInJson = new JsonArrayRequest(Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                dlg.setVisibility(ProgressBar.GONE);
                Context context = getApplicationContext();
                try {
                    final Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
                    final JSONArray listOfAllAds = new JSONArray(response.toString());
                    rowItems = new ArrayList<>();
                    for (int i = 0; i < listOfAllAds.length(); i++) {
                        final String title = listOfAllAds.getJSONObject(i).toString();
                        //use RowItem class to get from GSON
                        final GroupedMsgItem rowItem = gson.fromJson(title, GroupedMsgItem.class);
                        rowItems.add(rowItem);
                    }
                    getSupportActionBar().setTitle("Messages: " + listOfAllAds.length());
                    getSupportActionBar().setSubtitle(getUserName());
                } catch (final JSONException e) {
                    e.printStackTrace();
                }

                adapter = new MsgOverviewAdapter(
                        context, R.layout.msg_overview_item, rowItems);
                listView.setAdapter(adapter);
                listView.setSelection(listView.getCount() - 1);
                adapter.notifyDataSetChanged();
                listView.setSelectionAfterHeaderView();
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                        final GroupedMsgItem rowItem = (GroupedMsgItem) listView.getItemAtPosition(position);
                        //open message threat
                        final Intent intent = new Intent(getApplicationContext(), MessagesActivity.class);
                        intent.putExtra(Constants.AD_ID, rowItem.getAdId());
                        intent.putExtra(Constants.SENDER_ID, rowItem.getIdTo());
                        intent.putExtra(Constants.ID_FROM, rowItem.getIdFrom());
                        intent.putExtra(Constants.ID_TO, rowItem.getIdTo());
                        intent.putExtra(Constants.SENDER_NAME, rowItem.getName());
                        intent.putExtra(Constants.AD_URL, Urls.MAIN_SERVER_URL + Urls.GET_PICTURE_THUMB + rowItem.getUrl());
                        startActivityForResult(intent, Constants.REQUEST_ID_FOR_MESSAGES);
                    }
                });
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), "Missing network connection!\n" + error.toString(), Toast.LENGTH_LONG).show();
            }
        });
        requestQueue.add(getAllAdsInJson);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.REQUEST_ID_FOR_MESSAGES) {
            getMessagesForUser(getUserId(), mMessagesProgressBar);
        }
    }

    private String getUserId() {
        return getSharedPreferences(SHARED_PREFS_USER_INFO, 0).getString(Constants.USER_ID, "");
    }

    private String getUserName() {
        return getSharedPreferences(SHARED_PREFS_USER_INFO, 0).getString(Constants.USER_NAME, "");
    }
}