package wichura.de.camperapp.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Display;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
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
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;

import wichura.de.camperapp.R;
import wichura.de.camperapp.http.Service;
import wichura.de.camperapp.mainactivity.Constants;
import wichura.de.camperapp.presentation.OpenAdPresenter;

import static wichura.de.camperapp.R.id.map;
import static wichura.de.camperapp.mainactivity.Constants.SHARED_PREFS_USER_INFO;

public class OpenAdActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener, OnMapReadyCallback {

    // private static double longitute;
    //private static double latitude;
    public Button mBookmarkButton;
    private int mAdId;

    private int displayHeight;
    private int displayWidth;
    public boolean isBookmarked;

    private ProgressBar mOpenAdProgressBar;

    private OpenAdPresenter presenter;

    private MapView mapView;
    private GoogleMap googleMap;

    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;

    private LatLng latLng;
    private Marker mCurrLocation;

    private double lat;
    private double lng;


    @Override
    protected void onCreate(final Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.open_ad_activity);

        MapsInitializer.initialize(this);

        switch (GooglePlayServicesUtil.isGooglePlayServicesAvailable(this)) {
            case ConnectionResult.SUCCESS:
                Toast.makeText(this, "SUCCESS", Toast.LENGTH_SHORT).show();
                mapView = (MapView) findViewById(map);
                mapView.onCreate(savedInstanceState);
                mapView.onResume();
                mapView.getMapAsync(this);
        }


        presenter = new OpenAdPresenter(this, new Service(), getApplicationContext());

        Toolbar toolbar = (Toolbar) findViewById(R.id.open_ad_toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            toolbar.setNavigationOnClickListener((view) -> finish());
        }

        mOpenAdProgressBar = (ProgressBar) findViewById(R.id.open_Ad_ProgressBar);

        getDisplayDimensions();

        TextView mTitleText = (TextView) findViewById(R.id.title);
        TextView mPrice = (TextView) findViewById(R.id.price);
        TextView mDescText = (TextView) findViewById(R.id.description);
        TextView mDateText = (TextView) findViewById(R.id.ad_date);
        ImageView imgView = (ImageView) findViewById(R.id.imageView);
        Button mDelAndMsgButton = (Button) findViewById(R.id.delButton);

        //get data from Intent
        String pictureUri = getIntent().getStringExtra(Constants.URI);
        mTitleText.setText(getIntent().getStringExtra(Constants.TITLE));
        mPrice.setText(getIntent().getStringExtra(Constants.PRICE));
        mDescText.setText(getIntent().getStringExtra(Constants.DESCRIPTION));
        mDateText.setText(DateFormat.getDateInstance().format(getIntent().getLongExtra(Constants.DATE, 0)));
        mAdId = getIntent().getIntExtra(Constants.ID, 0);

        lat = getIntent().getDoubleExtra(Constants.LAT, 0);
        lng = getIntent().getDoubleExtra(Constants.LNG, 0);


        mBookmarkButton = (Button) findViewById(R.id.bookmarkButton);
        mBookmarkButton.setClickable(false);
        //loadBookmarks for user
        presenter.loadBookmarksForUser();

        presenter.increaseViewCount(mAdId);

        int ratio = Math.round((float) displayWidth / (float) displayWidth);

        Picasso.with(getApplicationContext())
                .load(pictureUri)
                .placeholder(R.drawable.empty_photo)
                .resize((int) Math.round((float) displayWidth * 0.6), (int) Math.round((float) displayHeight * 0.6) * ratio)
                .centerCrop()
                .into(imgView, new Callback() {
                    @Override
                    public void onSuccess() {
                        mOpenAdProgressBar.setVisibility(ProgressBar.GONE);
                    }

                    @Override
                    public void onError() {
                        mOpenAdProgressBar.setVisibility(ProgressBar.GONE);
                        Toast.makeText(getApplicationContext(), "No network connection!", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });

        if (isOwnAd() && mDelAndMsgButton != null) {
            mDelAndMsgButton.setOnClickListener((view) -> {
                //get ad id and send delete request
                String adId = getIntent().getStringExtra(Constants.ARTICLE_ID);
                Log.i("CONAN", "AdId: " + mAdId);
                deleteAdRequest(adId);
            });
        } else {
            mDelAndMsgButton.setText("Send message");
            mDelAndMsgButton.setOnClickListener((view) -> {

                if (!getUserId().equals("")) {
                    //send a message to ad owner
                    String adId = getIntent().getStringExtra(Constants.ARTICLE_ID);
                    String ownerId = getIntent().getStringExtra(Constants.USER_ID_FROM_AD);
                    String sender = getUserId();
                    sendMessage(adId, ownerId, sender);
                } else {
                    final Intent facebookIntent = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivityForResult(facebookIntent, Constants.REQUEST_ID_FOR_LOGIN);
                }
            });
        }

        mBookmarkButton.setOnClickListener((view) -> {
            String adId = getIntent().getStringExtra(Constants.ARTICLE_ID);
            if (isBookmarked) {
                presenter.deleteBookmark(adId, getUserToken());
            } else {
                presenter.bookmarkAd(adId, getUserToken());
            }
        });

        Log.d("CONAN", "request Picture: " + pictureUri);

        //map fragment in app : https://developers.google.com/maps/documentation/android-api/start#die_xml-layoutdatei
        //TODO: show location on map fragment
        //TODO: get LatLng in JSON from  http://maps.google.com/maps/api/geocode/json?address=%22greifswald%22&sensor=false
        //getLocationInfo()
        //now get Lat and Lng  from  getLatLong()
        // this:  http://stackoverflow.com/questions/3574644/how-can-i-find-the-latitude-and-longitude-from-address
        buildGoogleApiClient();
        mGoogleApiClient.connect();

    }

    protected synchronized void buildGoogleApiClient() {
        Toast.makeText(this, "buildGoogleApiClient", Toast.LENGTH_SHORT).show();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }


    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;
    }

