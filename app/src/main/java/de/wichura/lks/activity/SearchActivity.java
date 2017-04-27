package de.wichura.lks.activity;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnTabSelectListener;

import de.wichura.lks.R;
import de.wichura.lks.dialogs.SetPriceDialog;
import de.wichura.lks.dialogs.ZipDialogFragment;
import de.wichura.lks.mainactivity.Constants;

import static de.wichura.lks.mainactivity.Constants.SHARED_PREFS_USER_INFO;

/**
 * Created by Bernd Wichura on 05.04.2016.
 * Luftkraftsport
 */

public class SearchActivity extends AppCompatActivity implements
        SetPriceDialog.OnCompleteListener,
        ZipDialogFragment.OnCompleteListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.basic_search_layout);

        RelativeLayout fraglayout = (RelativeLayout) findViewById(R.id.rl_Containe);

        BottomBar bottomBar = (BottomBar) findViewById(R.id.bottomBar);
        bottomBar.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelected(@IdRes int tabId) {
                if (tabId == R.id.tab_list_saved_searches) {
                    if ("".equals(getUserToken())) {
                        //TODO softpad hier schließen -> liegt aber im fragment!!! wie
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

        //TODO messen der bottombar und anpassen der layouts!!!
        //TODO http://stackoverflow.com/questions/4936553/android-how-can-you-align-a-button-at-the-bottom-and-listview-above
        bottomBar.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                // viewToMeasure is now measured and laid out, and displayed dimensions are known.
                logComputedViewDimensions(bottomBar.getWidth(), bottomBar.getHeight());

                // Remove this listener, as we have now successfully calculated the desired dimensions.
                bottomBar.getViewTreeObserver().removeOnPreDrawListener(this);

                // Always return true to continue drawing.
                return true;
            }
        });
    }

    private void logComputedViewDimensions(final int width, final int height) {
        Log.d("CONAN", "viewToMeasure has width " + width);
        Log.d("CONAN", "viewToMeasure has height " + height);
    }

    //permission request from location fragment
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 666: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Toast.makeText(this, "Granted", Toast.LENGTH_LONG).show();
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {
                    new ZipDialogFragment().show(getSupportFragmentManager(), null);

                    Toast.makeText(this, "nix Granted", Toast.LENGTH_LONG).show();
                }
                Toast.makeText(this, "in666", Toast.LENGTH_LONG).show();
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @Override
    public void onPriceRangeComplete(String priceFrom, String priceTo) {
        storePriceRange(priceFrom, priceTo);
        Fragment f = getCurrentFragment();
        if (getString(R.string.price_does_not_matter).equals(priceFrom) && getString(R.string.price_does_not_matter).equals(priceFrom)) {
            ((SearchFragment) f).adaptLayoutForPrice(priceFrom, priceTo);
        } else {
            ((SearchFragment) f).adaptLayoutForPrice(priceFrom, priceTo);
        }
    }

    @Override
    public void onZipCodeComplete(String zipCode) {
        Log.d("CONAN", "Zipcode from dialog: " + zipCode);
       // getLatLngFromPlz(zipCode);
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
