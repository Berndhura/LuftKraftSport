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
import wichura.de.camperapp.mainactivity.Constants;
import wichura.de.camperapp.models.AdsAsPage;
import wichura.de.camperapp.models.ArticleDetails;
import wichura.de.camperapp.models.GroupedMsgItem;
import wichura.de.camperapp.models.MsgRowItem;
import wichura.de.camperapp.models.RowItem;
import wichura.de.camperapp.models.SearchItem;
import wichura.de.camperapp.models.User;

/**
 * Created by ich on 16.10.2016.
 * CamperApp
 */

public class Service {

    private static final String WEB_SERVICE_BASE_URL_V3 = Urls.MAIN_SERVER_URL_V3;

    private final WebService mWebServiceV3;

    public Service() {

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        //api/V3
        OkHttpClient.Builder httpClientV3 = new OkHttpClient.Builder();

        httpClientV3.addInterceptor(logging);

        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        Retrofit restAdapterV2 = new Retrofit.Builder()
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .baseUrl(WEB_SERVICE_BASE_URL_V3)
                .client(httpClientV3.build())
                .build();

        mWebServiceV3 = restAdapterV2.create(WebService.class);
    }

    private interface WebService {

        @GET("bookmarks/ids")
        Observable<Long[]> getBookmarksForUser(
                @Query("token") String userToken);

        @GET("messages/forArticle")
        Observable<List<MsgRowItem>> getAllMessagesForAd(
                @Query("token") String userToken,
                @Query("sender") String chatPartner,
                @Query("articleId") int articleId);

        @GET("articles/{articleId}")
        Observable<ArticleDetails> getAdDetails(@Path("articleId") Integer articleId);

        @GET("articles")
        Observable<AdsAsPage> findAds(@Query("description") String description,
                                      @Query("lat") Double lat,
                                      @Query("lng") Double lng,
                                      @Query("distance") int distance,
                                      @Query("priceFrom") Integer priceFrom,
                                      @Query("priceTo") Integer priceTo,
                                      @Query("page") int page,
                                      @Query("size") int size,
                                      @Query("userId") String userId);

        @GET("articles")
        Observable<AdsAsPage> getAllAds(@Query("lat") Double lat,
                                        @Query("lng") Double lng,
                                        @Query("distance") int distance,
                                        @Query("page") int page,
                                        @Query("size") int size);

        @GET("articles/my")
        Observable<AdsAsPage> getAdsMy(@Query("page") int page, @Query("size") int size, @Query("token") String token);

        @GET("bookmarks")
        Observable<AdsAsPage> getMyBookmarkedAds(
                @Query("lat") Double lat,
                @Query("lng") Double lng,
                @Query("page") int page,
                @Query("size") int size,
                @Query("token") String token);

        @POST("articles/{articleId}/increaseViewCount")
        Observable<String> increaseViewCount(@Path("articleId") int articleId);

        @POST("articles/{articleId}/bookmark")
        Observable<String> bookmarkAd(@Path("articleId") int articleId, @Query("token") String token);

        @DELETE("bookmarks/{articleId}")
        Observable<String> delBookmarkAd(@Path("articleId") int articleId, @Query("token") String token);

        @POST("messages")
        Observable<String> sendNewMessage(@Query("message") String message,
                                          @Query("articleId") int articleId,
                                          @Query("idTo") String idTo,
                                          @Query("token") String token);

        @DELETE("articles/{articleId}")
        Observable<String> deleteAd(@Path("articleId") int articleId, @Query("token") String token);

        @GET("messages/forUser")
        Observable<List<GroupedMsgItem>> getAllMessagesFromUser(@Query("token") String userToken);

        @Multipart
        @POST("articles/{articleId}/addPicture")
        Observable<String> uploadPicture(@Path("articleId") Long articleId,
                                         @Query("token") String userToken,
                                         @Part MultipartBody.Part file);

        @POST("articles")
        Observable<RowItem> saveNewAd(@Query("token") String userToken,
                                      @Body RowItem item);

        @GET("pictures/{pictureId}/thumbnail")
        Observable<byte[]> getPictureThumbnail(@Path("pictureId") Integer pictureId);

        @GET("pictures/{pictureId}")
        Observable<byte[]> getPicture(@Path("pictureId") Integer pictureId);

        @GET("searches")
        Observable<List<SearchItem>> findSearches(@Query("token") String userToken);

        @DELETE("searches/{id}")
        Observable<String> deleteSearch(@Path("id") Long id, @Query("token") String userToken);

        @POST("searches/new")
        Observable<String> saveSearch(@Query("description") String description,
                                      @Query("priceFrom") Integer priceFrom,
                                      @Query("priceTo") Integer priceTo,
                                      @Query("lat") Double latitude,
                                      @Query("lng") Double longitude,
                                      @Query("distance") Long distance,
                                      @Query("token") String userToken);

        @POST("users/sendToken")
        Observable<String> sendDeviceToken(@Query("token") String token,
                                           @Query("deviceToken") String deviceToken);