    public void updateBookmarkButton(String[] bookmark) {
        isBookmarked = false;
        ArrayList<String> bookmarkList = new ArrayList<>(Arrays.asList(bookmark));
        if (bookmarkList.contains(mAdId)) {
            mBookmarkButton.setText("Remove bookmark!");
            mBookmarkButton.setClickable(true);
            isBookmarked = true;
        } else {
            mBookmarkButton.setText("Bookmark");
            mBookmarkButton.setClickable(true);
        }
    }

    private void sendMessage(final String adId, final String receiverId, final String sender) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        final EditText edittext = new EditText(OpenAdActivity.this);
        alert.setTitle("Send a message");
        alert.setView(edittext);
        alert.setPositiveButton("Send", (dialog, whichButton) -> {
            String message = edittext.getText().toString();
            presenter.sendNewMessage(message, adId, receiverId, getUserToken());
        });
        alert.setNegativeButton("not yet", (dismissDialog, whichButton) -> {/*just go away*/});
        alert.show();
    }

    private void deleteAdRequest(final String adId) {
        AlertDialog myQuittingDialogBox = new AlertDialog.Builder(this)
                .setTitle("Delete Ad")
                .setMessage("Do you want to delete this ad?")
                .setIcon(R.drawable.delete)
                .setPositiveButton("Delete", (dialog, whichButton) -> {
                    presenter.deleteAd(adId);
                    dialog.dismiss();
                })
                .setNegativeButton("cancel", (dialog, whichButton) -> dialog.dismiss())
                .create();
        myQuittingDialogBox.show();
    }

    private String getUserId() {
        return getSharedPreferences(SHARED_PREFS_USER_INFO, 0).getString(Constants.USER_ID, "");
    }

    private String getUserToken() {
        return getSharedPreferences(SHARED_PREFS_USER_INFO, 0).getString(Constants.USER_TOKEN, "");
    }

    private boolean isOwnAd() {
        return getIntent().getStringExtra(Constants.USER_ID_FROM_AD).equals(getIntent().getStringExtra(Constants.USER_ID));
    }

    private void getDisplayDimensions() {

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        Log.i("CONAN", "X " + size.x);
        Log.i("CONAN", "Y " + size.y);

        displayWidth = size.x;
        displayHeight = size.y;
    }

    public Context getContext() {
        return getApplicationContext();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Toast.makeText(this, "onConnected", Toast.LENGTH_SHORT).show();
        ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE);
       // Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
        //       mGoogleApiClient);
        //if (mLastLocation != null) {
            //place marker at current position
            googleMap.clear();
            latLng = new LatLng(lat, lng);
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLng);
            markerOptions.title("Current Position");
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
            mCurrLocation = googleMap.addMarker(markerOptions);
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), 11));
            googleMap.getUiSettings().setZoomControlsEnabled(true);
            googleMap.getUiSettings().setScrollGesturesEnabled(true);
            googleMap.getUiSettings().setCompassEnabled(true);
            googleMap.getUiSettings().setMyLocationButtonEnabled(true);

           /* CircleOptions circleOptions = new CircleOptions()
                    .center(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()))
                    .radius(1000); // In meters

            // Get back the mutable Circle
            Circle circle = googleMap.addCircle(circleOptions);
            circle.setVisible(true);*/
       // }

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(5000); //5 seconds
        mLocationRequest.setFastestInterval(3000); //3 seconds
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        //mLocationRequest.setSmallestDisplacement(0.1F); //1/10 meter

        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Toast.makeText(this, "onConnectionSuspended", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, "onConnectionFailed", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLocationChanged(Location location) {

    }




     /* public static boolean getLatLong(JSONObject jsonObject) {
        try {
            longitute = ((JSONArray) jsonObject.get("results")).getJSONObject(0)
                    .getJSONObject("geometry").getJSONObject("location")
                    .getDouble("lng");

            latitude = ((JSONArray) jsonObject.get("results")).getJSONObject(0)
                    .getJSONObject("geometry").getJSONObject("location")
                    .getDouble("lat");
        } catch (JSONException e) {
            return false;
        }
        return true;
    }*/


   /* public static JSONObject getLocationInfo(String address) {
        StringBuilder stringBuilder = new StringBuilder();
        try {

            address = address.replaceAll(" ", "%20");

            HttpPost httppost = new HttpPost("http://maps.google.com/maps/api/geocode/json?address=" + address + "&sensor=false");
            HttpClient client = new DefaultHttpClient();
            HttpResponse response;
            stringBuilder = new StringBuilder();


            response = client.execute(httppost);
            HttpEntity entity = response.getEntity();
            InputStream stream = entity.getContent();
            int b;
            while ((b = stream.read()) != -1) {
                stringBuilder.append((char) b);
            }
        } catch (ClientProtocolException e) {
        } catch (IOException e) {
        }

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject = new JSONObject(stringBuilder.toString());
        } catch (JSONException e) {

            e.printStackTrace();
        }

        return jsonObject;
    }*/
}
