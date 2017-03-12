package wichura.de.camperapp.activity;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnTabSelectListener;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import wichura.de.camperapp.R;
import wichura.de.camperapp.http.Service;
import wichura.de.camperapp.mainactivity.Constants;

import static wichura.de.camperapp.mainactivity.Constants.DISTANCE_INFINITY;
import static wichura.de.camperapp.mainactivity.Constants.SHARED_PREFS_USER_INFO;

/**
 * Created by Bernd Wichura on 05.04.2016.
 * CamperApp
 */

public class SearchActivity extends AppCompatActivity {

    private TextView keywords;
    private TextView price;
    private String priceTo;
    private String priceFrom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.basic_search_layout);

        RelativeLayout fraglayout = (RelativeLayout) findViewById(R.id.rl_Container);

        Toolbar toolbar = (Toolbar) findViewById(R.id.search_toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setDisplayShowHomeEnabled(true);
            }
            toolbar.setNavigationOnClickListener((view) -> finish());
        }
        //createGui();

        //showLocation();

        BottomBar bottomBar = (BottomBar) findViewById(R.id.bottomBar);
        bottomBar.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelected(@IdRes int tabId) {
                if (tabId == R.id.tab_location) {

                    android.support.v4.app.FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                    fragmentTransaction.replace(R.id.layout, new LocationFragment());
                    fragmentTransaction.commit();
                }

                if (tabId == R.id.tab_list_saved_searches) {
                    if ("".equals(getUserToken())) {
                        final Intent facebookIntent = new Intent(getApplicationContext(), LoginActivity.class);
                        startActivityForResult(facebookIntent, Constants.REQUEST_ID_FOR_LOGIN);
                    } else {
                        //Intent intent = new Intent(getApplicationContext(), SearchesActivity.class);
                        //startActivityForResult(intent, Constants.REQUEST_ID_FOR_SEARCHES);
                        android.support.v4.app.FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                        fragmentTransaction.replace(R.id.layout, new SearchesActivity());
                        fragmentTransaction.commit();
                    }
                }

                if (tabId == R.id.tab_follow_search) {
                    //TODO: check if loged in before save
                    saveSearch();
                }

                if (tabId == R.id.tab_main_search) {
                    Toast.makeText(SearchActivity.this, "MAIN", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }


    private void createGui() {
        keywords = (TextView) findViewById(R.id.keywords);
        price = (TextView) findViewById(R.id.price_from);
        ImageView changePriceBtn = (ImageView) findViewById(R.id.changePrice);

        Button searchButton = (Button) findViewById(R.id.search_button);

        searchButton.setOnClickListener(view -> {
            final Intent data = new Intent();
            data.putExtra(Constants.KEYWORDS, keywords.getText().toString());

            if (getString(R.string.price_does_not_matter).equals(price.getText().toString())) {
                data.putExtra(Constants.PRICE_FROM, "");
                data.putExtra(Constants.PRICE_TO, "");
            } else {

                if (getString(R.string.price_does_not_matter).equals(priceFrom)) {
                    data.putExtra(Constants.PRICE_FROM, "");
                } else {
                    data.putExtra(Constants.PRICE_FROM, priceFrom);
                }

                if (getString(R.string.price_does_not_matter).equals(priceTo)) {
                    data.putExtra(Constants.PRICE_TO, "");
                } else {
                    data.putExtra(Constants.PRICE_TO, priceTo);
                }
            }

            data.putExtra(Constants.DISTANCE, getDistance());

            setResult(RESULT_OK, data);
            finish();
        });

        changePriceBtn.setOnClickListener(view -> {
            Intent priceIntent = new Intent(getApplicationContext(), SetPriceActivity.class);
            startActivityForResult(priceIntent, Constants.REQUEST_ID_FOR_PRICE);
        });
    }

    private void saveSearch() {
        String description = keywords.getText().toString();
        Service service = new Service();

        //TODO: richtige werte bitte!
        service.saveSearchObserv(description, 1, 5000, getLat(), getLng(), getDistance(), getUserToken())
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<String>() {
                    @Override
                    public void onCompleted() {
                        Toast.makeText(getApplicationContext(), "Suche abgespeichert!", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d("CONAN", "error saving searches: " + e.getMessage());
                    }

                    @Override
                    public void onNext(String result) {

                    }
                });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constants.REQUEST_ID_FOR_LOCATION) {
            showLocation();

        } else if (requestCode == Constants.REQUEST_ID_FOR_PRICE) {
            priceFrom = data.getStringExtra(Constants.PRICE_FROM);
            priceTo = data.getStringExtra(Constants.PRICE_TO);
            adaptLayoutForPrice(priceFrom, priceTo);
        }
    }

    private void adaptLayoutForPrice(String from, String to) {
        if (getString(R.string.price_does_not_matter).equals(priceFrom)) {
            price.setText(R.string.price_does_not_matter);
        } else {
            price.setText(from + " bis " + to);
        }
    }

    private void showLocation() {
        SharedPreferences location = getSharedPreferences(Constants.USERS_LOCATION, 0);
        int distance = location.getInt(Constants.DISTANCE, DISTANCE_INFINITY);
        if (getSupportActionBar() != null) getSupportActionBar()
                .setSubtitle("in " + location.getString(Constants.LOCATION, "") + ((distance == DISTANCE_INFINITY) ? (" Unbegrenzt") : (" (+" + location.getInt(Constants.DISTANCE, 0) / 1000 + " km)")));
    }

    public String getUserToken() {
        return getSharedPreferences(SHARED_PREFS_USER_INFO, 0).getString(Constants.USER_TOKEN, "");
    }

    public Double getLng() {
        SharedPreferences settings = getApplicationContext().getSharedPreferences(Constants.USERS_LOCATION, 0);
        return Double.longBitsToDouble(settings.getLong(Constants.LNG, 0));
    }

    public int getDistance() {
        SharedPreferences settings = getApplicationContext().getSharedPreferences(Constants.USERS_LOCATION, 0);
        return settings.getInt(Constants.DISTANCE, Constants.DISTANCE_INFINITY);
    }


    public Double getLat() {
        SharedPreferences settings = getApplicationContext().getSharedPreferences(Constants.USERS_LOCATION, 0);
        return Double.longBitsToDouble(settings.getLong(Constants.LAT, 0));
    }
}
