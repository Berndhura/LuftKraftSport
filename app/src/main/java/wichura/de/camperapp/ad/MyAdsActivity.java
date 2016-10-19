package wichura.de.camperapp.ad;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
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
import wichura.de.camperapp.http.Urls;
import wichura.de.camperapp.mainactivity.Constants;
import wichura.de.camperapp.models.RowItem;

import static wichura.de.camperapp.mainactivity.Constants.REQUEST_ID_FOR_NEW_AD;

/**
 * Created by Bernd Wichura on 14.03.2016.
 * CamperApp
 */
public class MyAdsActivity extends AppCompatActivity {

    private ListView listView;
    private List<RowItem> rowItems;
    private MyAdsListViewAdapter adapter;
    private String userId;

    public static final int REQUEST_ID_FOR_OPEN_AD = 3;

    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_ads_main_layout);

        Toolbar toolbar = (Toolbar) findViewById(R.id.my_ads_toolbar);
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

        userId = getIntent().getStringExtra(Constants.USER_ID);
        rowItems = new ArrayList<>();

        getAdsJsonForKeyword(Urls.MAIN_SERVER_URL + Urls.GET_ALL_ADS_FROM_USER + userId);

        ImageView newButton = (ImageView) findViewById(R.id.new_button);
        if (newButton != null) {
            newButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final Intent intent = new Intent(getApplicationContext(), NewAdActivity.class);
                    startActivityForResult(intent, REQUEST_ID_FOR_NEW_AD);
                }
            });
        }
    }

    private void getAdsJsonForKeyword(String url) {
        RequestQueue queue = Volley.newRequestQueue(this);

        JsonArrayRequest getAllAdsFromUserInJson = new JsonArrayRequest(
                Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                try {
                    final Gson gson = new GsonBuilder()
                            .excludeFieldsWithoutExposeAnnotation().create();

                    final JSONArray listOfAllAds = new JSONArray(response.toString());

                    for (int i = 0; i < listOfAllAds.length(); i++) {
                        // get the titel information JSON object
                        final String title = listOfAllAds.getJSONObject(i).toString();
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

        //TODO refactor, same in mainactivity for list elements
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(final AdapterView<?> arg0,
                                    final View arg1, final int position, final long arg3) {

                final RowItem rowItem = (RowItem) listView.getItemAtPosition(position);
                //open new details page with sel. item
                final Intent intent = new Intent(getApplicationContext(),
                        OpenAdActivity.class);
                intent.putExtra(Constants.URI, rowItem.getUrl());
                intent.putExtra(Constants.AD_ID, rowItem.getAdId());
                intent.putExtra(Constants.TITLE, rowItem.getTitle());
                intent.putExtra(Constants.DESCRIPTION, rowItem.getDescription());
                intent.putExtra(Constants.LOCATION, rowItem.getLocation());
                intent.putExtra(Constants.PHONE, rowItem.getPhone());
                intent.putExtra(Constants.PRICE, rowItem.getPrice());
                intent.putExtra(Constants.DATE, rowItem.getDate());
                intent.putExtra(Constants.VIEWS, rowItem.getViews());
                intent.putExtra(Constants.USER_ID_FROM_AD, rowItem.getUserId());
                intent.putExtra(Constants.USER_ID, userId);
                startActivityForResult(intent, REQUEST_ID_FOR_OPEN_AD);
            }
        });
    }
}
