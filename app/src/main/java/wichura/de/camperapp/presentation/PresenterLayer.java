package wichura.de.camperapp.presentation;

import java.util.List;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import wichura.de.camperapp.http.Service;
import wichura.de.camperapp.mainactivity.MainActivity;
import wichura.de.camperapp.models.RowItem;

/**
 * Created by ich on 20.10.2016.
 */

public class PresenterLayer {

    private MainActivity view;
    private Service service;
    private Subscription subscription;

    public PresenterLayer(MainActivity view, Service service){
        this.view = view;
        this.service = service;
    }

    public void loadAdData(){
        //view.showRxInProcess();
        Observable<List<RowItem>> friendResponseObservable = (Observable<List<RowItem>>)
                service.getAllAdsForUserObserv();
        subscription = friendResponseObservable.subscribe(new Observer<List<RowItem>>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(List<RowItem> response) {
                view.updateAds(response);
            }
        });
    }

    public void rxUnSubscribe(){
        if(subscription!=null && !subscription.isUnsubscribed())
            subscription.unsubscribe();
    }
}
