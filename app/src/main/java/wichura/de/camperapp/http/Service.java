package wichura.de.camperapp.http;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import wichura.de.camperapp.models.AdDetails;
import wichura.de.camperapp.models.AdsAsPage;
import wichura.de.camperapp.models.Bookmarks;
import wichura.de.camperapp.models.MsgRowItem;

import static wichura.de.camperapp.http.Urls.GET_ADS_MY;
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

        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        Retrofit restAdapterV2 = new Retrofit.Builder()
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .baseUrl(WEB_SERVICE_BASE_URL_V2)
                .client(httpClientV2.build())
                .build();

        mWebServiceV2 = restAdapterV2.create(WebService.class);
    }

    private interface WebService {
        @GET(GET_BOOKMARKS_FOR_USER)
        Observable<Bookmarks> getBookmarksForUser(@Query("userId") String userId);

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

        @GET("/ads/{adId}")
        Observable<AdDetails> getAdDetails(@Query("adId") Integer adId);

        @GET(GET_FIND_ADS)
        Observable<AdsAsPage> getFindAds(@Query("page") int page, @Query("size") int size);

        @GET(GET_ADS_MY)
        Observable<AdsAsPage> getAdsMy(
                @Query("page") int page,
                @Query("size") int size,
                @Query("token") String token);

        @POST("ads/{adId}/increaseViewCount")
        Observable<String> increaseViewCount(@Path("adId") String adId);

        @POST("ads/{adId}/bookmark")
        Observable<String> bookmarkAd(@Path("adId") String adId, @Query("token") String token);

        @DELETE("bookmarks/{adId}")
        Observable<String> delBookmarkAd(@Path("adId") String adId, @Query("token") String token);

        @POST("messages")
        Observable<String> sendNewMessage(@Query("message") String message,
                                          @Query("adId") String adId,
                                          @Query("idTo") String idTo,
                                          @Query("token") String token);

        @DELETE("ads/{adId}")
        Observable<String> deleteAd(@Path("adId") String adId);

        @POST("users")
        Observable<String> createUser(@Query("name") String name, @Query("token") String token);

        @POST("/users/sendToken")
        Observable<String> sendDeviceToken(@Query("token") String token,
                                           @Query("deviceToken") String deviceToken);
    }

    public Observable<String> sendDeviceTokenObserv(String userToken, String deviceToken) {
        return mWebServiceV2.sendDeviceToken(userToken, deviceToken);
    }

    public Observable<String> createUserObserv(String name, String userToken) {
        return mWebServiceV2.createUser(name, userToken);
    }

    public Observable<String> deleteAdObserv(String adId) {
        return mWebServiceV2.deleteAd(adId);
    }

    public Observable<String> sendNewMessageObserv(String message, String adId, String idTo, String userToken) {
        return mWebServiceV2.sendNewMessage(message, adId, idTo, userToken);
    }

    public Observable<String> delBookmarkAdObserv(String adId, String userToken) {
        return mWebServiceV2.delBookmarkAd(adId, userToken);
    }

    public Observable<String> bookmarkAdObserv(String adId, String userToken) {
        return mWebServiceV2.bookmarkAd(adId, userToken);
    }

    public Observable<String> increaseViewCount(String adId) {
        return mWebServiceV2.increaseViewCount(adId);
    }

    public Observable<AdsAsPage> getAdsMyObserv(int page, int size, String token) {
        return mWebServiceV2.getAdsMy(page, size, token);
    }

    public Observable<AdsAsPage> getFindAdsObserv(int page, int size) {
        return mWebService.getFindAds(page, size);
    }

    public Observable<AdDetails> getAdDetailsObserv(Integer adId) {
        return mWebServiceV2.getAdDetails(adId);
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
