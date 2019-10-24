package de.wichura.lks.http;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import io.reactivex.Observable;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by Bernd Wichura on 18.02.2017.
 * Luftkraftsport
 */

public class GoogleService {

    private static final String GOOGLE_WEB = Urls.GOOGLE_MAPS_URL;

    private final GoogleService.WebService mGoogleWebService;

    public GoogleService() {

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BASIC);

        //Google api
        OkHttpClient.Builder httpGoogleClient = new OkHttpClient.Builder();


        httpGoogleClient.addInterceptor(logging);

        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        Retrofit restAdapterV2 = new Retrofit.Builder()
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .baseUrl(GOOGLE_WEB)
                .client(httpGoogleClient.build())
                .build();

        mGoogleWebService = restAdapterV2.create(GoogleService.WebService.class);
    }

    private interface WebService {

        @GET("geocode/json")
        Observable<JsonObject> getCityNameFromLatLng(@Query("latlng") String latlng,
                                                     @Query("sensor") Boolean sensor);

        @GET("json")
        Observable<JsonObject> getLatLngFromAddress(@Query("address") String address,
                                                    @Query("key") String api_key);
    }

    public Observable<JsonObject> getCityNameFromLatLngObserable(Double lat, Double lng, Boolean sensor) {
        String latlng = lat + "," + lng;
        return mGoogleWebService.getCityNameFromLatLng(latlng, sensor);
    }

    public Observable<JsonObject> getLatLngFromAddressObservable(String address) {
        return mGoogleWebService.getLatLngFromAddress(address,
                "AIzaSyA8x3Il4-xgNij1FSOxQkMYX4_1bzVo12k");
    }
}
