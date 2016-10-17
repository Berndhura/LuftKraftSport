package wichura.de.camperapp.http;

import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.http.GET;
import retrofit.http.Query;
import rx.Observable;

/**
 * Created by ich on 16.10.2016.
 */

public class Service {

    private static final String WEB_SERVICE_BASE_URL = "http://api.openweathermap.org/data/2.5";
    private static final String API_KEY = "insert your api key here";
    private final OpenWeatherMapWebService mWebService;

    public Service() {
        RequestInterceptor requestInterceptor = new RequestInterceptor() {
            @Override
            public void intercept(RequestInterceptor.RequestFacade request) {
                request.addHeader("Accept", "application/json");
            }
        };

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(WEB_SERVICE_BASE_URL)
                .setRequestInterceptor(requestInterceptor)
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .build();

        mWebService = restAdapter.create(OpenWeatherMapWebService.class);
    }

    private interface OpenWeatherMapWebService {
        @GET("/weather?units=metric&apikey=" + API_KEY)
        Observable<String> fetchCurrentWeather(@Query("lon") double longitude,
                                                                   @Query("lat") double latitude);

        @GET("/forecast/daily?units=metric&cnt=7&apikey=" + API_KEY)
        Observable<String> fetchWeatherForecasts(
                @Query("lon") double longitude, @Query("lat") double latitude);
    }
}
