package wichura.de.camperapp.ad;

import android.content.Context;

import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import wichura.de.camperapp.http.Service;
import wichura.de.camperapp.mainactivity.Constants;
import wichura.de.camperapp.models.Bookmarks;

/**
 * Created by ich on 13.11.2016.
 */

public class PresenterLayerOpenAd {

    private Service service;
    private Context context;
    private OpenAdActivity view;
    private Subscription subscription;

    public PresenterLayerOpenAd(OpenAdActivity myAdsActivity, Service service, Context applicationContext) {
        this.service = service;
        this.context = applicationContext;
        this.view = myAdsActivity;
    }

    public void loadBookmarksForUser() {
        service.getBookmarksForUserObserv(getUserId()).subscribeOn(Schedulers.newThread())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Bookmarks>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(Bookmarks bookmarks) {
                        view.updateBookmarkButton(bookmarks);
                    }
                });
    }

    private String getUserId() {
        return context.getSharedPreferences("UserInfo", 0).getString(Constants.USER_ID, "");
    }
}
