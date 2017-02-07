package wichura.de.camperapp.presentation;

import android.content.Context;
import android.util.Log;

import java.util.List;

import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import wichura.de.camperapp.activity.SearchesActivity;
import wichura.de.camperapp.http.Service;
import wichura.de.camperapp.models.SearchItem;
import wichura.de.camperapp.util.Utility;

/**
 * Created by bwichura on 07.02.2017.
 *
 */

public class SearchesPresenter {

    private Service service;
    private Context context;
    private SearchesActivity view;
    private Subscription subscription;
    private Utility utils;

    public SearchesPresenter(SearchesActivity searchesActivity, Service service, Context applicationContext) {
        this.service = service;
        this.context = applicationContext;
        this.view = searchesActivity;
        this.utils = new Utility(searchesActivity);
    }

    public void loadSearchesForUser() {
        view.enableProgress();
        service.findSearchesObserv(utils.getUserToken())
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<List<SearchItem>>() {
                    @Override
                    public void onCompleted() {
                        view.disableProgress();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d("CONAN", "error loading searches: " + e.getMessage());
                    }

                    @Override
                    public void onNext(List<SearchItem> searchItem) {
                        view.updateSearches(searchItem);
                    }
                });
    }
}
