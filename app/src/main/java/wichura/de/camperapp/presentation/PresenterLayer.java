package wichura.de.camperapp.presentation;

import android.content.Context;
import android.util.Log;
import android.widget.ProgressBar;

import java.util.List;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.functions.Func2;
import rx.schedulers.Schedulers;
import wichura.de.camperapp.http.Service;
import wichura.de.camperapp.mainactivity.Constants;
import wichura.de.camperapp.mainactivity.MainActivity;
import wichura.de.camperapp.models.AdsAndBookmarks;
import wichura.de.camperapp.models.RowItem;

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

    public void loadAdData(String url) {
        view.progressBar.setVisibility(ProgressBar.VISIBLE);

        Observable<String> getBookmarksObserv = service.getBookmarksForUserObserv(getUserId()).subscribeOn(Schedulers.newThread());
        Observable<List<RowItem>> getAllAdsForUserObserv = service.getAllUrlObserv(url).subscribeOn(Schedulers.newThread());

        Observable<AdsAndBookmarks> zippedReqForBookmarksAndAds =
                Observable.zip(getBookmarksObserv, getAllAdsForUserObserv, new Func2<String, List<RowItem>, AdsAndBookmarks>() {
                    @Override
                    public AdsAndBookmarks call(String bookmarks, List<RowItem> ads) {
                        AdsAndBookmarks elements = new AdsAndBookmarks();
                        elements.setAds(ads);
                        elements.setBookmarks(bookmarks);
                        return elements;
                    }
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

                view.progressBar.setVisibility(ProgressBar.GONE);
                view.updateAds(element);
            }
        });
    }

    public void rxUnSubscribe() {
        if (subscription != null && !subscription.isUnsubscribed())
            subscription.unsubscribe();
    }

    private String getUserId() {
        return context.getSharedPreferences("UserInfo", 0).getString(Constants.USER_ID, "");
    }
}
