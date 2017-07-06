package de.wichura.lks.presentation;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.GraphRequest;
import com.facebook.Profile;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.location.LocationServices;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

import de.wichura.lks.http.Service;
import de.wichura.lks.mainactivity.Constants;
import de.wichura.lks.mainactivity.MainActivity;
import de.wichura.lks.models.AdsAndBookmarks;
import de.wichura.lks.models.AdsAsPage;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static android.content.Context.MODE_PRIVATE;
import static de.wichura.lks.mainactivity.Constants.SHARED_PREFS_USER_INFO;
import static de.wichura.lks.mainactivity.Constants.TYPE_ALL;

/**
 * Created by Bernd Wichura on 20.10.2016.
 * LuftKraftSport
 */

public class MainPresenter {

    private MainActivity activity;
    private Service service;
    private Context context;
    public Subscription subscription;

    public MainPresenter(MainActivity activity, Service service, Context context) {
        this.activity = activity;
        this.service = service;
        this.context = context;
    }

    public void deleteBookmark(Integer adId) {
        service.delBookmarkAdObserv(adId, getUserToken())
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<String>() {
                    @Override
                    public void onCompleted() {
                        Toast.makeText(context, "Von den Favoriten gelöscht!", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d("CONAN", "error in bookmark ad: " + e.getMessage());
                    }

                    @Override
                    public void onNext(String result) {
                        Log.d("CONAN", "bookmark deleted: " + result);
                    }
                });
    }

