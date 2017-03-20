package de.wichura.lks.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;
import android.widget.TextView;

import de.wichura.lks.R;
import de.wichura.lks.mainactivity.Constants;

/**
 * Created by bwichura on 20.02.2017.
 * deSurf
 */

public class SetPriceActivity extends AppCompatActivity {

    private TextView priceFromTv;
    private TextView priceToTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.set_price_activity);

        priceFromTv = (TextView) findViewById(R.id.priceFrom);
        priceToTv = (TextView) findViewById(R.id.priceTo);

        priceFromTv.setOnClickListener(view -> priceFromTv.setText(""));
        priceFromTv.setOnTouchListener((view, event) -> {
            priceFromTv.setText("");
            return false;
        });

        priceToTv.setOnClickListener(view -> priceToTv.setText(""));
        priceToTv.setOnTouchListener((view, event) -> {
            priceToTv.setText("");
            return false;
        });

        Toolbar toolbar = (Toolbar) findViewById(R.id.set_price_toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setDisplayShowHomeEnabled(true);
            }
            toolbar.setNavigationOnClickListener((view) -> finish());
        }

        ImageView okButton = (ImageView) findViewById(R.id.store_price);
        if (okButton != null) {
            okButton.setOnClickListener((view) -> {
                getPrices();
                setResult(RESULT_OK, null);
                finish();
            });
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        getPrices();
    }


    private void getPrices() {
        String priceFrom = priceFromTv.getText().toString();
        if ("Beliebig".equals(priceFrom)) {
            priceFrom = "0";
        } else {
            priceFrom = priceFromTv.getText().toString();
        }

        String priceTo = priceToTv.getText().toString();
        if ("Beliebig".equals(priceTo)) {
            //TODO: hoechstgrenze unklar
            priceTo = Constants.MAX_PRICE.toString();
        } else {
            priceTo = priceToTv.getText().toString();
        }
        storePriceRange(priceFrom, priceTo);
    }

    private void storePriceRange(String from, String to) {

        SharedPreferences sp = getSharedPreferences(Constants.USER_PRICE_RANGE, MODE_PRIVATE);
        SharedPreferences.Editor ed = sp.edit();
        ed.putString(Constants.PRICE_FROM, from);
        ed.putString(Constants.PRICE_TO, to);
        ed.apply();
    }
}
