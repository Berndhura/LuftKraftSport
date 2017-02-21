package wichura.de.camperapp.mainactivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import java.util.Timer;
import java.util.TimerTask;

import wichura.de.camperapp.R;
import wichura.de.camperapp.http.StartActivity;

/**
 * Created by Bernd Wichura on 10.05.2016.
 * Camper App
 */
public class Splash extends Activity {

    private final static int STR_SPLASH_TIME = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        startSplashTimer();
    }

    private void startSplashTimer() {
        try {
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {

                @Override
                public void run() {
                    Intent intent = new Intent(Splash.this, StartActivity.class);
                    startActivity(intent);
                    finish();
                }
            }, STR_SPLASH_TIME);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
