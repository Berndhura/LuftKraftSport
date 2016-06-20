package wichura.de.camperapp.messages;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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
import wichura.de.camperapp.http.Urls;
import wichura.de.camperapp.mainactivity.Constants;

/**
 * Created by ich on 20.06.2016.
 * Camper App
 */
public class MessagesOverviewActivity extends AppCompatActivity {

    private List<GroupedMsgItem> rowItems;

    private MsgOverviewAdapter adapter;

    private ListView listView;

    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.message_overview_layout);

        listView = (ListView) findViewById(R.id.message_overview_list);

        SharedPreferences settings = getSharedPreferences("UserInfo", 0);
        final String userId = settings.getString(Constants.USER_ID, "");

        final ProgressDialog progressDialog = new ProgressDialog(MessagesOverviewActivity.this,
                R.style.AppTheme);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Loading data...");
        progressDialog.show();
        getMessagesForUser(userId, progressDialog);

    }

    private void getMessagesForUser(String userId, final ProgressDialog dlg) {
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());

        String url = Urls.MAIN_SERVER_URL + Urls.GET_ALL_MESSAGES_FOR_USER + "?userId=" + userId;

        JsonArrayRequest getAllAdsInJson = new JsonArrayRequest(Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                dlg.dismiss();
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
                } catch (final JSONException e) {
                    e.printStackTrace();
                }

                adapter = new MsgOverviewAdapter(
                        context, R.layout.msg_overview_item, rowItems);
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
