package de.wichura.lks.presentation;

import android.util.Log;
import android.view.View;

import java.util.List;

import de.wichura.lks.activity.MessagesActivity;
import de.wichura.lks.http.Service;
import de.wichura.lks.models.ArticleDetails;
import de.wichura.lks.models.MsgRowItem;


import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by Bernd Wichura on 01.11.2016.
 * Luftkraftsport
 */

public class MessagesPresenter {

    private MessagesActivity view;
    private Service service;
    private Disposable disposable;

    public MessagesPresenter(MessagesActivity view) {
        this.view = view;
        this.service = new Service();
    }

    public void loadMessages(String userToken, String chatPartner, Integer adId) {

        Observable<List<MsgRowItem>> getMessagesForAdObserv = service.getAllMessagesForAdObserv(userToken, chatPartner, adId);

        Log.d("CONAN", "message: sender, adId: " + chatPartner + ", " + adId);

        getMessagesForAdObserv
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<MsgRowItem>>() {
                    @Override
                    public void onComplete() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d("CONAN", "error in getting messages for an ad: " + e.toString());
                    }

                    @Override
                    public void onNext(List<MsgRowItem> msgRowItems) {
                        view.progress.setVisibility(View.GONE);
                        view.showLinkToAdButton();
                        view.showMessages(msgRowItems);
                    }

                    @Override
                    public void onSubscribe(Disposable d) {

                    }
                });
    }

    public void sendMessage(String message, Integer adId, String idTo, String userToken) {

        service.sendNewMessageObserv(message, adId, idTo, userToken)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onComplete() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d("CONAN", "error in sending message: " + e.getMessage());
                    }

                    @Override
                    public void onNext(String s) {
                        Log.d("CONAN", "message send: " + s);
                    }

                    @Override
                    public void onSubscribe(Disposable d) {

                    }
                });
    }

    public void disableSubscription() {
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
    }

    public void getAd(Integer articleId) {
        service.getAdDetailsObserv(articleId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ArticleDetails>() {
                    @Override
                    public void onComplete() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d("CONAN", "error in getting article details: " + e.getMessage());
                    }

                    @Override
                    public void onNext(ArticleDetails articleDetails) {
                        view.openAdActivityFor(articleDetails);
                    }

                    @Override
                    public void onSubscribe(Disposable d) {

                    }
                });
    }
}
