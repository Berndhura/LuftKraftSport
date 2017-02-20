package wichura.de.camperapp.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import wichura.de.camperapp.R;

/**
 * Created by bwichura on 20.02.2017.
 * deSurf
 */

public class SetPriceActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.set_price_activity);

        TextView priceFrom = (TextView) findViewById(R.id.priceFrom);
        TextView priceTo = (TextView) findViewById(R.id.priceTo);

        Toolbar toolbar = (Toolbar) findViewById(R.id.set_price_toolbar);
        if (toolbar != null) {
           // setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setDisplayShowHomeEnabled(true);
            }
            toolbar.setNavigationOnClickListener((view) -> finish());
        }

    }

}
