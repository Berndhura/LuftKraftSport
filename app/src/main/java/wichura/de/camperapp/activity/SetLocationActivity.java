package wichura.de.camperapp.activity;

import android.Manifest;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import wichura.de.camperapp.R;
import wichura.de.camperapp.mainactivity.Constants;
import wichura.de.camperapp.presentation.LocationPresenter;

import static wichura.de.camperapp.R.id.map;

/**
 * Created by ich on 17.02.2017.
 * deSurf
 */

public class SetLocationActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener, OnMapReadyCallback {

    private GoogleMap googleMap;
    private GoogleApiClient mGoogleApiClient;

    private LinearLayout mainLinearLayout;
    private LinearLayout distanceView;

    private LocationPresenter presenter;

    private SeekBar seekBar;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.set_location_layout);

        mainLinearLayout = (LinearLayout) findViewById(R.id.location_main_linear_layout);
        distanceView = (LinearLayout) findViewById(R.id.location_distance_view);
        distanceView.setVisibility(View.GONE);

        MapsInitializer.initialize(this);

        presenter = new LocationPresenter(getApplicationContext(), this);

        switch (GooglePlayServicesUtil.isGooglePlayServicesAvailable(this)) {
            case ConnectionResult.SUCCESS: {
                MapView mapView = (MapView) findViewById(map);
                mapView.onCreate(savedInstanceState);
                mapView.onResume();
                mapView.getMapAsync(this);
            }
            case ConnectionResult.INVALID_ACCOUNT: {
                Log.d("CONAN", "Google play service: ConnectionResult.INVALID_ACCOUNT");
            }
            case ConnectionResult.NETWORK_ERROR: {
                Log.d("CONAN", "Google play service: ConnectionResult.NETWORK_ERROR");
            }
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.location_toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            toolbar.setNavigationOnClickListener((view) -> finish());
        }

        buildGoogleApiClient();
        mGoogleApiClient.connect();


    }

    private void initDistanceSeekBar() {
        distanceView.setVisibility(View.VISIBLE);

        TextView textView = (TextView) findViewById(R.id.textView9);

        seekBar = (SeekBar) findViewById(R.id.distance_seek_bat);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                adaptToolbar(seekBar.getProgress() * 5);
                CircleOptions circleOptions = new CircleOptions()
                        .center(new LatLng(getLat(), getLng()))
                        .radius(seekBar.getProgress() * 5000); // In meters

                // Get back the mutable Circle
                Circle circle = googleMap.addCircle(circleOptions);
                circle.setVisible(true);
                storeDistance(seekBar.getProgress() * 5);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                googleMap.clear();
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(new LatLng(getLat(), getLng()));
                markerOptions.title(getIntent().getStringExtra(Constants.TITLE));
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                googleMap.addMarker(markerOptions);
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                Integer distance = progress * 5;
                textView.setText("Umkreis: " + String.valueOf(distance) + " km");
            }
        });
    }

    private void adaptToolbar(int progress) {
        getSupportActionBar().setSubtitle("im Umkreis von " + progress + " km");
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE);
        // Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
        //       mGoogleApiClient);
        //if (mLastLocation != null) {
        //place marker at current position

        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(54.0, 13.0), 9));
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setScrollGesturesEnabled(true);
        googleMap.getUiSettings().setCompassEnabled(true);
        googleMap.getUiSettings().setMyLocationButtonEnabled(true);
        googleMap.setMyLocationEnabled(true);
        googleMap.clear();

        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick(LatLng latLng) {
                googleMap.clear();
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(latLng);
                markerOptions.title(getIntent().getStringExtra(Constants.TITLE));
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                googleMap.addMarker(markerOptions);
                //save position in shared preferences
                presenter.saveUsersLocation(latLng.latitude, latLng.longitude);
                initDistanceSeekBar();
            }
        });

        //click on marker, not on map
        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                LatLng position = marker.getPosition();

                Toast.makeText(
                        getApplicationContext(),
                        "Lat " + position.latitude + " "
                                + "Long " + position.longitude,
                        Toast.LENGTH_LONG).show();
                return true;
            }
        });

        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(5000); //5 seconds
        mLocationRequest.setFastestInterval(3000); //3 seconds
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        //mLocationRequest.setSmallestDisplacement(0.1F); //1/10 meter

        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

    }

    public void updateCity(String cityName) {
        getSupportActionBar().setTitle(cityName);
    }

    protected synchronized void buildGoogleApiClient() {
        //Toast.makeText(this, "buildGoogleApiClient", Toast.LENGTH_SHORT).show();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }


    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d("CONAN", "location changed");
        Double lat = location.getLatitude();
        Double lng = location.getLongitude();
        presenter.saveUsersLocation(lat, lng);
    }

    public Double getLng() {
        SharedPreferences settings = getApplicationContext().getSharedPreferences(Constants.USERS_LOCATION, 0);
        return Double.longBitsToDouble(settings.getLong(Constants.LNG, 0));
    }

    public Double getLat() {
        SharedPreferences settings = getApplicationContext().getSharedPreferences(Constants.USERS_LOCATION, 0);
        return  Double.longBitsToDouble(settings.getLong(Constants.LAT, 0));
    }

    private void storeDistance(int distance) {

        SharedPreferences sp = getApplicationContext().getSharedPreferences(Constants.USERS_LOCATION, MODE_PRIVATE);
        SharedPreferences.Editor ed = sp.edit();
        ed.putInt(Constants.DISTANCE, distance);
        ed.apply();
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
    }
}
