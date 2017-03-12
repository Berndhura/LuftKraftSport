package wichura.de.camperapp.activity;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.RelativeLayout;

import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnTabSelectListener;

import wichura.de.camperapp.R;
import wichura.de.camperapp.mainactivity.Constants;

import static wichura.de.camperapp.mainactivity.Constants.SHARED_PREFS_USER_INFO;

/**
 * Created by Bernd Wichura on 05.04.2016.
 * CamperApp
 */

public class SearchActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.basic_search_layout);

        RelativeLayout fraglayout = (RelativeLayout) findViewById(R.id.rl_Container);

        BottomBar bottomBar = (BottomBar) findViewById(R.id.bottomBar);
        bottomBar.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelected(@IdRes int tabId) {
                if (tabId == R.id.tab_location) {

                    android.support.v4.app.FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                    fragmentTransaction.replace(R.id.layout, new LocationFragment());
                    fragmentTransaction.commit();
                }

                if (tabId == R.id.tab_list_saved_searches) {
                    if ("".equals(getUserToken())) {
                        final Intent facebookIntent = new Intent(getApplicationContext(), LoginActivity.class);
                        startActivityForResult(facebookIntent, Constants.REQUEST_ID_FOR_LOGIN);
                    } else {
                        android.support.v4.app.FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                        fragmentTransaction.replace(R.id.layout, new SearchesActivity());
                        fragmentTransaction.commit();
                    }
                }

                if (tabId == R.id.tab_main_search) {
                    android.support.v4.app.FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                    fragmentTransaction.replace(R.id.layout, new SearchFragment());
                    fragmentTransaction.commit();
                }
            }
        });
    }

    public String getUserToken() {
        return getSharedPreferences(SHARED_PREFS_USER_INFO, 0).getString(Constants.USER_TOKEN, "");
    }
}
