package wichura.de.camperapp.http;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;
import rx.Observable;
import wichura.de.camperapp.models.AdDetails;
import wichura.de.camperapp.models.AdsAsPage;
import wichura.de.camperapp.models.GroupedMsgItem;
import wichura.de.camperapp.models.MsgRowItem;
import wichura.de.camperapp.models.RowItem;

/**
 * Created by ich on 16.10.2016.
 * CamperApp
 */

public class Service {

    private static final String WEB_SERVICE_BASE_URL_V2 = Urls.MAIN_SERVER_URL_V2;

    private final WebService mWebServiceV2;

    public Service() {

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        //api/V2
        OkHttpClient.Builder httpClientV2 = new OkHttpClient.Builder();

        httpClientV2.addInterceptor(logging);

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
        @GET("bookmarkIds")
        Observable<String[]> getBookmarksForUser(@Query("token") String userToken);

        @GET("messages/forArticle")
        Observable<List<MsgRowItem>> getAllMessagesForAd(
                @Query("token") String userToken,
                @Query("sender") String chatPartner,
                @Query("articleId") String articleId);

        @GET("articles/{articleId}")
        Observable<AdDetails> getAdDetails(@Path("articleId") Integer articleId);

        @GET("articles")
        Observable<AdsAsPage> findAds(@Query("description") String description,
                                      @Query("priceFrom") int priceFrom,
                                      @Query("priceTo") int priceTo,
                                      @Query("page") int page,
                                      @Query("size") int size);

        @GET("articles")
        Observable<AdsAsPage> getAllAds(@Query("page") int page,
                                        @Query("size") int size);

        @GET("articles/my")
        Observable<AdsAsPage> getAdsMy(@Query("page") int page, @Query("size") int size, @Query("token") String token);

        @GET("bookmarks")
        Observable<AdsAsPage> getMyBookmarkedAds(@Query("page") int page, @Query("size") int size, @Query("token") String token);

        @POST("articles/{articleId}/increaseViewCount")
        Observable<String> increaseViewCount(@Path("articleId") String articleId);

        @POST("articles/{articleId}/bookmark")
        Observable<String> bookmarkAd(@Path("articleId") String articleId, @Query("token") String token);

        @DELETE("bookmarks/{articleId}")
        Observable<String> delBookmarkAd(@Path("articleId") String articleId, @Query("token") String token);

        @POST("messages")
        Observable<String> sendNewMessage(@Query("message") String message,
                                          @Query("articleId") String articleId,
                                          @Query("idTo") String idTo,
                                          @Query("token") String token);

        @DELETE("articles/{articleId}")
        Observable<String> deleteAd(@Path("articleId") String articleId);

        @POST("users")
        Observable<String> createUser(@Query("name") String name, @Query("token") String token);

        @POST("users/sendToken")
        Observable<String> sendDeviceToken(@Query("token") String token,
                                           @Query("deviceToken") String deviceToken);

        @POST("users/login")
        Observable<String> loginUser(@Query("email") String email,
                                     @Query("password") String password);

        @GET("messages/forUser")
        Observable<List<GroupedMsgItem>> getAllMessagesFromUser(@Query("token") String userToken);

        @Multipart
        @POST("articles/{articleId}/addPicture")
        Observable<String> uploadPicture(@Query("articleId") Integer articleId,
                                         @Query("token") String userToken,
                                         @Part("file") MultipartBody.Part file);

        @POST("articles")
        Observable<RowItem> saveNewAd(@Query("token") String userToken,
                                      @Body RowItem item);

        @GET("pictures/{pictureId}/thumbnail")
        Observable<byte[]> getPictureThumbnail(@Path("pictureId") Integer pictureId);

        @GET("pictures/{pictureId}")
        Observable<byte[]> getPicture(@Path("pictureId") Integer pictureId);
    }

    public Observable<String> uploadPictureObserv(Integer adId, String userToken, MultipartBody.Part file) {
        return mWebServiceV2.uploadPicture(adId, userToken, file);
    }

    public Observable<RowItem> saveNewAdObserv(String userToken, RowItem item) {
        return mWebServiceV2.saveNewAd(userToken, item);
    }

    public Observable<List<GroupedMsgItem>> getAllMessagesFromUserObserv(String userToken) {
        return mWebServiceV2.getAllMessagesFromUser(userToken);
    }

    public Observable<String> loginUserObserv(String email, String password) {
        return mWebServiceV2.loginUser(email, password);
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

    public Observable<AdsAsPage> getMyBookmarkedAdsObserv(int page, int size, String token) {
        return mWebServiceV2.getMyBookmarkedAds(page, size, token);
    }

    public Observable<AdsAsPage> findAdsObserv(String description, int priceFrom, int priceTo, int page, int size) {
        return mWebServiceV2.findAds(description, priceFrom, priceTo, page, size);
    }

    public Observable<AdsAsPage> getAllAdsObserv(int page, int size) {
        return mWebServiceV2.getAllAds(page, size);
    }

    public Observable<AdDetails> getAdDetailsObserv(Integer adId) {
        return mWebServiceV2.getAdDetails(adId);
    }

    public Observable<String[]> getBookmarksForUserObserv(String userToken) {
        return mWebServiceV2.getBookmarksForUser(userToken);
    }

    public Observable<List<MsgRowItem>> getAllMessagesForAdObserv(String userToken, String sender, String adId) {
        return mWebServiceV2.getAllMessagesForAd(userToken, sender, adId);
    }
}
