package wichura.de.camperapp.http;

import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Url;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import wichura.de.camperapp.messages.MsgRowItem;
import wichura.de.camperapp.models.Bookmarks;
import wichura.de.camperapp.models.RowItem;

import static wichura.de.camperapp.http.Urls.GET_ALL_ADS_URL;
import static wichura.de.camperapp.http.Urls.GET_ALL_MESSAGES_FOR_AD;
import static wichura.de.camperapp.http.Urls.GET_BOOKMARKS_FOR_USER;
import static wichura.de.camperapp.http.Urls.SEND_MESSAGE;

/**
 * Created by ich on 16.10.2016.
 * CamperApp
 */

public class Service {

    private static final String WEB_SERVICE_BASE_URL = Urls.MAIN_SERVER_URL;

    private final WebService mWebService;

    public Service() {

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

        httpClient.addInterceptor(logging);

        Retrofit restAdapter = new Retrofit.Builder()
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(WEB_SERVICE_BASE_URL)
                .client(httpClient.build())
                .build();

        mWebService = restAdapter.create(WebService.class);
    }

    private interface WebService {
        @GET(GET_BOOKMARKS_FOR_USER)
        Observable<Bookmarks> getBookmarksForUser(@Query("userId") String userId);

        @GET(GET_ALL_ADS_URL)
        Observable<List<RowItem>> getAllAdsForUser();

        @GET
        Observable<List<RowItem>> getAllUrl(@Url String url);

        @GET("anfang/{lastPart}")
        Observable<List<RowItem>> getExample(@Path("lastPart") String lastPart);

        @GET(GET_ALL_MESSAGES_FOR_AD)
        Observable<List<MsgRowItem>>getAllMessagesForAd(
                @Query("userId") String userId,
                @Query("sender") String sender,
                @Query("adId") String adId);

        @GET(SEND_MESSAGE)
        Observable<String> sendMessage(
                @Query("message") String message,
                @Query("adId") String adId,
                @Query("idFrom") String idFrom,
                @Query("idTo") String idTo);
    }

    public Observable<String> sendMessagesObserv(String message, String adId, String idFrom, String idTo) {
        return mWebService.sendMessage(message, adId, idFrom, idTo)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<Bookmarks> getBookmarksForUserObserv(String userId) {

        return mWebService.getBookmarksForUser(userId)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<List<MsgRowItem>> getAllMessagesForAdObserv(String userId, String sender, String adId) {
        return mWebService.getAllMessagesForAd(userId, sender, adId)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
    }


    public Observable<List<RowItem>> getAllAdsForUserObserv() {

        return mWebService.getAllAdsForUser()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<List<RowItem>> getAllUrlObserv(String url) {

        return mWebService.getAllUrl(url)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
    }
}