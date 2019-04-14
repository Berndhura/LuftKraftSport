package de.wichura.lks.http;

import android.app.Application;
import android.content.res.Resources;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import de.wichura.lks.mainactivity.Constants;
import de.wichura.lks.models.AdsAsPage;
import de.wichura.lks.models.ApiError;
import de.wichura.lks.models.ArticleDetails;
import de.wichura.lks.models.GroupedMsgItem;
import de.wichura.lks.models.MsgRowItem;
import de.wichura.lks.models.RowItem;
import de.wichura.lks.models.SearchItem;
import de.wichura.lks.models.User;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
//import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Converter;
import retrofit2.Retrofit;

import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

import io.reactivex.Observable;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;


/**
 * Created by Bernd Wichura on 16.10.2016.
 * Luftkraftsport
 */

public class Service {

    private static final String WEB_SERVICE_BASE_URL_V3 = Urls.MAIN_SERVER_URL_V3;

    private WebService mWebServiceV3 = null;
    private Retrofit.Builder builder = null;
    private Retrofit restAdapterV2 = null;

    public Service() {

      //  HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
      //  logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        //api/V3
        OkHttpClient.Builder httpClientV3 = new OkHttpClient.Builder();

      //httpClientV3.addInterceptor(logging);

        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        //https
        SSLContext sslContext;
        TrustManager[] trustManagers;
        try {
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null, null);

            InputStream fis = null;
            try {
                //fis = new java.io.FileInputStream("/Users/digiconan/StudioProjects/LuftKraftSport/app/src/main/assets/lks.pem");
                fis = Resources.getSystem().getAssets().open( "lks.pem");
                String pw = "Bw12345!";
                char[] password = new char[] { 'B', 'w', '1', '2', '3', '4', '5', '!' };
                    keyStore.load(fis, password);
                } finally{
                    if (fis != null) {
                        fis.close();
                    }
                }



                //getAssets()  only with context possible
                //InputStream certInputStream = getAssets().open("lks.pem");


                BufferedInputStream bis = new BufferedInputStream(fis);
                CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
                while (bis.available() > 0) {
                    Certificate cert = certificateFactory.generateCertificate(bis);
                    keyStore.setCertificateEntry("www.luftkraftsport.de", cert);
                }
                TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                trustManagerFactory.init(keyStore);
                trustManagers = trustManagerFactory.getTrustManagers();
                sslContext = SSLContext.getInstance("TLS");
                sslContext.init(null, trustManagers, null);
            } catch (Exception e) {
                e.printStackTrace(); //TODO replace with real exception handling tailored to your needs
                return;
            }

            OkHttpClient client = new OkHttpClient.Builder()
                    .sslSocketFactory(sslContext.getSocketFactory(), (X509TrustManager) trustManagers[0])
                    .build();
            //https end




            builder = new Retrofit.Builder()
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .baseUrl(WEB_SERVICE_BASE_URL_V3)
                .client(client);

        restAdapterV2 = builder.build();

        mWebServiceV3 = restAdapterV2.create(WebService.class);
    }

    public Converter<ResponseBody, ApiError> getErrorConverter() {
        return restAdapterV2.responseBodyConverter(ApiError.class, new Annotation[0]);
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

        @DELETE("articles/{articleId}/{pictureId}/deletePicture")
        Observable<String> deletePicture(@Path("articleId") Long articleId,
                                         @Path("pictureId") Long pictureId,
                                         @Query("token") String userToken);

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
                                      @Query("distance") Integer distance,
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

    public Observable<String> deletePictureObserv(Long articleId, Long pictureId, String userToken) {
        return mWebServiceV3.deletePicture(articleId, pictureId, userToken);
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
                                               Integer distance,
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
