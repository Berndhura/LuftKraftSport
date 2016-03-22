package wichura.de.camperapp.ad;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import wichura.de.camperapp.R;
import wichura.de.camperapp.http.Urls;
import wichura.de.camperapp.mainactivity.RowItem;

/**
 * Created by Bernd Wichura on 14.03.2016.
 */
public class MyAdsActivity extends Activity {

    private ListView listView;
    private List<RowItem> rowItems;
    private MyAdsListViewAdapter adapter;
    private String userId;

    public static final int REQUEST_ID_FOR_OPEN_AD = 3;

    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_ads_main_layout);

        userId = getIntent().getStringExtra("userid");

        rowItems = new ArrayList<RowItem>();

        getAdsJsonForKeyword(Urls.MAIN_SERVER_URL + Urls.GET_ALL_ADS_FROM_USER + userId);


    }

    private void getAdsJsonForKeyword(String url) {
        RequestQueue queue = Volley.newRequestQueue(this);

        JsonArrayRequest getAllAdsFromUserInJson = new JsonArrayRequest(
                Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                Context context = getApplicationContext();
                try {
                    final Gson gson = new GsonBuilder()
                            .excludeFieldsWithoutExposeAnnotation().create();

                    final JSONArray listOfAllAds = new JSONArray(response.toString());

                    for (int i = 0; i < listOfAllAds.length(); i++) {
                        // get the titel information JSON object
                        final String title = listOfAllAds.getJSONObject(i)
                                .toString();
                        //use RowItem class to get from GSON
                        final RowItem rowItem = gson.fromJson(title, RowItem.class);

                        rowItems.add(rowItem);
                    }
                } catch (final JSONException e) {
                    e.printStackTrace();
                }
                createAdapter(rowItems);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), "Missing network connection!\n" + error.toString(), Toast.LENGTH_LONG).show();
            }
        });
        queue.add(getAllAdsFromUserInJson);
        //TODO:refactor end
    }

    private void createAdapter(List<RowItem> rowItems) {

        listView = (ListView) findViewById(R.id.my_list);
        adapter = new MyAdsListViewAdapter(this, getApplicationContext(), R.layout.my_ads_layout, rowItems);
        listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    public void refreshList()
    {
        adapter.notifyDataSetChanged();
    }



}
