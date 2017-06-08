package de.wichura.lks.presentation;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.api.OptionalPendingResult;

import java.util.List;

import de.wichura.lks.activity.SearchesActivity;
import de.wichura.lks.http.GoogleService;
import de.wichura.lks.http.Service;
import de.wichura.lks.models.SearchItem;
import de.wichura.lks.util.Utility;
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
    private SearchesActivity activity;
    private Subscription subscription;
    private Utility utils;

    public SearchesPresenter(SearchesActivity searchesActivity, Service service, Context applicationContext) {
        this.service = service;
        this.context = applicationContext;
        this.activity = searchesActivity;
        this.utils = new Utility(searchesActivity.getActivity());
        this.googleService = new GoogleService();
    }

    public void loadSearchesForUser() {
        activity.enableProgressBar();
        service.findSearchesObserv(utils.getUserToken())
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<List<SearchItem>>() {
                    @Override
                    public void onCompleted() {
                        activity.disableProgressbar();
                    }

                    @Override
                    public void onError(Throwable e) {
                        if ("HTTP 401 Unauthorized".equals(e.getMessage())) {
                            //refresh userToken
                            refreshUserIdToken();
                        }
                        Log.d("CONAN", "error loading saved searches: " + e.getMessage());
                        activity.disableProgressbar();
                        activity.showProblem();
                    }

                    @Override
                    public void onNext(List<SearchItem> searchItem) {
                        if (searchItem.size() == 0) {
                            activity.emptyPage();
                        } else {
                            activity.updateSearches(searchItem);
                        }
                    }
                });
    }

    private void refreshUserIdToken() {

        OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(activity.getGoogleApiClient());
        if (opr.isDone()) {
            Log.d("CONAN", "Got cached sign-in in follow search!");
            GoogleSignInResult result = opr.get();
            handleSignInResult(result);
        } else {
            Log.d("CONAN", "cache sign-in leer, get user token from google in follow search!");
            opr.setResultCallback(googleSignInResult -> handleSignInResult(googleSignInResult));
        }
    }

    private void handleSignInResult(GoogleSignInResult result) {
        Log.d("CONAN", "handleSignInResult in follow search: " + result.isSuccess());
        if (result.isSuccess()) {
            GoogleSignInAccount acct = result.getSignInAccount();
            Log.d("CONAN", "neues Token von Google: " + acct.getIdToken());
            if (acct.getIdToken() != null) {
                utils.setUserPreferences(null, null, acct.getIdToken());
                //TODO request data again -> todesschleife?
                loadSearchesForUser();
            }
        } else {
            Log.d("CONAN", "handleSignIn:  result ist nicht success!!!");
        }
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
                                activity.updateSearches(searchItem);
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