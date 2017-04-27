package de.wichura.lks.presentation;

import android.content.Context;
import android.util.Log;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import de.wichura.lks.activity.NewAdActivity;
import de.wichura.lks.http.GoogleService;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by ich on 31.03.2017.
 * Luftkraftsport
 */

public class NewArticlePresenter {

    private Context context;
    private GoogleService googleService;
    private NewAdActivity view;

    public NewArticlePresenter(Context applicationContext, NewAdActivity view) {
        this.context = applicationContext;
        this.googleService = new GoogleService();
        this.view = view;
    }

    public void getCityNameFromLatLng(Double lat, Double lng) {

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
                        Log.d("CONAN", "new Article Presenter: error in getting city name from google maps api: " + e.toString());
                    }

                    @Override
                    public void onNext(JsonObject location) {
                        JsonElement city = location.get("results").getAsJsonArray()
                                .get(0).getAsJsonObject().get("address_components").getAsJsonArray()
                                .get(2).getAsJsonObject().get("long_name");

                        Log.d("CONAN", "city name from google maps api: " + city);
                        view.setCityName(city.getAsString());
                    }
                });
    }
}
