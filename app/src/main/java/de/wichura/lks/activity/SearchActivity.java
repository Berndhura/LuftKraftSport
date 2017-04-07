package de.wichura.lks.activity;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.widget.RelativeLayout;

import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnTabSelectListener;

import de.wichura.lks.R;
import de.wichura.lks.dialogs.SetPriceDialog;
import de.wichura.lks.mainactivity.Constants;

import static de.wichura.lks.mainactivity.Constants.SHARED_PREFS_USER_INFO;

/**
 * Created by Bernd Wichura on 05.04.2016.
 * Luftkraftsport
 */

public class SearchActivity extends AppCompatActivity implements
        SetPriceDialog.OnCompleteListener {

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

    @Override
    public void onPriceRangeComplete(String priceFrom, String priceTo) {
        storePriceRange(priceFrom, priceTo );
        Fragment f = getCurrentFragment();
        ((SearchFragment) f).adaptLayoutForPrice(priceFrom, priceTo);
    }

    private Fragment getCurrentFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        int stackCount = fragmentManager.getBackStackEntryCount();
        if (fragmentManager.getFragments() != null)
            return fragmentManager.getFragments().get(stackCount > 0 ? stackCount - 1 : stackCount);
        else return null;
    }

    private void storePriceRange(String from, String to) {

        SharedPreferences sp = getSharedPreferences(Constants.USER_PRICE_RANGE, MODE_PRIVATE);
        SharedPreferences.Editor ed = sp.edit();
        ed.putString(Constants.PRICE_FROM, from);
        ed.putString(Constants.PRICE_TO, to);
        ed.apply();
    }

    public String getUserToken() {
        return getSharedPreferences(SHARED_PREFS_USER_INFO, 0).getString(Constants.USER_TOKEN, "");
    }
}
