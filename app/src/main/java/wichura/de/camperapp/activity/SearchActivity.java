package wichura.de.camperapp.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import wichura.de.camperapp.R;
import wichura.de.camperapp.gui.Widget;
import wichura.de.camperapp.mainactivity.Constants;

/**
 * Created by Bernd Wichura on 05.04.2016.
 * CamperApp
 */

public class SearchActivity extends AppCompatActivity {

    private Button searchButton;
    private TextView keywords;
    private TextView priceFrom;
    private TextView priceTo;
    private Spinner distance;
    private ImageView locationButton;


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
    }

    private void createGui() {
        keywords = (TextView) findViewById(R.id.keywords);
        priceFrom = (TextView) findViewById(R.id.price_from);
        priceTo = (TextView) findViewById(R.id.price_to);
        distance = (Spinner) findViewById(R.id.spinner_distance);
        Widget.addItemsOnSpinner(this, distance);
        searchButton = (Button) findViewById(R.id.search_button);

        searchButton.setOnClickListener(view -> {
            final Intent data = new Intent();
            data.putExtra(Constants.KEYWORDS, keywords.getText().toString());
            data.putExtra(Constants.PRICE_FROM, priceFrom.getText().toString());
            data.putExtra(Constants.PRICE_TO, priceTo.getText().toString());
            data.putExtra(Constants.DISTANCE, Widget.getDistanceFromSpinner(distance));
            setResult(RESULT_OK, data);
            finish();
        });

        locationButton = (ImageView) findViewById(R.id.set_location);
        locationButton.setOnClickListener(view -> {
            Intent location = new Intent(getApplicationContext(), SetLocationActivity.class);
            startActivityForResult(location, Constants.REQUEST_ID_FOR_LOCATION);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constants.REQUEST_ID_FOR_LOCATION) {
            showLocation(data);
        }
    }

    private void showLocation(Intent data) {
        SharedPreferences location = getSharedPreferences(Constants.USERS_LOCATION, 0);
        getSupportActionBar().setSubtitle("in " + location.getString(Constants.LOCATION, ""));
    }
}
