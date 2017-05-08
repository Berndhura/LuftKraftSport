package de.wichura.lks.presentation;

import android.content.Context;
import android.util.Log;

import com.google.gson.JsonObject;

import org.json.JSONObject;

import java.util.List;

import de.wichura.lks.activity.SearchesActivity;
import de.wichura.lks.http.GoogleService;
import de.wichura.lks.http.Service;
import de.wichura.lks.models.SearchItem;
import de.wichura.lks.util.Utility;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Bernd Wichura on 07.02.2017.
 * Luftkraftsport
 */

public class SearchesPresenter {

    private Service service;
    private GoogleService googleService;
    private Context context;
    private SearchesActivity view;
    private Subscription subscription;
    private Utility utils;

    public SearchesPresenter(SearchesActivity searchesActivity, Service service, Context applicationContext) {
        this.service = service;
        this.context = applicationContext;
        this.view = searchesActivity;
        this.utils = new Utility(searchesActivity.getActivity());
        this.googleService = new GoogleService();
    }

    public void loadSearchesForUser() {
        view.enableProgressBar();
        service.findSearchesObserv(utils.getUserToken())
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<List<SearchItem>>() {
                    @Override
                    public void onCompleted() {
                        view.disableProgressbar();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d("CONAN", "error loading saved searches: " + e.getMessage());
                        view.disableProgressbar();
                        view.showProblem();
                    }

                    @Override
                    public void onNext(List<SearchItem> searchItem) {
                        view.updateSearches(searchItem);
                    }
                });
    }

    /*private void getLocationNames(List<SearchItem> searchItem) {

        Observable.from(searchItem)
                .flatMap(item -> googleService.getCityNameFromLatLngObserable(item.getLat(), item.getLng(), null)
                        .map(jsonObject -> createWithLocation(item, getLocation(jsonObject)))
                        .subscribeOn(Schedulers.io()))
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Subscriber<JsonObject>() {
                            @Override
                            public void onCompleted() {
                                view.updateSearches(searchItem);
                            }

                            @Override
                            public void onError(Throwable e) {

                            }

                            @Override
                            public void onNext(SearchItem item) {

                            }
                        });
    }

    private void createWithLocation(SearchItem item, String loc) {

    }

    private String getLocation(JsonObject response) {
        if (response.get("results").getAsJsonArray().size() > 0) {
            String loc = response.get("results").getAsJsonArray()
                    .get(0).getAsJsonObject().get("address_components").getAsJsonArray()
                    .get(2).getAsJsonObject().get("long_name").toString();
            Log.d("CONAN", "City: " + loc);
            return loc;
        }
        return "nix!";
    }*/
}