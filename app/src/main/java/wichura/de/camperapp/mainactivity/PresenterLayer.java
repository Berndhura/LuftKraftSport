package wichura.de.camperapp.mainactivity;

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

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import wichura.de.camperapp.http.Service;
import wichura.de.camperapp.mainactivity.Constants;
import wichura.de.camperapp.mainactivity.MainActivity;
import wichura.de.camperapp.models.AdsAndBookmarks;
import wichura.de.camperapp.models.AdsAsPage;
import wichura.de.camperapp.models.Bookmarks;

import static wichura.de.camperapp.mainactivity.Constants.SHARED_PREFS_USER_INFO;

/**
 * Created by ich on 20.10.2016.
 * CamperApp
 */

public class PresenterLayer {

    private MainActivity view;
    private Service service;
    private Subscription subscription;
    private Context context;

    public PresenterLayer(MainActivity view, Service service, Context context) {
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

    public void loadAdDataPage(int page, int size) {
        if (page == 0) {
            if (view.listView != null) {
                view.listView.setVisibility(View.INVISIBLE);
            }
        }
        view.progressBar.setVisibility(ProgressBar.VISIBLE);
        //Log.d("CONAN", url);

        Observable<Bookmarks> getBookmarksObserv = service.getBookmarksForUserObserv(getUserId());
        Observable<AdsAsPage> getAllAdsForUserObserv = service.getFindAdsObserv(page, size);

        Observable<AdsAndBookmarks> zippedReqForBookmarksAndAds =
                Observable.zip(getBookmarksObserv, getAllAdsForUserObserv, (bookmarks, ads) ->
                {

                    AdsAndBookmarks elements = new AdsAndBookmarks();
                    elements.setAds(ads);
                    elements.setBookmarks(bookmarks.getBookmarks());
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
                        Log.d("CONAN", "Error in Observer: " + e.toString());
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
                            view.updateAds(element);
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

    private String getUserId() {
        return context.getSharedPreferences(SHARED_PREFS_USER_INFO, 0).getString(Constants.USER_ID, "");
    }
}
