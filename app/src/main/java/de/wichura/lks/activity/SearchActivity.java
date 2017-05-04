package de.wichura.lks.activity;


import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnTabSelectListener;

import java.io.IOException;
import java.util.List;

import de.wichura.lks.R;
import de.wichura.lks.dialogs.SetPriceDialog;
import de.wichura.lks.dialogs.ZipDialogFragment;
import de.wichura.lks.http.GoogleService;
import de.wichura.lks.mainactivity.Constants;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

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
    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case Constants.REQUEST_ID_FOR_LOCATION_PERMISSION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Toast.makeText(this, "Granted", Toast.LENGTH_LONG).show();
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    //TODO mache google map auf, ergebnis merken?!
                    android.support.v4.app.FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                    fragmentTransaction.replace(R.id.layout, new LocationFragment());
                    fragmentTransaction.commit();

                } else {
                    //TODO from api level 23 nur wie mache ich das mit den alten versionen?

                    boolean showRationale = shouldShowRequestPermissionRationale( permissions[0] );
                    new ZipDialogFragment().show(getSupportFragmentManager(), null);

                    Toast.makeText(this, "nix Granted", Toast.LENGTH_LONG).show();
                }
            }
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
        getLatLngFromPlz(zipCode);
    }

    public void getLatLngFromPlz(String zip) {
        final Geocoder geocoder = new Geocoder(this);
        try {
            List<Address> addresses = geocoder.getFromLocationName(zip, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);

                //TODO lat lng sind nicht gspeichert, nur für den namen benutzt
                //store lat lng for article
                double lat = address.getLatitude();
                double lng = address.getLongitude();

                //show city name
                getCityNameFromLatLng(address.getLatitude(), address.getLongitude());
            } else {
                // Display appropriate message when Geocoder services are not available
                Toast.makeText(getApplicationContext(), "Hat leider nicht geklappt mit deiner PLZ, versuche nochmal!", Toast.LENGTH_LONG).show();
            }
        } catch (IOException e) {
            // handle exception
        }
    }

    private void getCityNameFromLatLng(Double lat, Double lng) {

        GoogleService googleService = new GoogleService();

        Observable<JsonObject> getCityNameFromLatLng = googleService.getCityNameFromLatLngObserable(lat, lng, false);

        getCityNameFromLatLng
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<JsonObject>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d("CONAN", "serach anctivity: error in getting city name from google maps api: " + e.toString());
                    }

                    @Override
                    public void onNext(JsonObject location) {
                        JsonElement city = location.get("results").getAsJsonArray()
                                .get(0).getAsJsonObject().get("address_components").getAsJsonArray()
                                .get(2).getAsJsonObject().get("long_name");

                        Log.d("CONAN", "city name from google maps api: " + city);
                        //set location name in searchFragment and store lat lng in shared prefs
                        storeLocation(lat, lng, city.getAsString());
                    }
                });
    }

    private void storeLocation(Double lat, Double lng, String location) {

        SharedPreferences sp = getSharedPreferences(Constants.USERS_LOCATION, MODE_PRIVATE);
        SharedPreferences.Editor ed = sp.edit();
        ed.putLong(Constants.LAT, Double.doubleToRawLongBits(lat));
        ed.putLong(Constants.LNG, Double.doubleToRawLongBits(lng));
        ed.putString(Constants.LOCATION, location);
        ed.apply();

        ((TextView) getCurrentFragment().getView().findViewById(R.id.search_location_zip_and_location)).setText(location);

        //TODO initDistanceSeekBar -> fehlt hier noch

        //Fragment f = getCurrentFragment();
        //((LocationFragment) f).updateCity(location);
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
