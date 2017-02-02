package wichura.de.camperapp.presentation;

import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import wichura.de.camperapp.activity.MessagesActivity;
import wichura.de.camperapp.http.Service;
import wichura.de.camperapp.models.ArticleDetails;
import wichura.de.camperapp.models.MsgRowItem;

/**
 * Created by ich on 01.11.2016.
 * CamperApp
 */

public class MessagesPresenter {

    private MessagesActivity view;
    private Service service;
    private Subscription subscription;

    public MessagesPresenter(MessagesActivity view) {
        this.view = view;
        this.service = new Service();
    }

    public void loadMessages(String userToken, String chatPartner, Integer adId) {

        if (view.listView!=null) {
            view.listView.setVisibility(View.INVISIBLE);
        }
        view.enableProgress();

        Observable<List<MsgRowItem>> getMessagesForAdObserv = service.getAllMessagesForAdObserv(userToken, chatPartner, adId);

        Log.d("CONAN", "message: sender, adId: " + chatPartner + ", " + adId);

        subscription = getMessagesForAdObserv
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(new Subscriber<List<MsgRowItem>>() {
                    @Override
                    public void onCompleted() {}

                    @Override
                    public void onError(Throwable e) {
                        Log.d("CONAN", "error in getting messages for an ad: " + e.toString());
                    }

                    @Override
                    public void onNext(List<MsgRowItem> msgRowItems) {
                        view.progressBar.setVisibility(ProgressBar.GONE);
                        if (view.listView!=null) {
                            view.listView.setVisibility(View.VISIBLE);
                        }
                        view.showMessages(msgRowItems);
                        view.showLinkToAdButton();
                    }
                });
    }

    public void sendMessage(String message, Integer adId, String idTo, String userToken) {

        service.sendNewMessageObserv(message, adId, idTo, userToken)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<String>() {
                    @Override
                    public void onCompleted() {}

                    @Override
                    public void onError(Throwable e) {
                        Log.d("CONAN", "error in sending message: "+e.getMessage());
                    }

                    @Override
                    public void onNext(String s) {
                        Log.d("CONAN", "message send: "+s);
                    }
                });
    }

    public void disableSubscription() {
        if (subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
        }
    }

    public void getAd(Integer articleId) {
        service.getAdDetailsObserv(articleId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<ArticleDetails>() {
                    @Override
                    public void onCompleted() {}

                    @Override
                    public void onError(Throwable e) {
                        Log.d("CONAN", "error in getting article details: " + e.getMessage());
                    }

                    @Override
                    public void onNext(ArticleDetails articleDetails) {
                        view.openAdActivityFor(articleDetails);
                    }
                });
    }
}
