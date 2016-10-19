package wichura.de.camperapp.http;

import java.util.List;

import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.http.GET;
import retrofit.http.Query;
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
        RequestInterceptor requestInterceptor = new RequestInterceptor() {
            @Override
            public void intercept(RequestInterceptor.RequestFacade request) {
                request.addHeader("Accept", "application/json");
            }
        };

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(WEB_SERVICE_BASE_URL)
                .setRequestInterceptor(requestInterceptor)
                .setLogLevel(RestAdapter.LogLevel.FULL)
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
