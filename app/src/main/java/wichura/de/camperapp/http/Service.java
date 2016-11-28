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
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import wichura.de.camperapp.models.MsgRowItem;
import wichura.de.camperapp.models.AdDetails;
import wichura.de.camperapp.models.AdsAsPage;
import wichura.de.camperapp.models.Bookmarks;
import wichura.de.camperapp.models.RowItem;

import static wichura.de.camperapp.http.Urls.GET_ADS_MY;
import static wichura.de.camperapp.http.Urls.GET_AD_DETAILS;
import static wichura.de.camperapp.http.Urls.GET_ALL_ADS_URL;
import static wichura.de.camperapp.http.Urls.GET_ALL_MESSAGES_FOR_AD;
import static wichura.de.camperapp.http.Urls.GET_BOOKMARKS_FOR_USER;
import static wichura.de.camperapp.http.Urls.GET_FIND_ADS;
import static wichura.de.camperapp.http.Urls.SEND_MESSAGE;

/**
 * Created by ich on 16.10.2016.
 * CamperApp
 */

public class Service {

    private static final String WEB_SERVICE_BASE_URL = Urls.MAIN_SERVER_URL;
    private static final String WEB_SERVICE_BASE_URL_V2 = Urls.MAIN_SERVER_URL_V2;

    private final WebService mWebService;
    private final WebService mWebServiceV2;

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

        //api/V2
        OkHttpClient.Builder httpClientV2 = new OkHttpClient.Builder();

        httpClient.addInterceptor(logging);

        Retrofit restAdapterV2 = new Retrofit.Builder()
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(WEB_SERVICE_BASE_URL_V2)
                .client(httpClientV2.build())
                .build();

        mWebServiceV2 = restAdapterV2.create(WebService.class);
    }

    private interface WebService {
        @GET(GET_BOOKMARKS_FOR_USER)
        Observable<Bookmarks> getBookmarksForUser(@Query("userId") String userId);

        @GET("anfang/{lastPart}")
        Observable<List<RowItem>> getExample(@Path("lastPart") String lastPart);

        @GET(GET_ALL_MESSAGES_FOR_AD)
        Observable<List<MsgRowItem>> getAllMessagesForAd(
                @Query("userId") String userId,
                @Query("sender") String sender,
                @Query("adId") String adId);

        @GET(SEND_MESSAGE)
        Observable<String> sendMessage(
                @Query("message") String message,
                @Query("adId") String adId,
                @Query("idFrom") String idFrom,
                @Query("idTo") String idTo);

        @GET(GET_AD_DETAILS)
        Observable<AdDetails> getAdDetails(@Query("adId") String adId);

        @GET(GET_FIND_ADS)
        Observable<AdsAsPage> getFindAds(@Query("page") int page, @Query("size") int size);

        @GET(GET_ADS_MY)
        Observable<AdsAsPage> getAdsMy(
                @Query("page") int page,
                @Query("size") int size,
                @Query("token") String token);
    }

    public Observable<AdsAsPage> getAdsMyObserv(int page, int size, String token) {
        return mWebServiceV2.getAdsMy(page, size, token);
    }

    public Observable<AdsAsPage> getFindAdsObserv(int page, int size) {
        return mWebService.getFindAds(page, size);
    }

    public Observable<AdDetails> getAdDetailsObserv(String adId) {
        return mWebService.getAdDetails(adId);
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
}
