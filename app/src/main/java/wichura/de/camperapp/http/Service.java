package wichura.de.camperapp.http;

import java.util.List;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import wichura.de.camperapp.models.RowItem;

import static wichura.de.camperapp.http.Urls.GET_ALL_ADS_URL;
import static wichura.de.camperapp.http.Urls.GET_BOOKMARKS_FOR_USER;

/**
 * Created by ich on 16.10.2016.
 * CamperApp
 */

public class Service {

    private static final String WEB_SERVICE_BASE_URL = Urls.MAIN_SERVER_URL;

    private final WebService mWebService;

    public Service() {


        Retrofit  restAdapter = new Retrofit.Builder()
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(WEB_SERVICE_BASE_URL)
                .build();

        mWebService = restAdapter.create(WebService.class);
    }

    private interface WebService {
        @GET(GET_BOOKMARKS_FOR_USER)
        Observable<String> getBookmarksForUser(@Query("userId") String userId);

        @GET(GET_ALL_ADS_URL)
        Observable<List<RowItem>> getAllAdsForUser();


    }

    public Observable<String> getBookmarksForUserObserv(String userId) {

        return mWebService.getBookmarksForUser(userId)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
    }


    public Observable<List<RowItem>> getAllAdsForUserObserv() {

        return mWebService.getAllAdsForUser()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
    }


}
