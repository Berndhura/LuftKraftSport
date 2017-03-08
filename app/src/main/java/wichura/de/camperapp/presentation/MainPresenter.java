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
    private Context context;
    public Subscription subscription;

    public MainPresenter(MainActivity view, Service service, Context context) {
        this.view = view;
        this.service = service;
        this.context = context;
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

    public void searchForArticles(int page, int size, Integer priceFrom, Integer priceTo, int distance, String description) {
        if (page == 0) {
            if (view.listView != null) {
                view.listView.setVisibility(View.INVISIBLE);
            }
        }
        view.progressBar.setVisibility(ProgressBar.VISIBLE);

        if (!getUserToken().equals("")) {
            Observable<Long[]> getBookmarksObserv = service.getBookmarksForUserObserv(getUserToken());
            Observable<AdsAsPage> searchForAdsObserv = service.findAdsObserv(description, getLat(), getLng(), distance, priceFrom, priceTo, page, size);

            Observable<AdsAndBookmarks> zippedReqForBookmarksAndAds =
                    Observable.zip(getBookmarksObserv, searchForAdsObserv, (bookmarks, ads) ->
                    {
                        ArrayList<Long> bm = new ArrayList<>(Arrays.asList(bookmarks));
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
                                view.updateAds(element, null, priceFrom, priceTo, distance, description);
                            } else {
                                view.addMoreAdsToList(element);
                            }
                        }
                    });
        } else {
            Observable<AdsAsPage> searchForAdsObserv = service.findAdsObserv(description, getLat(), getLng(), distance, priceFrom, priceTo, page, size);

            subscription = searchForAdsObserv
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Subscriber<AdsAsPage>() {
                        @Override
                        public void onCompleted() {

                        }

                        @Override
                        public void onError(Throwable e) {
                            Log.d("CONAN", "Error in getting all ads: " + e.toString());
                        }

                        @Override
                        public void onNext(AdsAsPage adsAsPage) {
                            view.progressBar.setVisibility(ProgressBar.GONE);
                            AdsAndBookmarks adsAndBookmarks = new AdsAndBookmarks();
                            adsAndBookmarks.setAds(adsAsPage);
                            //no user -> empty bookmarklist
                            adsAndBookmarks.setBookmarks(new ArrayList<>());
                            if (page == 0) {
                                if (view.listView != null) {
                                    view.listView.setVisibility(View.VISIBLE);
                                }
                                view.hideEmptyView();
                                view.updateAds(adsAndBookmarks, null, priceFrom, priceTo, distance, description);
                            } else {
                                view.addMoreAdsToList(adsAndBookmarks);
                            }
                        }
                    });
        }
    }

    private void loadBookmarkedAds(int page, int size, String type) {
        if (page == 0) {
            if (view.listView != null) {
                view.listView.setVisibility(View.INVISIBLE);
            }
        }

        Double lat = getLat();
        Double lng = getLng();

        view.progressBar.setVisibility(ProgressBar.VISIBLE);

        Observable<Long[]> getBookmarksObserv = service.getBookmarksForUserObserv(getUserToken());
        Observable<AdsAsPage> getBookmarkedAdsObserv = service.getMyBookmarkedAdsObserv(lat, lng, page, size, getUserToken());

        Observable<AdsAndBookmarks> zippedReqForBookmarksAndAds =
                Observable.zip(getBookmarksObserv, getBookmarkedAdsObserv, (bookmarks, ads) ->
                {
                    ArrayList<Long> bm = new ArrayList<>(Arrays.asList(bookmarks));
                    AdsAndBookmarks elements = new AdsAndBookmarks();
                    elements.setAds(ads);
                    elements.setBookmarks(bm);
                    return elements;
                });

        zippedReqForBookmarksAndAds
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
                            view.updateAds(element, type, null, null, null, null);
                        } else {
                            view.addMoreAdsToList(element);
                        }
                    }
                });
    }

    private void loadAllAd(int page, int size, String type) {
        Double lat = getLat();
        Double lng = getLng();

        if (page == 0) {
            if (view.listView != null) {
                view.listView.setVisibility(View.INVISIBLE);
            }
        }
        view.progressBar.setVisibility(ProgressBar.VISIBLE);

        if (!getUserToken().equals("")) {
            Observable<Long[]> getBookmarksObserv = service.getBookmarksForUserObserv(getUserToken());
            Observable<AdsAsPage> getAllAdsForUserObserv = service.getAllAdsObserv(lat, lng, page, size);

            Observable<AdsAndBookmarks> zippedReqForBookmarksAndAds =
                    Observable.zip(getBookmarksObserv, getAllAdsForUserObserv, (bookmarks, ads) ->
                    {
                        ArrayList<Long> bm = new ArrayList<>(Arrays.asList(bookmarks));
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
                                view.updateAds(element, type, null, null, null, null);
                            } else {
                                view.addMoreAdsToList(element);
                            }
                        }
                    });
        } else {
            Observable<AdsAsPage> getAllAdsForUserObserv = service.getAllAdsObserv(lat, lng, page, size);

            subscription = getAllAdsForUserObserv
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Subscriber<AdsAsPage>() {
                        @Override
                        public void onCompleted() {

                        }

                        @Override
                        public void onError(Throwable e) {
                            Log.d("CONAN", "Error in getting all ads: " + e.toString());
                        }

                        @Override
                        public void onNext(AdsAsPage adsAsPage) {
                            view.progressBar.setVisibility(ProgressBar.GONE);
                            AdsAndBookmarks adsAndBookmarks = new AdsAndBookmarks();
                            adsAndBookmarks.setAds(adsAsPage);
                            //no user -> empty bookmarklist
                            adsAndBookmarks.setBookmarks(new ArrayList<>());
                            if (page == 0) {
                                if (view.listView != null) {
                                    view.listView.setVisibility(View.VISIBLE);
                                }
                                view.hideEmptyView();
                                view.updateAds(adsAndBookmarks, type, null, null, null, null);
                            } else {
                                view.addMoreAdsToList(adsAndBookmarks);
                            }
                        }
                    });
        }
    }

    private void getAdsForUser(int page, int size, String type, String token) {
        if (page == 0) {
            if (view.listView != null) {
                view.listView.setVisibility(View.INVISIBLE);
            }
        }
        view.progressBar.setVisibility(ProgressBar.VISIBLE);

        //use user Token here
        Observable<Long[]> getBookmarksObserv = service.getBookmarksForUserObserv(token);
        Observable<AdsAsPage> getAllAdsForUserObserv = service.getAdsMyObserv(page, size, token);

        Observable<AdsAndBookmarks> zippedReqForBookmarksAndAds =
                Observable.zip(getBookmarksObserv, getAllAdsForUserObserv, (bookmarks, ads) ->
                {
                    ArrayList<Long> bm = new ArrayList<>(Arrays.asList(bookmarks));
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
                            view.updateAds(element, type, null, null, null, null);
                        } else {
                            view.addMoreAdsToList(element);
                        }
                    }
                });
    }

    private void setUserPreferences(String name, String userId, String userToken) {
        SharedPreferences settings = context.getSharedPreferences(SHARED_PREFS_USER_INFO, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(Constants.USER_NAME, name);
        editor.putString(Constants.USER_ID, userId);
        editor.putString(Constants.USER_TOKEN, userToken);
        editor.apply();
    }

    public Double getLng() {
        SharedPreferences settings = context.getSharedPreferences(Constants.USERS_LOCATION, 0);
        return Double.longBitsToDouble(settings.getLong(Constants.LNG, 0));
    }

    public Double getLat() {
        SharedPreferences settings = context.getSharedPreferences(Constants.USERS_LOCATION, 0);
        return  Double.longBitsToDouble(settings.getLong(Constants.LAT, 0));
    }

    private String getUserToken() {
        return context.getSharedPreferences(SHARED_PREFS_USER_INFO, 0).getString(Constants.USER_TOKEN, "");
    }

    public void sendUserPicToServer(String userProfilePic, String userId) {

        service.saveUserPictureObserv(userProfilePic, userId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<String>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d("CONAN", "error in sending pictureUri to server: " + e.getMessage());
                    }

                    @Override
                    public void onNext(String s) {
                        Log.d("CONAN", "pictureUri send: " + s);
                    }
                });
    }
}
