package de.wichura.lks.activity;

import android.content.Intent;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.set_price_activity);

        TextView priceFromTv = (TextView) findViewById(R.id.priceFrom);
        TextView priceToTv = (TextView) findViewById(R.id.priceTo);

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
                String priceFrom = priceFromTv.getText().toString();
                String priceTo = priceToTv.getText().toString();
                Intent result = new Intent();
                result.putExtra(Constants.PRICE_FROM, priceFrom);
                result.putExtra(Constants.PRICE_TO, priceTo);
                setResult(RESULT_OK, result);
                finish();
            });
        }
    }
}
