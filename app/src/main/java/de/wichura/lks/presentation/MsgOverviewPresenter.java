package de.wichura.lks.presentation;

import android.util.Log;

import java.util.List;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import de.wichura.lks.activity.MessagesOverviewActivity;
import de.wichura.lks.http.Service;
import de.wichura.lks.models.GroupedMsgItem;

/**
 * Created by ich on 05.12.2016.
 * Camper App
 */

public class MsgOverviewPresenter {

    private MessagesOverviewActivity view;
    private Service service;


    public MsgOverviewPresenter(MessagesOverviewActivity view, Service service) {
        this.view = view;
        this.service = service;
    }

    public void loadAllMessages(String userToken) {

        service.getAllMessagesFromUserObserv(userToken)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<List<GroupedMsgItem>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d("CONAN", "error in getting message overview " + e.toString());
                    }

                    @Override
                    public void onNext(List<GroupedMsgItem> msgRowItems) {
                        view.hideProgressBar();
                        view.updateMsgList(msgRowItems);
                    }
                });
    }
}
