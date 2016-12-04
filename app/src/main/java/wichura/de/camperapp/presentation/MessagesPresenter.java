package wichura.de.camperapp.presentation;

import android.util.Log;

import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import wichura.de.camperapp.activity.MessagesActivity;
import wichura.de.camperapp.http.Service;
import wichura.de.camperapp.models.AdDetails;
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

    public void loadMessages(String userId, String sender, String adId) {

        /*if (view.listView!=null) {
            view.listView.setVisibility(View.INVISIBLE);
        }
        view.enableProgress();
*/
        Observable<List<MsgRowItem>> getMessagesForAdObserv = service.getAllMessagesForAdObserv(userId, sender, adId);

        subscription = getMessagesForAdObserv
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(new Subscriber<List<MsgRowItem>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d("CONAN", e.toString());
                    }

                    @Override
                    public void onNext(List<MsgRowItem> msgRowItems) {
                        /*view.progressBar.setVisibility(ProgressBar.GONE);
                        if (view.listView!=null) {
                            view.listView.setVisibility(View.VISIBLE);
                        }*/
                        view.showMessages(msgRowItems);
                        view.showLinkToAdButton();
                    }
                });
    }

    public void sendMessage(String message, String adId, String idTo, String userToken) {

        service.sendNewMessageObserv(message, adId, idTo, userToken)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<String>() {
                    @Override
                    public void onCompleted() {

                    }

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

    public void getAd(String adId) {
        service.getAdDetailsObserv(Integer.parseInt(adId))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<AdDetails>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d("CONAN", e.getMessage() + "\n" + e.getCause());
                    }

                    @Override
                    public void onNext(AdDetails adDetails) {
                        view.openAdActivityFor(adDetails.getAd());
                    }
                });
    }
}
