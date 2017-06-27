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
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
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
import com.squareup.picasso.Picasso;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;

import de.wichura.lks.R;
import de.wichura.lks.adapter.CustomSwipeAdapter;
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

    public AVLoadingIndicatorView mOpenAdProgressBar;
    public AVLoadingIndicatorView mOpenFullScreenImgProgressBar;

    private OpenAdPresenter presenter;
    private GoogleMap googleMap;
    private GoogleApiClient mGoogleApiClient;

    private double lat;
    private double lng;

    private HeightWrappingViewPager imagePager;
    private ImageView userPic;
    private TextView userName;
    private TextView userNumberOfArticles;
    private Button mDelAndMsgButton;
    private TextView mTitleText;
    private TextView mPrice;
    private TextView mDescText;
    private TextView mDateText;
    private Integer mAdId;
    private TextView locationName;
    public ImageView shareArticle;
    public Button facebookit;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.open_ad_activity);

        utils = new Utility(this);

        MapsInitializer.initialize(this);

        presenter = new OpenAdPresenter(this, new Service(), getApplicationContext());

        checkGoogleConnection(savedInstanceState);

        Toolbar toolbar = (Toolbar) findViewById(R.id.open_ad_toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            toolbar.setNavigationOnClickListener((view) -> finish());
        }

        mOpenAdProgressBar = (AVLoadingIndicatorView) findViewById(R.id.open_Ad_ProgressBar);
        mTitleText = (TextView) findViewById(R.id.title);
        mPrice = (TextView) findViewById(R.id.price);
        mDescText = (TextView) findViewById(R.id.description);
        mDateText = (TextView) findViewById(R.id.ad_date);
        imagePager = (HeightWrappingViewPager) findViewById(R.id.view_pager);
        mDelAndMsgButton = (Button) findViewById(R.id.delButton);
        mBookmarkButton = (Button) findViewById(R.id.bookmarkButton);
        userName = (TextView) findViewById(R.id.user_name);
        userNumberOfArticles = (TextView) findViewById(R.id.user_number_of_articles);
        userPic = (ImageView) findViewById(R.id.user_image);
        locationName = (TextView) findViewById(R.id.open_ad_location_name);
        shareArticle = (ImageView) findViewById(R.id.share_article);
        facebookit = (Button) findViewById(R.id.facebookit);

        getDisplayDimensions();

        //get data from Intent from mainActivity
        if (!"article".equals(getIntent().getStringExtra(Constants.NOTIFICATION_TYPE))) {
            //intent comes from article overview
            String pictureUri = getIntent().getStringExtra(Constants.URI_AS_LIST);
            mTitleText.setText(getIntent().getStringExtra(Constants.TITLE));
            locationName.setText(getIntent().getStringExtra(Constants.LOCATION_NAME));

            Float price = getIntent().getFloatExtra(Constants.PRICE, 0);
            mPrice.setText(Utility.getPriceString(price));

            mDescText.setText(getIntent().getStringExtra(Constants.DESCRIPTION));
            mDateText.setText("Erstellt am: " + DateFormat.getDateInstance().format(getIntent().getLongExtra(Constants.DATE, 0)));
            mAdId = getIntent().getIntExtra(Constants.ID, 0);
            lat = getIntent().getDoubleExtra(Constants.LAT, 0);
            lng = getIntent().getDoubleExtra(Constants.LNG, 0);
            String ownerId = getIntent().getStringExtra(Constants.USER_ID_FROM_AD);

            shareArticle.setOnClickListener(v -> {
                final Intent emailIntent = new Intent(Intent.ACTION_SEND);
                emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "Hier das könnte etwas für Dich sein: " +
                        //"http://www.luftkraftsport.de:9876/#/arcticle/" + mAdId + "/show");  //http://luftkraftsport.de:9876/#/article/3221/show
                        "localhost:8080/#/arcticle/" + mAdId + "/show");  //http://luftkraftsport.de:9876/#/article/3221/show
                emailIntent.setType("text/plain");
                startActivity(Intent.createChooser(emailIntent, "Sende es einem Freund"));
            });

            //Share on Facebook
            if (getUserType().equals(Constants.FACEBOOK_USER)) {
                facebookit.setVisibility(View.VISIBLE);
                facebookit.setOnClickListener(v -> fbImageSubmit(pictureUri,
                        getIntent().getStringExtra(Constants.TITLE),
                        getIntent().getStringExtra(Constants.DESCRIPTION),
                        Utility.getPriceString(getIntent().getFloatExtra(Constants.PRICE, 0))));
            }

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

    private void checkGoogleConnection(Bundle savedInstanceState) {
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
    }

    public void prepareDataFromArticle(ArticleDetails articleDetails) {

        //artikelvorschlag, deshalb muss USER_ID_FROM_AD mit aufs Intent -> delete oder message button
        getIntent().putExtra(Constants.USER_ID_FROM_AD, articleDetails.getUserId());

        String pictureUri = "";
        if (articleDetails.getUrls() != null) {
            pictureUri = articleDetails.getUrls();
        }

        mTitleText.setText(articleDetails.getTitle());
        locationName.setText(articleDetails.getLocationName());
        mPrice.setText(Utility.getPriceString(articleDetails.getPrice()));
        mDescText.setText(articleDetails.getDescription());
        mDateText.setText(DateFormat.getDateInstance().format(articleDetails.getDate()));
        mAdId = articleDetails.getId();
        String ownerId = articleDetails.getUserId();
        lat = articleDetails.getLocation().getCoordinates()[0];
        lng = articleDetails.getLocation().getCoordinates()[1];
        presenter.getSellerInformation(articleDetails.getUserId());

        shareArticle.setOnClickListener(v -> {
            final Intent emailIntent = new Intent(Intent.ACTION_SEND);
            emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "Hier das könnte etwas für Dich sein: " +
                    "http://www.luftkraftsport.de:9876/#/arcticle/" + mAdId + "/show");  //http://luftkraftsport.de:9876/#/article/3221/show
            emailIntent.setType("text/plain");
            startActivity(Intent.createChooser(emailIntent, "Sende es einem Freund"));
        });

        setupPanel(pictureUri, ownerId);
        //TODO ok mit onConnect nochmal aufrufen?
        //daten fuer artikel kommen später als onConnect
        onConnected(null);
    }

    private void setupPanel(String pictureUri, String ownerId) {
        mOpenAdProgressBar.setVisibility(View.VISIBLE);

        CustomSwipeAdapter swipeAdapter = new CustomSwipeAdapter(this, pictureUri, displayHeight, displayWidth);
        imagePager.setAdapter(swipeAdapter);


        if (isOwnAd() && mDelAndMsgButton != null) {
            mDelAndMsgButton.setOnClickListener((view) -> {
                //get ad id and send delete request
                Log.i("CONAN", "AdId: " + mAdId);
                int position = getIntent().getIntExtra(Constants.POSITION_IN_LIST, 0);
                deleteAdRequest(mAdId, position);
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
            if (isOwnAd()) {
                Intent i = new Intent(this, NewAdActivity.class);
                i.putExtra(Constants.ARTICLE_ID, mAdId);
                i.putExtra(Constants.TITLE, getIntent().getStringExtra(Constants.TITLE));
                i.putExtra(Constants.DESCRIPTION, getIntent().getStringExtra(Constants.DESCRIPTION));
                i.putExtra(Constants.PRICE, getIntent().getFloatExtra(Constants.PRICE, 0));
                i.putExtra(Constants.AD_URL, getIntent().getStringExtra(Constants.AD_URL));
                i.putExtra(Constants.LAT, getIntent().getDoubleExtra(Constants.LAT, 0));
                i.putExtra(Constants.LNG, getIntent().getDoubleExtra(Constants.LNG, 0));
                i.putExtra(Constants.DATE, getIntent().getLongExtra(Constants.DATE, 0));
                startActivityForResult(i, Constants.REQUEST_ID_FOR_NEW_AD);
            } else {

                if (!"".equals(getUserId())) {
                    if (isBookmarked) {
                        presenter.deleteBookmark(mAdId, utils.getUserToken());
                    } else {
                        presenter.bookmarkAd(mAdId, utils.getUserToken());
                    }
                } else {
                    Toast.makeText(this, "Bitte anmelden!", Toast.LENGTH_LONG).show();
                }
            }
        });

        presenter.increaseViewCount(mAdId);

        Log.d("CONAN", "request Picture: " + pictureUri);
    }

    private void fbImageSubmit(String imgUrlList, String title, String desc, String price) {

        facebookit.setClickable(false);
        facebookit.setText("wird geteilt auf Facebook...");

        String url = Urls.MAIN_SERVER_URL_V3 + "pictures/" + getMainPictureFromList(imgUrlList);

        JSONObject msg = new JSONObject();
        try {
            msg.put("message", "Auf Luftkraftsport gibt es folgendes: \n\n" + title + "\n\n" + desc + "\n\n" + "Preis: "
                    + price +"\n\n"
                    + "Luftkraftsport App im Play Store:\nhttps://play.google.com/store/apps/details?id=de.wichura.lks");

            msg.put("url", url);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        GraphRequest request = GraphRequest.newPostRequest(AccessToken.getCurrentAccessToken(), "/me/photos", msg, new GraphRequest.Callback() {
            @Override
            public void onCompleted(GraphResponse response) {
                Log.d("CONAN", "facebook upload done: " + response.getJSONObject());
                if (response.getError() != null)
                    Log.d("CONAN", "facebook upload done: " + response.getError().getErrorMessage());
                facebookit.setText("Geteilt auf Facebook!");
            }
        });

        GraphRequest.executeBatchAsync(request);

       /* GraphRequest request2 = GraphRequest.newPostRequest(AccessToken.getCurrentAccessToken(),
                "?url=https://play.google.com/store/apps/details?id=de.wichura.lks", null, new GraphRequest.Callback() {
            @Override
            public void onCompleted(GraphResponse response) {
                Log.d("CONAN", "facebook upload done: " + response.getJSONObject());
                if (response.getError() != null)
                    Log.d("CONAN", "facebook upload done: " + response.getError().getErrorMessage());
                facebookit.setText("Geteilt auf Facebook!");
            }
        });

        GraphRequest.executeBatchAsync(request2);
*/
    }

    private String getMainPictureFromList(String imgList) {

        if (imgList != null) {
            String[] uris = imgList.split(",");
            if (uris.length > 0) {
                return uris[0];
            }
        } else {
            return "nix";
        }
        return "problem";
    }


    public void updateSellerInformation(User user) {

        if (user != null) {
            userName.setText(user.getName());
            userNumberOfArticles.setText("Anzeigen: " + user.getNumberOfArticles().toString());
            Picasso.with(getContext())
                    .load(user.getProfilePictureUrl())
                    .placeholder(R.drawable.ic_person_outline_blue_grey_600_24dp)
                    .into(userPic);

            userPic.setOnClickListener(v -> {
                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                i.putExtra(Constants.USER_ID_FROM_AD, getIntent().getStringExtra(Constants.USER_ID_FROM_AD));
                startActivityForResult(i, Constants.REQUEST_ID_FOR_MAINACTIVITY);
            });
        } else {
            userName.setText("Keine Benutzer Infos vorhanden!");
            Picasso.with(getContext())
                    .load(R.drawable.ic_person_outline_blue_grey_600_24dp)
                    .into(userPic);
        }
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
        if (!isOwnAd()) {
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
        } else {
            mBookmarkButton.setText("Bearbeiten");
            mBookmarkButton.setClickable(true);
        }
    }

    private void sendMessage(final Integer adId, final String receiverId) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        final EditText edittext = new EditText(OpenAdActivity.this);
        //edittext.setInputType(InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE);
        alert.setTitle("Sende eine Nachricht");
        alert.setView(edittext);
        alert.setPositiveButton("Senden", (dialog, whichButton) -> {
            String message = edittext.getText().toString();
            presenter.sendNewMessage(message, adId, receiverId, utils.getUserToken());
        });
        alert.setNegativeButton("nein doch nicht", (dismissDialog, whichButton) -> {/*just go away*/});
        alert.show();
    }

    private void deleteAdRequest(final Integer adId, int position) {
        AlertDialog myQuittingDialogBox = new AlertDialog.Builder(this)
                .setTitle("Artikel Löschen")
                .setMessage("Willst du den Artikel wirklich löschen?")
                .setIcon(R.drawable.delete)
                .setPositiveButton("Löschen!", (dialog, whichButton) -> {
                    presenter.deleteAd(adId);
                    dialog.dismiss();
                    //get back postion from main list to remove from view in mainActivity
                    Intent intent = new Intent();
                    intent.putExtra(Constants.POSITION_IN_LIST, position);
                    setResult(RESULT_OK, intent);
                    finish();
                })
                .setNegativeButton("nein doch nicht", (dialog, whichButton) -> dialog.dismiss())
                .create();
        myQuittingDialogBox.show();
    }

    private String getUserId() {
        return getSharedPreferences(SHARED_PREFS_USER_INFO, 0).getString(Constants.USER_ID, "");
    }

    private String getFacebookToken() {
        if (getSharedPreferences(SHARED_PREFS_USER_INFO, 0).getString(Constants.USER_TYPE, "").equals(Constants.FACEBOOK_USER)) {
            return getSharedPreferences(SHARED_PREFS_USER_INFO, 0).getString(Constants.USER_TOKEN, "");
        } else {
            return "";
        }
    }

    private String getUserType() {
        return getSharedPreferences(SHARED_PREFS_USER_INFO, 0).getString(Constants.USER_TYPE, "");
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
        Log.d("CONAN", "in onConnected in OpenActivity");
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
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    @Override
    public void onLocationChanged(Location location) {
    }
}