        @POST("users/register")
        Observable<String> registerUser(@Query("name") String name,
                                        @Query("email") String email,
                                        @Query("password") String password);

        @POST("users/activate")
        Observable<String> activateUser(@Query("activation_code") String activationCode,
                                        @Query("email") String email);

        @POST("users/login")
        Observable<User> loginUser(@Query("email") String email,
                                   @Query("password") String password);

        @GET("users/{userId}")
        Observable<User> getSellerInformation(
                @Path("userId") String userId,
                @Query("token") String userToken);

        @POST("users/profilePictureUrl")
        Observable<String> saveUserPicture(
                @Query("token") String userToken,
                @Query("url") String userPicUrl);

    }

    public Observable<String> saveUserPictureObserv(String token, String userPicUrl) {
        return mWebServiceV3.saveUserPicture(token, userPicUrl);
    }

    public Observable<User> getSellerInformationObserv(String userId, String token) {
        return mWebServiceV3.getSellerInformation(userId, token);
    }

    public Observable<String> registerUserObserv(String name,
                                                 String email,
                                                 String password) {
        return mWebServiceV3.registerUser(name, email, password);
    }

    public Observable<String> activateUserObserv(String activationCode,
                                                 String email) {
        return mWebServiceV3.activateUser(activationCode, email);
    }

    public Observable<User> loginUserObserv(String email,
                                            String password) {
        return mWebServiceV3.loginUser(email, password);
    }

    public Observable<String> saveSearchObserv(String description,
                                               Integer priceFrom,
                                               Integer priceTo,
                                               Double lat,
                                               Double lng,
                                               Long distance,
                                               String userToken) {
        return mWebServiceV3.saveSearch(description, priceFrom, priceTo, lat, lng, distance, userToken);
    }

    public Observable<String> deleteSearchesObserv(Long id, String userToken) {
        return mWebServiceV3.deleteSearch(id, userToken);
    }

    public Observable<List<SearchItem>> findSearchesObserv(String userToken) {
        return mWebServiceV3.findSearches(userToken);
    }

    public Observable<String> uploadPictureObserv(Long adId, String userToken, MultipartBody.Part file) {
        return mWebServiceV3.uploadPicture(adId, userToken, file);
    }

    public Observable<RowItem> saveNewAdObserv(String userToken, RowItem item) {
        return mWebServiceV3.saveNewAd(userToken, item);
    }

    public Observable<List<GroupedMsgItem>> getAllMessagesFromUserObserv(String userToken) {
        return mWebServiceV3.getAllMessagesFromUser(userToken);
    }

    public Observable<String> sendDeviceTokenObserv(String userToken, String deviceToken) {
        return mWebServiceV3.sendDeviceToken(userToken, deviceToken);
    }

    public Observable<String> deleteAdObserv(Integer adId, String userToken) {

        return mWebServiceV3.deleteAd(adId, userToken);
    }

    public Observable<String> sendNewMessageObserv(String message, Integer adId, String idTo, String userToken) {
        return mWebServiceV3.sendNewMessage(message, adId, idTo, userToken);
    }

    public Observable<String> delBookmarkAdObserv(Integer adId, String userToken) {
        return mWebServiceV3.delBookmarkAd(adId, userToken);
    }

    public Observable<String> bookmarkAdObserv(Integer adId, String userToken) {
        return mWebServiceV3.bookmarkAd(adId, userToken);
    }

    public Observable<String> increaseViewCount(Integer adId) {
        return mWebServiceV3.increaseViewCount(adId);
    }

    public Observable<AdsAsPage> getAdsMyObserv(int page, int size, String token) {
        return mWebServiceV3.getAdsMy(page, size, token);
    }

    public Observable<AdsAsPage> getMyBookmarkedAdsObserv(Double lat, Double lng, int page, int size, String token) {
        return mWebServiceV3.getMyBookmarkedAds(lat, lng, page, size, token);
    }

    public Observable<AdsAsPage> findAdsObserv(String description, Double lat, Double lng, int distance, Integer priceFrom, Integer priceTo, int page, int size, String userId) {
        return mWebServiceV3.findAds(description, lat, lng, distance, priceFrom, priceTo, page, size, userId);
    }

    public Observable<AdsAsPage> getAllAdsObserv(Double lat, Double lng, int page, int size) {
        return mWebServiceV3.getAllAds(lat, lng, Constants.DISTANCE_INFINITY, page, size);
    }

    public Observable<ArticleDetails> getAdDetailsObserv(Integer adId) {
        return mWebServiceV3.getAdDetails(adId);
    }

    public Observable<Long[]> getBookmarksForUserObserv(String userToken) {
        return mWebServiceV3.getBookmarksForUser(userToken);
    }

    public Observable<List<MsgRowItem>> getAllMessagesForAdObserv(String userToken, String sender, Integer adId) {
        return mWebServiceV3.getAllMessagesForAd(userToken, sender, adId);
    }
}
