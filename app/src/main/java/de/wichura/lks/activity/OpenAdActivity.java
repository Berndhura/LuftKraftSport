package de.wichura.lks.activity;

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
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;

import de.wichura.lks.R;
import de.wichura.lks.http.Service;
import de.wichura.lks.http.Urls;
import de.wichura.lks.mainactivity.Constants;
import de.wichura.lks.mainactivity.MainActivity;
import de.wichura.lks.models.ArticleDetails;
import de.wichura.lks.models.User;
import de.wichura.lks.presentation.OpenAdPresenter;
import de.wichura.lks.util.Utility;

import static de.wichura.lks.R.id.map;
import static de.wichura.lks.mainactivity.Constants.SHARED_PREFS_USER_INFO;

public class OpenAdActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener, OnMapReadyCallback {

    public Button mBookmarkButton;
    public boolean isBookmarked;

    private Utility utils;

    private int displayHeight;
    private int displayWidth;

    private ProgressBar mOpenAdProgressBar;
    private OpenAdPresenter presenter;
    private GoogleMap googleMap;
    private GoogleApiClient mGoogleApiClient;

    private double lat;
    private double lng;
    private ImageView imgView;
    private ImageView userPic;
    private TextView userName;
    private TextView userNumberOfArticles;
    private Button mDelAndMsgButton;
    private TextView mTitleText;
    private TextView mPrice;
    private TextView mDescText;
    private TextView mDateText;
    private Integer mAdId;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.open_ad_activity);

        utils = new Utility(this);

        MapsInitializer.initialize(this);

        switch (GooglePlayServicesUtil.isGooglePlayServicesAvailable(this)) {
            case ConnectionResult.SUCCESS: {
                //Toast.makeText(this, "SUCCESS", Toast.LENGTH_SHORT).show();
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

        presenter = new OpenAdPresenter(this, new Service(), getApplicationContext());

        Toolbar toolbar = (Toolbar) findViewById(R.id.open_ad_toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            toolbar.setNavigationOnClickListener((view) -> finish());
        }

        mOpenAdProgressBar = (ProgressBar) findViewById(R.id.open_Ad_ProgressBar);
        mTitleText = (TextView) findViewById(R.id.title);
        mPrice = (TextView) findViewById(R.id.price);
        mDescText = (TextView) findViewById(R.id.description);
        mDateText = (TextView) findViewById(R.id.ad_date);
        imgView = (ImageView) findViewById(R.id.imageView);
        mDelAndMsgButton = (Button) findViewById(R.id.delButton);
        mBookmarkButton = (Button) findViewById(R.id.bookmarkButton);
        userName = (TextView) findViewById(R.id.user_name);
        userNumberOfArticles = (TextView) findViewById(R.id.user_number_of_articles);
        userPic = (ImageView) findViewById(R.id.user_image);

        getDisplayDimensions();

        //get data from Intent
        if (!"article".equals(getIntent().getStringExtra(Constants.NOTIFICATION_TYPE))) {
            //intent comes from article overview
            String pictureUri = getIntent().getStringExtra(Constants.URI);
            mTitleText.setText(getIntent().getStringExtra(Constants.TITLE));
            String formatedPrice = getIntent().getStringExtra(Constants.PRICE).split("\\.")[0] + " €";
            mPrice.setText(formatedPrice);
            mDescText.setText(getIntent().getStringExtra(Constants.DESCRIPTION));
            mDateText.setText("Erstellt am: " + DateFormat.getDateInstance().format(getIntent().getLongExtra(Constants.DATE, 0)));
            mAdId = getIntent().getIntExtra(Constants.ID, 0);
            lat = getIntent().getDoubleExtra(Constants.LAT, 0);
            lng = getIntent().getDoubleExtra(Constants.LNG, 0);
            String ownerId = getIntent().getStringExtra(Constants.USER_ID_FROM_AD);
            setupPanel(pictureUri, ownerId);
        } else {
            //intent comes from notification -> get article first
            presenter.getAd(getIntent().getIntExtra(Constants.ID, 0));
            Log.e("CONAN", "articlevorschlag");
        }

        if (!"article".equals(getIntent().getStringExtra(Constants.NOTIFICATION_TYPE))) {
            presenter.getSellerInformation(getIntent().getStringExtra(Constants.USER_ID_FROM_AD));
        }

        buildGoogleApiClient();
        mGoogleApiClient.connect();
    }

    public void prepareDataFromArticle(ArticleDetails articleDetails) {
        Log.e("CONAN", "prepare");
        String pictureUri = Urls.MAIN_SERVER_URL_V3 + "pictures/" + articleDetails.getUrls();
        mTitleText.setText(articleDetails.getTitle());
        String formatedPrice = getIntent().getStringExtra(Constants.PRICE).split("\\.")[0] + " €";
        mPrice.setText(formatedPrice);
        mDescText.setText(articleDetails.getDescription());
        mDateText.setText(DateFormat.getDateInstance().format(articleDetails.getDate()));
        mAdId = articleDetails.getId();
        String ownerId = articleDetails.getUserId();
        lat = articleDetails.getLocation().getCoordinates()[0];
        lng = articleDetails.getLocation().getCoordinates()[1];
        presenter.getSellerInformation(articleDetails.getUserId());
        setupPanel(pictureUri, ownerId);
    }

    private void setupPanel(String pictureUri, String ownerId) {
        int ratio = Math.round((float) displayWidth / (float) displayWidth);
        Picasso.with(getApplicationContext())
                .load(pictureUri)
                .placeholder(R.drawable.empty_photo)
                .resize((int) Math.round((float) displayWidth * 0.6), (int) Math.round((float) displayHeight * 0.6) * ratio)
                .centerInside()
                .into(imgView, new Callback() {
                    @Override
                    public void onSuccess() {
                        mOpenAdProgressBar.setVisibility(ProgressBar.GONE);
                    }

                    @Override
                    public void onError() {
                        mOpenAdProgressBar.setVisibility(ProgressBar.GONE);
                        Toast.makeText(getApplicationContext(), "No network connection while loading picture!", Toast.LENGTH_SHORT).show();
                        showDefaultPic();
                    }
                });

        if (isOwnAd() && mDelAndMsgButton != null) {
            mDelAndMsgButton.setOnClickListener((view) -> {
                //get ad id and send delete request
                Log.i("CONAN", "AdId: " + mAdId);
                deleteAdRequest(mAdId);
            });
        } else {
            mDelAndMsgButton.setText("Nachricht");
            mDelAndMsgButton.setOnClickListener((view) -> {

                if (!getUserId().equals("")) {
                    //send a message to ad owner
                    sendMessage(mAdId, ownerId);
                } else {
                    final Intent facebookIntent = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivityForResult(facebookIntent, Constants.REQUEST_ID_FOR_LOGIN);
                }
            });
        }

        mBookmarkButton.setClickable(false);
        //loadBookmarks for user
        presenter.loadBookmarksForUser();

        mBookmarkButton.setOnClickListener((view) -> {
            if (isBookmarked) {
                presenter.deleteBookmark(mAdId, utils.getUserToken());
            } else {
                presenter.bookmarkAd(mAdId, utils.getUserToken());
            }
        });

        Log.e("CONAN", "id oder whatever: " + mAdId);
        presenter.increaseViewCount(mAdId);

        Log.d("CONAN", "request Picture: " + pictureUri);
    }

    public void updateSellerInformation(User user) {
        userName.setText(user.getName());
        userNumberOfArticles.setText("Anzeigen: " + user.getNumberOfArticles().toString());
        Picasso.with(getContext())
                .load(user.getProfilePictureUrl())
                .into(userPic);


        userPic.setOnClickListener(v -> {
            Intent i = new Intent(getApplicationContext(), MainActivity.class);
            i.putExtra(Constants.USER_ID_FROM_AD, getIntent().getStringExtra(Constants.USER_ID_FROM_AD));
            startActivityForResult(i, Constants.REQUEST_ID_FOR_MAINACTIVITY);
        });
    }

    private void showDefaultPic() {
        int ratio = Math.round((float) displayWidth / (float) displayWidth);
        Picasso.with(getApplicationContext())
                .load(R.drawable.applogo)
                .placeholder(R.drawable.empty_photo)
                .resize((int) Math.round((float) displayWidth * 0.6), (int) Math.round((float) displayHeight * 0.6) * ratio)
                .centerCrop()
                .into(imgView);
    }

    protected synchronized void buildGoogleApiClient() {
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

    public void updateBookmarkButton(Long[] bookmark) {
        isBookmarked = false;
        ArrayList<Long> bookmarkList = new ArrayList<>(Arrays.asList(bookmark));
        if (bookmarkList.contains(Long.parseLong(mAdId.toString()))) {
            mBookmarkButton.setText("Vergessen");
            mBookmarkButton.setClickable(true);
            isBookmarked = true;
        } else {
            mBookmarkButton.setText("Merken");
            mBookmarkButton.setClickable(true);
        }
    }

    private void sendMessage(final Integer adId, final String receiverId) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        final EditText edittext = new EditText(OpenAdActivity.this);
        alert.setTitle("Sende eine Nachricht");
        alert.setView(edittext);
        alert.setPositiveButton("Senden", (dialog, whichButton) -> {
            String message = edittext.getText().toString();
            presenter.sendNewMessage(message, adId, receiverId, utils.getUserToken());
        });
        alert.setNegativeButton("nein doch nicht", (dismissDialog, whichButton) -> {/*just go away*/});
        alert.show();
    }

    private void deleteAdRequest(final Integer adId) {
        AlertDialog myQuittingDialogBox = new AlertDialog.Builder(this)
                .setTitle("Artikel Löschen")
                .setMessage("Willst du den Artikel wirklich löschen?")
                .setIcon(R.drawable.delete)
                .setPositiveButton("Löschen!", (dialog, whichButton) -> {
                    presenter.deleteAd(adId);
                    dialog.dismiss();
                    finish();
                })
                .setNegativeButton("nein doch nicht", (dialog, whichButton) -> dialog.dismiss())
                .create();
        myQuittingDialogBox.show();
    }

    private String getUserId() {
        return getSharedPreferences(SHARED_PREFS_USER_INFO, 0).getString(Constants.USER_ID, "");
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
        ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE);
        googleMap.clear();
        LatLng latLng = new LatLng(lat, lng);
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title(getIntent().getStringExtra(Constants.TITLE));
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
        googleMap.addMarker(markerOptions);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), 7));
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setScrollGesturesEnabled(true);
        googleMap.getUiSettings().setCompassEnabled(true);
        googleMap.getUiSettings().setMyLocationButtonEnabled(true);
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
}
