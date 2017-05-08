package de.wichura.lks.activity;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
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
import com.google.android.gms.maps.model.MarkerOptions;

import de.wichura.lks.R;
import de.wichura.lks.mainactivity.Constants;
import de.wichura.lks.presentation.LocationPresenter;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by Bernd Wichura on 11.03.2017.
 * Luftkraftsport
 */

public class LocationFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener, OnMapReadyCallback {

    private GoogleMap mGoogleMap;
    private GoogleApiClient mGoogleApiClient;
    private LinearLayout distanceView;
    private LocationPresenter presenter;
    private MapView mapView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.set_location_layout, container, false);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) {
            //Restore the fragment's state here
            Log.d("CONAN", "restore.....");
        }
    }

    public void onSaveInstanceState(Bundle outState) {
        //This MUST be done before saving any of your own or your base class's variables
        final Bundle mapViewSaveState = new Bundle(outState);
        mapView.onSaveInstanceState(mapViewSaveState);
        outState.putBundle("mapViewSaveState", mapViewSaveState);
        //Add any other variables here.
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        mapView = (MapView) view.findViewById(R.id.location_google_map);

        final Bundle mapViewSavedInstanceState = savedInstanceState != null ? savedInstanceState.getBundle("mapViewSaveState") : null;
        if (mapView != null)
            mapView.onCreate(mapViewSavedInstanceState);

        distanceView = (LinearLayout) view.findViewById(R.id.location_distance_view);
        distanceView.setVisibility(View.GONE);

        MapsInitializer.initialize(getActivity());

        presenter = new LocationPresenter(getActivity().getApplicationContext(), (SearchActivity) getActivity());

        switch (GooglePlayServicesUtil.isGooglePlayServicesAvailable(getActivity())) {
            case ConnectionResult.SUCCESS: {
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

        setupMapIfNeeded(view);

        Toolbar toolbar = (Toolbar) view.findViewById(R.id.location_toolbar);
        if (toolbar != null) {
            ((SearchActivity) getActivity()).setSupportActionBar(toolbar);
        }

        ImageView closeLocation = (ImageView) view.findViewById(R.id.close_location_map);
        closeLocation.setOnClickListener((v) -> {
            //getFragmentManager().popBackStackImmediate();
            android.support.v4.app.FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.layout, new SearchFragment());
            fragmentTransaction.commit();
        });
        closeLocation.setVisibility(View.VISIBLE);

        buildGoogleApiClient();
        mGoogleApiClient.connect();
    }

    private void setupMapIfNeeded(View view) {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        if (mGoogleMap == null) {
            MapView mapView = (MapView) view.findViewById(R.id.location_google_map);
            mapView.getMapAsync(this);
        }
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
                Circle circle = mGoogleMap.addCircle(circleOptions);
                circle.setVisible(true);
                storeDistance((seekBar.getProgress() == 100) ? Constants.DISTANCE_INFINITY : seekBar.getProgress() * 5000);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mGoogleMap.clear();
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(new LatLng(getLat(), getLng()));
                //markerOptions.title(getIntent().getStringExtra(Constants.TITLE));
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                mGoogleMap.addMarker(markerOptions);

                //TODO zoom level auf umkreis automatisch anpassen
                //double  meters_per_pixel = 156543.03392 * Math.cos(getLat() * Math.PI / 180) / Math.pow(2, 11);
                //int zoom = 11;
                //mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(getLat(), getLng()), zoom));
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
        ((SearchActivity) getActivity()).getSupportActionBar();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        int accessCoarseLoc = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION);

        if (accessCoarseLoc == PackageManager.PERMISSION_DENIED) {

            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, Constants.REQUEST_ID_FOR_LOCATION_PERMISSION);

        } else {
            //TODO Kamera position -> anpassen
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(54.0, 13.0), 9));
            mGoogleMap.getUiSettings().setZoomControlsEnabled(true);
            mGoogleMap.getUiSettings().setScrollGesturesEnabled(true);
            mGoogleMap.getUiSettings().setCompassEnabled(true);
            mGoogleMap.getUiSettings().setMyLocationButtonEnabled(true);
            mGoogleMap.setMyLocationEnabled(true);
            mGoogleMap.clear();

            mGoogleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

                @Override
                public void onMapClick(LatLng latLng) {
                    mGoogleMap.clear();
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(latLng);
                    //markerOptions.title(getIntent().getStringExtra(Constants.TITLE));
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                    mGoogleMap.addMarker(markerOptions);
                    //save position in shared preferences
                    presenter.saveUsersLocation(latLng.latitude, latLng.longitude);
                    initDistanceSeekBar();
                }
            });
        }
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
        Log.d("CONAN", "store search distance: " + distance);
        SharedPreferences sp = getActivity().getSharedPreferences(Constants.USERS_LOCATION, MODE_PRIVATE);
        SharedPreferences.Editor ed = sp.edit();
        ed.putInt(Constants.DISTANCE, distance);
        ed.apply();
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.mGoogleMap = googleMap;
    }
}