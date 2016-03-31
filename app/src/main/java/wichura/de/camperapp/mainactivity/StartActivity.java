package wichura.de.camperapp.mainactivity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import wichura.de.camperapp.R;

/**
 * Created by Bernd Wichura on 31.03.2016.
 */
public class StartActivity  extends Activity{

    private Button startButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.start_page_layout);

        startButton = initStartButton();
    }

    private Button initStartButton() {
        startButton = (Button) findViewById(R.id.firstStartButton);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                finish();
            }
        });
        return startButton;
    }
}
