package wichura.de.camperapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.Button;
import android.widget.TextView;

import wichura.de.camperapp.R;
import wichura.de.camperapp.mainactivity.Constants;

/**
 * Created by Bernd Wichura on 05.04.2016.
 * CamperApp
 */

public class SearchActivity extends AppCompatActivity {

    Button searchButton;

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
        searchButton();
    }

    private void searchButton() {
        searchButton = (Button) findViewById(R.id.search_button);
        searchButton.setOnClickListener(view -> {
            final Intent data = new Intent();
            data.putExtra(Constants.KEYWORDS, ((TextView) findViewById(R.id.keywords)).getText().toString());
            data.putExtra(Constants.PRICE_FROM, ((TextView) findViewById(R.id.price_from)).getText().toString());
            data.putExtra(Constants.PRICE_TO, ((TextView) findViewById(R.id.price_to)).getText().toString());
            setResult(RESULT_OK, data);
            finish();
        });
    }
}
