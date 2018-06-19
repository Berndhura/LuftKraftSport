package de.wichura.lks.presentation;

import android.util.Log;

import java.util.List;

import de.wichura.lks.activity.MessagesOverviewActivity;
import de.wichura.lks.http.Service;
import de.wichura.lks.models.GroupedMsgItem;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by Bernd Wichura on 05.12.2016.
 * Luftkraftsport
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
                .subscribe(new Observer<List<GroupedMsgItem>>() {
                    @Override
                    public void onComplete() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d("CONAN", "error in getting message overview " + e.toString());
                    }

                    @Override
                    public void onNext(List<GroupedMsgItem> msgRowItems) {
                        view.hideProgressBar();
                        if (msgRowItems.size() == 0) {
                            view.emptyPage();
                        } else {
                            view.updateMsgList(msgRowItems);
                        }
                    }

                    @Override
                    public void onSubscribe(Disposable d) {

                    }
                });
    }
}
