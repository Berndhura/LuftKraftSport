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
import wichura.de.camperapp.models.AdsAsPage;
import wichura.de.camperapp.models.ArticleDetails;
import wichura.de.camperapp.models.GroupedMsgItem;
import wichura.de.camperapp.models.MsgRowItem;
import wichura.de.camperapp.models.RowItem;
import wichura.de.camperapp.models.SearchItem;

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
        @GET("bookmarkIds")
        Observable<Long[]> getBookmarksForUser(@Query("token") String userToken);

        @GET("messages/forArticle")
        Observable<List<MsgRowItem>> getAllMessagesForAd(
                @Query("token") String userToken,
                @Query("sender") String chatPartner,
                @Query("articleId") int articleId);

        @GET("articles/{articleId}")
        Observable<ArticleDetails> getAdDetails(@Path("articleId") Integer articleId);

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

        @GET("search")
        Observable<List<SearchItem>> findSearches(@Query("token") String userToken);

        @DELETE("search/{id}")
        Observable<String> deleteSearch(@Path("id") Long id, @Query("token") String token);

        /*
        @RequestMapping(value = "/search/new", method = RequestMethod.POST)
@ResponseBody
public String saveSearch(
@RequestParam(value = "description", required = false) String description,
@RequestParam(value = "priceFrom", required = false) Integer priceFrom,
@RequestParam(value = "priceTo", required = false) Integer priceTo,
@RequestParam(value = "lat", required = false) Float latitude,
@RequestParam(value = "lng", required = false) Float longitude,
@RequestParam(value = "distance", required = false) Integer distance,
@RequestParam("token") String token)
         */
    }

    public Observable<String> deleteSearchesObserv(Long id, String userToken) {
        return mWebServiceV3.deleteSearch(id, userToken);
    }

    public Observable<List<SearchItem>> findSearchesObserv(String userToken) {
        return mWebServiceV3.findSearches(userToken);
    }

    public Observable<String> uploadPictureObserv(Integer adId, String userToken, MultipartBody.Part file) {
        return mWebServiceV3.uploadPicture(adId, userToken, file);
    }

    public Observable<RowItem> saveNewAdObserv(String userToken, RowItem item) {
        return mWebServiceV3.saveNewAd(userToken, item);
    }

    public Observable<List<GroupedMsgItem>> getAllMessagesFromUserObserv(String userToken) {
        return mWebServiceV3.getAllMessagesFromUser(userToken);
    }

    public Observable<String> loginUserObserv(String email, String password) {
        return mWebServiceV3.loginUser(email, password);
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

    public Observable<AdsAsPage> getMyBookmarkedAdsObserv(int page, int size, String token) {
        return mWebServiceV3.getMyBookmarkedAds(page, size, token);
    }

    public Observable<AdsAsPage> findAdsObserv(String description, int priceFrom, int priceTo, int page, int size) {
        return mWebServiceV3.findAds(description, priceFrom, priceTo, page, size);
    }

    public Observable<AdsAsPage> getAllAdsObserv(int page, int size) {
        return mWebServiceV3.getAllAds(page, size);
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
