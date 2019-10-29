package de.wichura.lks.presentation;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import de.wichura.lks.activity.NewAdActivity;
import de.wichura.lks.http.GoogleService;
import de.wichura.lks.util.SharedPrefsHelper;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by ich on 31.03.2017.
 * Luftkraftsport
 */

public class NewArticlePresenter {

    private Context context;
    private GoogleService googleService;
    private NewAdActivity view;
    private SharedPrefsHelper sharedPrefsHelper;

    public NewArticlePresenter(Context applicationContext, NewAdActivity view) {
        this.context = applicationContext;
        this.googleService = new GoogleService();
        this.view = view;
        this.sharedPrefsHelper = new SharedPrefsHelper(view);
    }

    public void getLatLngFromAddress(String address) {
        googleService.getLatLngFromAddressObservable(address)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<JsonObject>() {
                    @Override
                    public void onComplete() { }

                    @Override
                    public void onError(Throwable e) {
                        Log.d("CONAN", "on Error in getLatLngFromAddress " + e.getLocalizedMessage() );
                    }

                    @Override
                    public void onNext(JsonObject location) {

                        if (location.get("error_message") != null) {
                            Log.d("CONAN", "problem with google maps api Geocoding: " + location.get("status"));
                            Toast.makeText(context, "Problem with google maps", Toast.LENGTH_SHORT).show();
                            //todo what if google map api does not work? default location

                        } else {

                            JsonElement city = location.get("results").getAsJsonArray()
                                    .get(0).getAsJsonObject().get("address_components").getAsJsonArray()
                                    .get(0).getAsJsonObject().get("long_name");

                            Double lat = location.get("results").getAsJsonArray().get(0).getAsJsonObject().get("geometry").getAsJsonObject().get("location").getAsJsonObject().get("lat").getAsDouble();
                            Double lng = location.get("results").getAsJsonArray().get(0).getAsJsonObject().get("geometry").getAsJsonObject().get("location").getAsJsonObject().get("lng").getAsDouble();

                            sharedPrefsHelper.setLastLocationName(city.getAsString());
                            sharedPrefsHelper.setLastLocationCoordinates(lat, lng);

                            Log.d("CONAN", "city name from google maps api: " + city + lat + " " + lng);
                            view.setCityName(city.getAsString());
                            view.isLocationSet = true;
                        }
                    }

                    @Override
                    public void onSubscribe(Disposable d) { }
                });
    }
}
