package wichura.de.camperapp.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import wichura.de.camperapp.R;
import wichura.de.camperapp.mainactivity.Constants;

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

        setContentView(R.layout.search_activity);

        Toolbar toolbar = (Toolbar) findViewById(R.id.search_toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setDisplayShowHomeEnabled(true);
            }
            toolbar.setNavigationOnClickListener((view) -> finish());
        }
        createGui();

        showLocation();
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

            setResult(RESULT_OK, data);
            finish();
        });

        changePriceBtn.setOnClickListener(view -> {
            Intent priceIntent = new Intent(getApplicationContext(), SetPriceActivity.class);
            startActivityForResult(priceIntent, Constants.REQUEST_ID_FOR_PRICE);
        });

        ImageView locationButton = (ImageView) findViewById(R.id.set_location);
        locationButton.setOnClickListener(view -> {
            Intent location = new Intent(getApplicationContext(), SetLocationActivity.class);
            startActivityForResult(location, Constants.REQUEST_ID_FOR_LOCATION);
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
        getSupportActionBar()
                .setSubtitle("in " + location.getString(Constants.LOCATION, "") + " (+" + location.getInt(Constants.DISTANCE, 0) + " km)");
    }
}
