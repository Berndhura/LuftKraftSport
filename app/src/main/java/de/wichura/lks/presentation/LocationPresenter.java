package de.wichura.lks.presentation;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import de.wichura.lks.activity.LocationFragment;
import de.wichura.lks.activity.SearchActivity;
import de.wichura.lks.http.GoogleService;
import de.wichura.lks.mainactivity.Constants;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by ich on 18.02.2017.
 * Luftkrafsport
 */

public class LocationPresenter {

    private Context context;
    private GoogleService googleService;
    private Subscription subscription;
    private SearchActivity view;

    public LocationPresenter(Context applicationContext, SearchActivity view) {
        this.context = applicationContext;
        this.googleService = new GoogleService();
        this.view = view;
    }

    public void saveUsersLocation(Double lat, Double lng) {

        Observable<JsonObject> getCityNameFromLatLng = googleService.getCityNameFrimLatLngObserv(lat, lng, false);

        subscription = getCityNameFromLatLng
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<JsonObject>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d("CONAN", "LocationPresenter: error in getting city name from google maps api: " + e.toString());
                    }

                    @Override
                    public void onNext(JsonObject location) {
                        JsonElement city = location.get("results").getAsJsonArray()
                                .get(0).getAsJsonObject().get("address_components").getAsJsonArray()
                                .get(2).getAsJsonObject().get("long_name");

                        Log.d("CONAN", "city name from google maps api: " + city);
                        storeCityName(lat, lng, city.getAsString());
                    }
                });
    }

    private void storeCityName(Double lat, Double lng, String location) {

        SharedPreferences sp = context.getSharedPreferences(Constants.USERS_LOCATION, MODE_PRIVATE);
        SharedPreferences.Editor ed = sp.edit();
        ed.putLong(Constants.LAT, Double.doubleToRawLongBits(lat));
        ed.putLong(Constants.LNG, Double.doubleToRawLongBits(lng));
        ed.putString(Constants.LOCATION, location);
        ed.apply();

        Fragment f = getCurrentFragment();
        ((LocationFragment) f).updateCity(location);
    }

    private Fragment getCurrentFragment() {
        FragmentManager fragmentManager = view.getSupportFragmentManager();
        int stackCount = fragmentManager.getBackStackEntryCount();
        if (fragmentManager.getFragments() != null)
            return fragmentManager.getFragments().get(stackCount > 0 ? stackCount - 1 : stackCount);
        else return null;
    }
}
