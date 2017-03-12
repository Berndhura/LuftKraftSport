package wichura.de.camperapp.activity;

import android.Manifest;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import static android.content.Context.MODE_PRIVATE;
import static wichura.de.camperapp.R.id.map;

/**
 * Created by ich on 11.03.2017.
 * LKS
 */

public class LocationFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener, OnMapReadyCallback {

    private GoogleMap googleMap;
    private GoogleApiClient mGoogleApiClient;
    private LinearLayout distanceView;
    private LocationPresenter presenter;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.set_location_layout, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        distanceView = (LinearLayout) view.findViewById(R.id.location_distance_view);
        distanceView.setVisibility(View.GONE);

        MapsInitializer.initialize(getActivity());

        presenter = new LocationPresenter(getActivity().getApplicationContext(), (SearchActivity) getActivity());

        switch (GooglePlayServicesUtil.isGooglePlayServicesAvailable(getActivity())) {
            case ConnectionResult.SUCCESS: {
                MapView mapView = (MapView) view.findViewById(map);
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

        Toolbar toolbar = (Toolbar) view.findViewById(R.id.location_toolbar);
        if (toolbar != null) {
            ((SearchActivity) getActivity()).setSupportActionBar(toolbar);
            ((SearchActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            ((SearchActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);
            toolbar.setNavigationOnClickListener((v) -> getActivity().finish());
        }

        buildGoogleApiClient();
        mGoogleApiClient.connect();


    }

    private void initDistanceSeekBar() {
        distanceView.setVisibility(View.VISIBLE);

        TextView textView = (TextView) getView().findViewById(R.id.textView9);

        SeekBar seekBar = (SeekBar) getView().findViewById(R.id.distance_seek_bat);
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
                storeDistance((seekBar.getProgress() == 100) ? Constants.DISTANCE_INFINITY : seekBar.getProgress() * 5000);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                googleMap.clear();
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(new LatLng(getLat(), getLng()));
                //markerOptions.title(getIntent().getStringExtra(Constants.TITLE));
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                googleMap.addMarker(markerOptions);
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                Integer distance = progress * 5;
                textView.setText(((distance == 500) ? "Unbegrenzt" : ("Umkreis: " + String.valueOf(distance)) + " km"));
            }
        });
    }

    private void adaptToolbar(int progress) {
        ((SearchActivity) getActivity()).getSupportActionBar().setSubtitle(((progress == 500) ? "Unbegrenzt" : ("Im Umkreis: " + String.valueOf(progress)) + " km"));
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_NETWORK_STATE);
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
                //markerOptions.title(getIntent().getStringExtra(Constants.TITLE));
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
                        getActivity(),
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
        ((SearchActivity) getActivity()).getSupportActionBar().setTitle(cityName);
    }

    protected synchronized void buildGoogleApiClient() {
        //Toast.makeText(this, "buildGoogleApiClient", Toast.LENGTH_SHORT).show();
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
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
        SharedPreferences settings = getActivity().getSharedPreferences(Constants.USERS_LOCATION, 0);
        return Double.longBitsToDouble(settings.getLong(Constants.LNG, 0));
    }

    public Double getLat() {
        SharedPreferences settings = getActivity().getSharedPreferences(Constants.USERS_LOCATION, 0);
        return Double.longBitsToDouble(settings.getLong(Constants.LAT, 0));
    }

    private void storeDistance(int distance) {

        SharedPreferences sp = getActivity().getSharedPreferences(Constants.USERS_LOCATION, MODE_PRIVATE);
        SharedPreferences.Editor ed = sp.edit();
        ed.putInt(Constants.DISTANCE, distance);
        ed.apply();
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
    }
}