package wichura.de.camperapp.ad;

import android.content.Context;
import android.util.Log;

import java.util.List;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.schedulers.Schedulers;
import wichura.de.camperapp.http.Service;
import wichura.de.camperapp.mainactivity.Constants;
import wichura.de.camperapp.models.AdsAndBookmarks;
import wichura.de.camperapp.models.Bookmarks;
import wichura.de.camperapp.models.RowItem;

/**
 * Created by bwichura on 25.10.2016.
 * CamperApp
 */

public class PresenterLayerMyAd {

    private Service service;
    private Context context;
    private MyAdsActivity view;
    private Subscription subscription;

    public PresenterLayerMyAd(MyAdsActivity myAdsActivity, Service service, Context applicationContext) {
        this.service = service;
        this.context = applicationContext;
        this.view = myAdsActivity;
    }

    public void loadMyAdsData(String url) {

        Observable<Bookmarks> getBookmarksObserv = service.getBookmarksForUserObserv(getUserId()).subscribeOn(Schedulers.newThread());
        Observable<List<RowItem>> getAllAdsForUserObserv = service.getAllUrlObserv(url).subscribeOn(Schedulers.newThread());

        Observable<AdsAndBookmarks> zippedReqForBookmarksAndAds
                = Observable.zip(getBookmarksObserv, getAllAdsForUserObserv, (bookmarks, ads) ->
        {
            AdsAndBookmarks elements = new AdsAndBookmarks();
            elements.setAds(ads);
            elements.setBookmarks(bookmarks.getBookmarks());
            return elements;
        });

        subscription = zippedReqForBookmarksAndAds.subscribe(new Observer<AdsAndBookmarks>() {
            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(Throwable e) {
                Log.d("CONAN", "Error in Observer: " + e.toString());
            }

            @Override
            public void onNext(AdsAndBookmarks element) {
                view.updateAds(element);
            }
        });
    }

    private String getUserId() {
        return context.getSharedPreferences("UserInfo", 0).getString(Constants.USER_ID, "");
    }
}
