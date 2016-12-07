package wichura.de.camperapp.presentation;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.GraphRequest;
import com.facebook.Profile;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import wichura.de.camperapp.http.Service;
import wichura.de.camperapp.mainactivity.Constants;
import wichura.de.camperapp.mainactivity.MainActivity;
import wichura.de.camperapp.models.AdsAndBookmarks;
import wichura.de.camperapp.models.AdsAsPage;

import static wichura.de.camperapp.mainactivity.Constants.SHARED_PREFS_USER_INFO;

/**
 * Created by ich on 20.10.2016.
 * CamperApp
 */

public class MainPresenter {

    private MainActivity view;
    private Service service;
    private Subscription subscription;
    private Context context;

    public MainPresenter(MainActivity view, Service service, Context context) {
        this.view = view;
        this.service = service;
        this.context = context;
    }

    public void createUser(String name, String userToken) {
        service.createUserObserv(name, userToken)
                .subscribeOn(Schedulers.newThread())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<String>() {
                    @Override
                    public void onCompleted() {
                        Log.d("CONAN", "created new user");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d("CONAN", "error in creating user: " + e.getMessage());
                    }

                    @Override
                    public void onNext(String result) {
                        Log.d("CONAN", "create user: " + result);
                    }
                });
    }

    public void getFacebookUserInfo() {
        CallbackManager.Factory.create();
        GraphRequest request = GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(), (object, response) -> {
            JSONObject json = response.getJSONObject();
            try {
                if (json != null) {
                    Log.d("CONAN", "Facebook userId : " + json.getString("id"));
                    Log.d("CONAN", "user name facebook: " + json.getString("name"));
                    //user Token
                    AccessToken userToken = AccessToken.getCurrentAccessToken();
                    String token = "";
                    if (userToken != null) token = userToken.getToken();
                    Log.d("CONAN", "Facebook user token: " + token);

                    view.setProfileName(json.getString("name"));
                    Profile profile = Profile.getCurrentProfile();
                    if (profile != null) {
                        Uri uri = profile.getProfilePictureUri(200, 200);
                        view.setProfilePicture(uri);
                    }
                    setUserPreferences(json.getString("name"), json.getString("id"), token);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Log.d("CONAN", "Facebook AccessToken or Facebook response not valid");
            }
        });

        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,link,picture");
        request.setParameters(parameters);
        request.executeAsync();
    }

    public void loadAdDataPage(int page, int size, String type) {
        if (Constants.TYPE_BOOKMARK.equals(type)) {
            loadBookmarkedAds(page, size, type);
        } else if (Constants.TYPE_ALL.equals(type)) {
            loadAllAd(page, size, type);
        } else if (Constants.TYPE_USER.equals(type)) {
            getAdsForUser(page, size, type, getUserToken());
        }
    }

    private void loadBookmarkedAds(int page, int size, String type) {
        if (page == 0) {
            if (view.listView != null) {
                view.listView.setVisibility(View.INVISIBLE);
            }
        }
        view.progressBar.setVisibility(ProgressBar.VISIBLE);
        //Log.d("CONAN", url);

        Observable<String[]> getBookmarksObserv = service.getBookmarksForUserObserv(getUserToken());
        Observable<AdsAsPage> getBookmarkedAdsObserv = service.getMyBookmarkedAdsObserv(page, size, getUserToken());

        Observable<AdsAndBookmarks> zippedReqForBookmarksAndAds =
                Observable.zip(getBookmarksObserv, getBookmarkedAdsObserv, (bookmarks, ads) ->
                {
                    ArrayList<String> bm = new ArrayList<>(Arrays.asList(bookmarks));
                    AdsAndBookmarks elements = new AdsAndBookmarks();
                    elements.setAds(ads);
                    elements.setBookmarks(bm);
                    return elements;
                });

        subscription = zippedReqForBookmarksAndAds
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<AdsAndBookmarks>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d("CONAN", "Error in getting bookmarked ads: " + e.toString());
                        view.showEmptyView();
                    }

                    @Override
                    public void onNext(AdsAndBookmarks element) {
                        view.progressBar.setVisibility(ProgressBar.GONE);
                        if (page == 0) {
                            if (view.listView != null) {
                                view.listView.setVisibility(View.VISIBLE);
                            }
                            view.hideEmptyView();
                            view.updateAds(element, type);
                        } else {
                            view.addMoreAdsToList(element);
                        }
                    }
                });
    }

    private void loadAllAd(int page, int size, String type) {
        if (page == 0) {
            if (view.listView != null) {
                view.listView.setVisibility(View.INVISIBLE);
            }
        }
        view.progressBar.setVisibility(ProgressBar.VISIBLE);
        //Log.d("CONAN", url);

        //TODO:erstes noch uneingeloged -> 500 weil kein userToken bei getBookmarks!!
        Observable<String[]> getBookmarksObserv = service.getBookmarksForUserObserv(getUserToken());
        Observable<AdsAsPage> getAllAdsForUserObserv = service.getAllAdsObserv(page, size);

        Observable<AdsAndBookmarks> zippedReqForBookmarksAndAds =
                Observable.zip(getBookmarksObserv, getAllAdsForUserObserv, (bookmarks, ads) ->
                {
                    ArrayList<String> bm = new ArrayList<>(Arrays.asList(bookmarks));
                    AdsAndBookmarks elements = new AdsAndBookmarks();
                    elements.setAds(ads);
                    elements.setBookmarks(bm);
                    return elements;
                });

        subscription = zippedReqForBookmarksAndAds
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<AdsAndBookmarks>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d("CONAN", "Error in getting all ads: " + e.toString());
                        view.showEmptyView();
                    }

                    @Override
                    public void onNext(AdsAndBookmarks element) {
                        view.progressBar.setVisibility(ProgressBar.GONE);
                        if (page == 0) {
                            if (view.listView != null) {
                                view.listView.setVisibility(View.VISIBLE);
                            }
                            view.hideEmptyView();
                            view.updateAds(element, type);
                        } else {
                            view.addMoreAdsToList(element);
                        }
                    }
                });
    }

    private void getAdsForUser(int page, int size, String type, String token) {
        if (page == 0) {
            if (view.listView != null) {
                view.listView.setVisibility(View.INVISIBLE);
            }
        }
        view.progressBar.setVisibility(ProgressBar.VISIBLE);

        //use user Token here
        Observable<String[]> getBookmarksObserv = service.getBookmarksForUserObserv(token);
        Observable<AdsAsPage> getAllAdsForUserObserv = service.getAdsMyObserv(page, size, token);

        Observable<AdsAndBookmarks> zippedReqForBookmarksAndAds =
                Observable.zip(getBookmarksObserv, getAllAdsForUserObserv, (bookmarks, ads) ->
                {
                    ArrayList<String> bm = new ArrayList<>(Arrays.asList(bookmarks));
                    AdsAndBookmarks elements = new AdsAndBookmarks();
                    elements.setAds(ads);
                    elements.setBookmarks(bm);
                    return elements;
                });

        subscription = zippedReqForBookmarksAndAds
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<AdsAndBookmarks>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d("CONAN", "Error in getting user's ads: " + e.toString());
                        view.showEmptyView();
                    }

                    @Override
                    public void onNext(AdsAndBookmarks element) {
                        view.progressBar.setVisibility(ProgressBar.GONE);
                        if (page == 0) {
                            if (view.listView != null) {
                                view.listView.setVisibility(View.VISIBLE);
                            }
                            view.hideEmptyView();
                            view.updateAds(element, type);
                        } else {
                            view.addMoreAdsToList(element);
                        }
                    }
                });
    }

    public void rxUnSubscribe() {
        if (subscription != null && !subscription.isUnsubscribed())
            subscription.unsubscribe();
    }

    private void setUserPreferences(String name, String userId, String userToken) {
        SharedPreferences settings = context.getSharedPreferences(SHARED_PREFS_USER_INFO, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(Constants.USER_NAME, name);
        editor.putString(Constants.USER_ID, userId);
        editor.putString(Constants.USER_TOKEN, userToken);
        editor.apply();
    }

    private String getUserToken() {
        return context.getSharedPreferences(SHARED_PREFS_USER_INFO, 0).getString(Constants.USER_TOKEN, "");
    }
}
