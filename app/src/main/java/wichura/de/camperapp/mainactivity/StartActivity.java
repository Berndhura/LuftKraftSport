package wichura.de.camperapp.mainactivity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

import wichura.de.camperapp.R;

/**
 * Created by Bernd Wichura on 31.03.2016.
 *
 */
public class StartActivity extends AppCompatActivity {

    private Button startButton;
    private Button showButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.start_page_layout);

        Toolbar toolbar = (Toolbar) findViewById(R.id.start_toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        startButton = initStartButton();
        showButton = initShowButton();


    }

    private Button initStartButton() {
        startButton = (Button) findViewById(R.id.firstStartButton);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                Intent intent = new Intent(StartActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
        return startButton;
    }

    private Button initShowButton() {
        showButton = (Button) findViewById(R.id.show_button);
        showButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                Intent intent = new Intent(StartActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
        return showButton;
    }
}
