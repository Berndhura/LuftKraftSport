package de.wichura.lks.presentation;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.wichura.lks.activity.LocationFragment;
import de.wichura.lks.activity.SearchActivity;
import de.wichura.lks.http.GoogleService;
import de.wichura.lks.mainactivity.Constants;


import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;


import static android.content.Context.MODE_PRIVATE;

/**
 * Created by Bernd Wichura on 18.02.2017.
 * Luftkrafsport
 */

public class LocationPresenter {

    private Context context;
    private GoogleService googleService;
    private SearchActivity view;

    public LocationPresenter(Context applicationContext, SearchActivity view) {
        this.context = applicationContext;
        this.googleService = new GoogleService();
        this.view = view;
    }

    public void saveUsersLocation(Double lat, Double lng) {

        Observable<JsonObject> getCityNameFromLatLng = googleService.getCityNameFromLatLngObserable(lat, lng);

        getCityNameFromLatLng
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<JsonObject>() {
                    @Override
                    public void onComplete() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d("CONAN", "LocationPresenter: error in getting city name from google maps api: " + e.toString());
                    }

                    @Override
                    public void onNext(JsonObject location) {
                        try {
                            JSONObject json = new JSONObject(location.toString());
                            parseForLocality(json, lat, lng);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onSubscribe(Disposable d) {

                    }
                });
    }

    private void parseForLocality(JSONObject json, Double lat, Double lng) {
        try {
            JSONArray array = json.getJSONArray("results");
            if (array.length() > 0) {

                JSONObject obj = array.getJSONObject(0);
                JSONArray addrComp = obj.getJSONArray("address_components");
                int i=0;
                while (i < addrComp.length()) {
                    JSONArray types=  addrComp.getJSONObject(i).getJSONArray("types");
                    if (types.getString(0).equals("locality")) {
                        String city = addrComp.getJSONObject(i).getString("short_name");
                        Log.d("CONAN", city);
                        storeCityName(lat, lng, city);
                        return;
                    } else {
                        i++;
                    }
                }
            }
        } catch (JSONException e) {
            Log.e("CONAN", e.toString());
        }
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
