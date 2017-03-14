package de.wichura.lks.http;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Created by ich on 18.02.2017.
 * deSurf
 */

public class GoogleService {

    private static final String GOOGLE_WEB = Urls.GOOGLE_MAPS_URL;

    private final GoogleService.WebService mGoogleWebService;

    public GoogleService() {

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        //Google api
        OkHttpClient.Builder httpGoogleClient = new OkHttpClient.Builder();

        httpGoogleClient.addInterceptor(logging);

        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        Retrofit restAdapterV2 = new Retrofit.Builder()
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .baseUrl(GOOGLE_WEB)
                .client(httpGoogleClient.build())
                .build();

        mGoogleWebService = restAdapterV2.create(GoogleService.WebService.class);
    }

    private interface WebService {

        @GET("geocode/json")
        Observable<JsonObject> getCityNameFrimLatLng(@Query("latlng") String latlng,
                                                     @Query("sensor") Boolean sensor);
    }

    public Observable<JsonObject> getCityNameFrimLatLngObserv(Double lat, Double lng, Boolean sensor) {
        String latlng = lat + "," + lng;
        return mGoogleWebService.getCityNameFrimLatLng(latlng, sensor);
    }
}
