package wichura.de.camperapp.ad;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import wichura.de.camperapp.R;
import wichura.de.camperapp.http.Service;
import wichura.de.camperapp.http.Urls;
import wichura.de.camperapp.mainactivity.Constants;
import wichura.de.camperapp.models.AdsAndBookmarks;
import wichura.de.camperapp.models.RowItem;

import static wichura.de.camperapp.mainactivity.Constants.REQUEST_ID_FOR_NEW_AD;

/**
 * Created by Bernd Wichura on 14.03.2016.
 * CamperApp
 */
public class MyAdsActivity extends AppCompatActivity {

    private ListView listView;
    private List<RowItem> rowItems;
    private Service service;

    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_ads_main_layout);

        service = new Service();

        Toolbar toolbar = (Toolbar) findViewById(R.id.my_ads_toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            toolbar.setNavigationOnClickListener((view) -> finish());
        }

        String userId = getIntent().getStringExtra(Constants.USER_ID);
        rowItems = new ArrayList<>();

        getAds(Urls.MAIN_SERVER_URL + Urls.GET_ALL_ADS_FROM_USER + userId);

        ImageView newButton = (ImageView) findViewById(R.id.new_button);
        if (newButton != null) {
            newButton.setOnClickListener((view) -> {
                    final Intent intent = new Intent(getApplicationContext(), NewAdActivity.class);
                    startActivityForResult(intent, REQUEST_ID_FOR_NEW_AD);
            });
        }
    }

    private void getAds(String url) {

        PresenterLayerMyAd presenterLayer = new PresenterLayerMyAd(this, service, getApplicationContext());
        presenterLayer.loadMyAdsData(url);
    }

    public void updateAds(AdsAndBookmarks elements) {

        rowItems = new ArrayList<>();
        for (RowItem e : elements.getAds()) {
            rowItems.add(e);
        }
        showNumberOfAds(elements.getAds().size());

        listView = (ListView) findViewById(R.id.my_list);
        MyAdsListViewAdapter adapter = new MyAdsListViewAdapter(this, getApplicationContext(), R.layout.my_ads_layout, rowItems);
        listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> arg0, final View arg1, final int position, final long arg3) {

                final RowItem rowItem = (RowItem) listView.getItemAtPosition(position);
                final Intent intent = new Intent(getApplicationContext(), OpenAdActivity.class);
                intent.putExtra(Constants.URI, Urls.MAIN_SERVER_URL + "getBild?id=" + rowItem.getUrl());
                intent.putExtra(Constants.AD_ID, rowItem.getAdId());
                intent.putExtra(Constants.TITLE, rowItem.getTitle());
                intent.putExtra(Constants.DESCRIPTION, rowItem.getDescription());
                intent.putExtra(Constants.LOCATION, rowItem.getLocation());
                intent.putExtra(Constants.PHONE, rowItem.getPhone());
                intent.putExtra(Constants.PRICE, rowItem.getPrice());
                intent.putExtra(Constants.DATE, rowItem.getDate());
                intent.putExtra(Constants.VIEWS, rowItem.getViews());
                intent.putExtra(Constants.USER_ID_FROM_AD, rowItem.getUserId());
                intent.putExtra(Constants.USER_ID, getUserId());
                startActivityForResult(intent, Constants.REQUEST_ID_FOR_OPEN_AD);
            }
        });
        setProgressBarIndeterminateVisibility(false);
    }

    private void showNumberOfAds(int numberOfAds) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setSubtitle("Ads: " + numberOfAds);
        }
    }

    private String getUserId() {
        return getApplicationContext().getSharedPreferences("UserInfo", 0).getString(Constants.USER_ID, "");
    }
}