    public void bookmarkAd(Integer adId) {
        service.bookmarkAdObserv(adId, getUserToken())
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<String>() {
                    @Override
                    public void onCompleted() {
                        Toast.makeText(context, "Zu den Favoriten hinzugefügt!", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d("CONAN", "error in bookmark ad: " + e.getMessage());
                    }

                    @Override
                    public void onNext(String result) {
                        Log.d("CONAN", "bookmark ad: " + result);
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

                    activity.setProfileName(json.getString("name"));
                    Profile profile = Profile.getCurrentProfile();
                    if (profile != null) {
                        Uri uri = profile.getProfilePictureUri(200, 200);
                        if (uri != null) {
                            activity.setProfilePicture(uri);
                            sendUserPicToServer(uri.toString());
                        } else {
                            activity.setProfilePicture(null);
                        }
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

    public void searchForArticles(int page, int size, Integer priceFrom, Integer priceTo, int distance, String description, String userId) {
        activity.setMainTitle("Suche");
        if (page == 0) {
            if (activity.listView != null) {
                activity.listView.setVisibility(View.INVISIBLE);
            }
            activity.showNumberOfAds(0);
        }
        activity.progressBar.setVisibility(ProgressBar.VISIBLE);

        if (!getUserToken().equals("")) {
            Observable<Long[]> getBookmarksObserv = service.getBookmarksForUserObserv(getUserToken());
            Observable<AdsAsPage> searchForAdsObserv = service.findAdsObserv(description, getLat(), getLng(), distance, priceFrom, priceTo, page, size, userId);

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
                            activity.progressBar.setVisibility(ProgressBar.GONE);
                            activity.showProblem(Constants.TYPE_SEARCH);
                        }

                        @Override
                        public void onNext(AdsAndBookmarks element) {
                            activity.progressBar.setVisibility(ProgressBar.GONE);
                            if (page == 0) {
                                if (activity.listView != null) {
                                    activity.listView.setVisibility(View.VISIBLE);
                                }
                                activity.updateAds(element, null, priceFrom, priceTo, distance, description, userId);
                            } else {
                                activity.addMoreAdsToList(element);
                            }
                        }
                    });
        } else {
            Observable<AdsAsPage> searchForAdsObserv = service.findAdsObserv(description, getLat(), getLng(), distance, priceFrom, priceTo, page, size, userId);

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
                            activity.progressBar.setVisibility(ProgressBar.GONE);
                            activity.showProblem(Constants.TYPE_SEARCH);
                        }

                        @Override
                        public void onNext(AdsAsPage adsAsPage) {
                            activity.progressBar.setVisibility(ProgressBar.GONE);
                            AdsAndBookmarks adsAndBookmarks = new AdsAndBookmarks();
                            adsAndBookmarks.setAds(adsAsPage);
                            //no user -> empty bookmarklist
                            adsAndBookmarks.setBookmarks(new ArrayList<>());
                            if (page == 0) {
                                if (activity.listView != null) {
                                    activity.listView.setVisibility(View.VISIBLE);
                                }
                                activity.updateAds(adsAndBookmarks, null, priceFrom, priceTo, distance, description, userId);
                            } else {
                                activity.addMoreAdsToList(adsAndBookmarks);
                            }
                        }
                    });
        }
    }

    private void loadBookmarkedAds(int page, int size, String type) {
        activity.setMainTitle("Meine Favoriten");

        if (page == 0) {
            if (activity.listView != null) {
                activity.listView.setVisibility(View.INVISIBLE);
            }
            activity.showNumberOfAds(0);
        }

        Double lat = getLat();
        Double lng = getLng();

        activity.progressBar.setVisibility(ProgressBar.VISIBLE);

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
                        activity.progressBar.setVisibility(ProgressBar.GONE);
                        activity.showProblem(type);
                    }

                    @Override
                    public void onNext(AdsAndBookmarks element) {
                        activity.progressBar.setVisibility(ProgressBar.GONE);
                        if (page == 0) {
                            if (activity.listView != null) {
                                activity.listView.setVisibility(View.VISIBLE);
                            }
                            activity.updateAds(element, type, null, null, null, null, null);
                        } else {
                            activity.addMoreAdsToList(element);
                        }
                    }
                });
    }

    private void loadAllAd(int page, int size, String type) {

        activity.setMainTitle("Luftkraftsport");
        Double lat = getLat();
        Double lng = getLng();

        if (page == 0) {
            if (activity.listView != null) {
                activity.listView.setVisibility(View.INVISIBLE);
            }
            activity.showNumberOfAds(0);
        }
        activity.progressBar.setVisibility(ProgressBar.VISIBLE);

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
                            activity.swipeContainer.setRefreshing(false);

                            if ("HTTP 401 Unauthorized".equals(e.getMessage())) {
                                //refresh userToken
                                refreshUserIdToken();
                            }
                            Log.d("CONAN", "Error in getting all ads: " + e.toString());
                            activity.progressBar.setVisibility(ProgressBar.GONE);
                            activity.showProblem(type);
                        }

                        @Override
                        public void onNext(AdsAndBookmarks element) {
                            activity.progressBar.setVisibility(ProgressBar.GONE);
                            if (page == 0) {
                                if (activity.listView != null) {
                                    activity.listView.setVisibility(View.VISIBLE);
                                }
                                activity.updateAds(element, type, null, null, null, null, null);
                            } else {
                                activity.addMoreAdsToList(element);
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
                            activity.progressBar.setVisibility(ProgressBar.GONE);
                            activity.showProblem(type);
                        }

                        @Override
                        public void onNext(AdsAsPage adsAsPage) {
                            activity.progressBar.setVisibility(ProgressBar.GONE);
                            AdsAndBookmarks adsAndBookmarks = new AdsAndBookmarks();
                            adsAndBookmarks.setAds(adsAsPage);
                            //no user -> empty bookmarklist
                            adsAndBookmarks.setBookmarks(new ArrayList<>());
                            if (page == 0) {
                                if (activity.listView != null) {
                                    activity.listView.setVisibility(View.VISIBLE);
                                }
                                activity.updateAds(adsAndBookmarks, type, null, null, null, null, null);
                            } else {
                                activity.addMoreAdsToList(adsAndBookmarks);
                            }
                        }
                    });
        }
    }

    private void getAdsForUser(int page, int size, String type, String token) {
        activity.setMainTitle("Meine Anzeigen");
        if (page == 0) {
            if (activity.listView != null) {
                activity.listView.setVisibility(View.INVISIBLE);
            }
            activity.showNumberOfAds(0);
        }
        activity.progressBar.setVisibility(ProgressBar.VISIBLE);

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
                        activity.progressBar.setVisibility(ProgressBar.GONE);
                        activity.showProblem(type);
                    }

                    @Override
                    public void onNext(AdsAndBookmarks element) {
                        activity.progressBar.setVisibility(ProgressBar.GONE);
                        if (page == 0) {
                            if (activity.listView != null) {
                                activity.listView.setVisibility(View.VISIBLE);
                            }
                            activity.updateAds(element, type, null, null, null, null, null);
                        } else {
                            activity.addMoreAdsToList(element);
                        }
                    }
                });
    }

    private void refreshUserIdToken() {

        if (activity.getGoogleApiClient() != null) {

            OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(activity.getGoogleApiClient());
            if (opr.isDone()) {
                Log.d("CONAN", "Got cached sign-in in GET ALL ADS!");
                GoogleSignInResult result = opr.get();
                handleSignInResult(result);
            } else {
                Log.d("CONAN", "cache sign-in leer, get user token from google in GET ALL ADS!");
                opr.setResultCallback(googleSignInResult -> handleSignInResult(googleSignInResult));
            }
        } else {
            Log.d("CONAN", "Google activity.getGoogleApiClient() is NULL during refreshUserIdToken");
        }
    }

    private void handleSignInResult(GoogleSignInResult result) {
        Log.d("CONAN", "handleSignInResult in follow search: " + result.isSuccess());
        if (result.isSuccess()) {
            GoogleSignInAccount acct = result.getSignInAccount();
            Log.d("CONAN", "neues Token von Google: " + acct.getIdToken());
            if (acct.getIdToken() != null) {
                setUserPreferences(null, null, acct.getIdToken());
                //TODO request data again -> todesschleife? was ist mit den anderen suchen -> in onError, wie die refreshen? Type??
                loadAllAd(0, 10, TYPE_ALL);
            }
        } else {
            Log.d("CONAN", "handleSignIn:  result ist nicht success!!!");
        }
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
        return Double.longBitsToDouble(settings.getLong(Constants.LAT, 0));
    }

    private String getUserToken() {
        return context.getSharedPreferences(SHARED_PREFS_USER_INFO, 0).getString(Constants.USER_TOKEN, "");
    }

    public void sendUserPicToServer(String userProfilePic) {

        Log.d("CONAN", "FART: " + userProfilePic);
        service.saveUserPictureObserv(getUserToken(), userProfilePic)
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

    public void getLastKnownLocation() {
        //get last location
        if (ContextCompat.checkSelfPermission(activity, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(activity.getGoogleApiClient());

            if (lastLocation != null) {
                Double lat = lastLocation.getLatitude();
                Double lng = lastLocation.getLongitude();
                //store in Shared prefs
                storeLocation(lat, lng);
            }
        }
    }

    private void storeLocation(Double lat, Double lng) {
        SharedPreferences sp = context.getSharedPreferences(Constants.USERS_LOCATION, MODE_PRIVATE);
        SharedPreferences.Editor ed = sp.edit();
        ed.putLong(Constants.LAT, Double.doubleToRawLongBits(lat));
        ed.putLong(Constants.LNG, Double.doubleToRawLongBits(lng));
        ed.apply();
    }
}
