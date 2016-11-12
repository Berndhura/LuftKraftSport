package wichura.de.camperapp.messages;

import android.util.Log;

import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import wichura.de.camperapp.http.Service;
import wichura.de.camperapp.models.RowItem;

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

    public void sendMessage(String message, String adId, String idFrom, String idTo) {

        service.sendMessagesObserv(message, adId, idFrom, idTo)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<String>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(String s) {

                    }
                });
    }

    protected void disableSubscription() {
        if (subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
        }
    }

    public void getAd(String adId) {
        service.getAdObserv(adId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<RowItem>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(RowItem ad) {
                        view.openAdActivityFor(ad);
                    }
                });
    }
}
