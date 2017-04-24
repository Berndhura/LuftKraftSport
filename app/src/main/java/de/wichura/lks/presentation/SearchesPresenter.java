package de.wichura.lks.presentation;

import android.content.Context;
import android.util.Log;

import java.util.List;

import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import de.wichura.lks.activity.SearchesActivity;
import de.wichura.lks.http.Service;
import de.wichura.lks.models.SearchItem;
import de.wichura.lks.util.Utility;

/**
 * Created by Bernd Wichura on 07.02.2017.
 * Luftkraftsport
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
        this.utils = new Utility(searchesActivity.getActivity());
    }

    public void loadSearchesForUser() {
        view.enableProgressBar();
        service.findSearchesObserv(utils.getUserToken())
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<List<SearchItem>>() {
                    @Override
                    public void onCompleted() {
                        // view.disableProgress();
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
}
