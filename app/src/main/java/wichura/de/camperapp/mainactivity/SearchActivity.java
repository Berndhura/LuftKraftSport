package wichura.de.camperapp.mainactivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import wichura.de.camperapp.R;

/**
 * Created by Bernd Wichura on 05.04.2016.
 *
 */

public class SearchActivity  extends Activity {

    Button searchButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.search_activity);

        searchButton();
    }

    private void searchButton() {
        searchButton = (Button) findViewById(R.id.search_button);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent data = new Intent();
                data.putExtra("KEYWORDS", ((TextView) findViewById(R.id.keywords)).getText().toString());
                data.putExtra("DATE", ((TextView) findViewById(R.id.date)).getText().toString());
                setResult(RESULT_OK, data);
                finish();
            }
        });
    }
}
