package de.wichura.lks.http;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Created by Bernd Wichura on 06.06.2017.
 * Luftkraftsport
 */

public class EbayRestService {

    private static final String EBAY_REST_URL_BASE = "https://svcs.ebay.com/";


    private static final String EBAY_REST_URL = EBAY_REST_URL_BASE;

    private final EbayRestService.WebService mEbayRestService;

    public EbayRestService() {

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient.Builder httpEbayClient = new OkHttpClient.Builder();

        httpEbayClient.addInterceptor(logging);

        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        Retrofit restAdapterV2 = new Retrofit.Builder()
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .baseUrl(EBAY_REST_URL)
                .client(httpEbayClient.build())
                .build();

        mEbayRestService = restAdapterV2.create(EbayRestService.WebService.class);
    }

    private interface WebService {

       /*
       https://svcs.ebay.com/services/search/FindingService/v1?
       SECURITY-APPNAME=berndwic-luftkraf-PRD-c7a9b454f-48afe28f&
       OPERATION-NAME=findItemsByKeywords&
       SERVICE-VERSION=1.0.0&
       RESPONSE-DATA-FORMAT=JSON&
       callback=_cb_findItemsByKeywords&
       REST-PAYLOAD&
       keywords=tabou 3s&
       paginationInput.entriesPerPage=6&
       GLOBAL-ID=EBAY-DE&
       siteid=77
        */

        @GET("services/search/FindingService/v1")
        Observable<JsonObject> findItemsByKeyword(@Query("SECURITY-APPNAME") String security,
                                                  @Query("OPERATION-NAME") String findItemsByKeywords,
                                                  @Query("SERVICE-VERSION") String serviceVersion,
                                                  @Query("RESPONSE-DATA-FORMAT") String dataFormat,
                                                 // @Query("callback") String callback,
                                                  @Query("REST-PAYLOAD") String payload,
                                                  @Query("keywords") String keywords,
                                                  @Query("paginationInput.entriesPerPage") String pages,
                                                  @Query("GLOBAL-ID") String globalId,
                                                  @Query("siteid") String siteId);
    }

    public Observable<JsonObject> findItemsByKeywordObersv(String keywords) {
        return mEbayRestService.findItemsByKeyword("berndwic-luftkraf-PRD-c7a9b454f-48afe28f",
                "findItemsByKeywords",
                "1.0.0",
                "JSON",
                //"_cb_findItemsByKeywords",
                 "",
                keywords,
                "100",
                "EBAY-DE",
                "77");
    }
}