package de.wichura.lks.mainactivity;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.squareup.picasso.Picasso;
import com.wang.avi.AVLoadingIndicatorView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.wichura.lks.R;
import de.wichura.lks.activity.LoginActivity;
import de.wichura.lks.activity.MessagesOverviewActivity;
import de.wichura.lks.activity.NewAdActivity;
import de.wichura.lks.activity.OpenAdActivity;
import de.wichura.lks.activity.SearchActivity;
import de.wichura.lks.activity.SettingsActivity;
import de.wichura.lks.adapter.MainListViewAdapter;
import de.wichura.lks.gcm.QuickstartPreferences;
import de.wichura.lks.gcm.RegistrationIntentService;
import de.wichura.lks.http.Service;
import de.wichura.lks.models.AdsAndBookmarks;
import de.wichura.lks.models.RowItem;
import de.wichura.lks.presentation.MainPresenter;
import me.leolin.shortcutbadger.ShortcutBadger;

import static de.wichura.lks.mainactivity.Constants.SHARED_PREFS_USER_INFO;
import static de.wichura.lks.mainactivity.Constants.SHOW_MY_ADS;
import static de.wichura.lks.mainactivity.Constants.UNREAD_MESSAGES;


public class MainActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener,
        SharedPreferences.OnSharedPreferenceChangeListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    public ListView listView;
    private int page;
    private int size;
    private int pages;
    private int total;

    private static final String TAG = "CONAN";
    private ImageView loginBtn;
    private ImageView messagesBtn;
    private DrawerLayout drawer;

    public AVLoadingIndicatorView progressBar;

    //Google Cloud Messages
    private BroadcastReceiver mGcmRegistrationBroadcastReceiver;
    private boolean isGcmReceiverRegistered;

    //login
    private BroadcastReceiver mLoginBroadcastReceiver;
    private boolean isLoginReceiverRegistered;

    //Messages
    private BroadcastReceiver mMessageBroadcastReceiver;
    private boolean isMessageBroadcastReceiver;


    private MainPresenter presenterLayer;
    private MainListViewAdapter adapter;
    private List<RowItem> rowItems;

    private static final String LIST_STATE = "listState";
    private Parcelable mListState = null;

    private GoogleApiClient mGoogleApiClient;

    //app navigation
    private Boolean isBookmarks;
    private Boolean isMyAds;
    private Boolean isSearch;

    //search again
    private Button searchAgainButton;
    private String searchKeyword;
    private String searchPriceFrom;
    private String searchPriceTo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        page = 0;
        size = 10;
        pages = 0;
        total = 0;

        isBookmarks = false;
        isMyAds = false;
        isSearch = false;

        Service service = new Service();
        presenterLayer = new MainPresenter(this, service, getApplicationContext());

        checkLocationServiceEnabled();

        getLastLocation();

        //TODO: set active false in messageActivity in onDestroy, onStop, on???  BUT NOT HERE
        SharedPreferences sp = getSharedPreferences(Constants.MESSAGE_ACTIVITY, MODE_PRIVATE);
        SharedPreferences.Editor ed = sp.edit();
        ed.putBoolean("active", false);
        ed.putString("adId", "");
        ed.apply();

        //get Facebook access token
        FacebookSdk.sdkInitialize(getApplicationContext());

        //load main layout
        setContentView(R.layout.activity_main);
        listView = (ListView) findViewById(R.id.main_list);

        //ProgressBar
        progressBar = (AVLoadingIndicatorView) findViewById(R.id.progressBar);
        messagesBtn = (ImageView) findViewById(R.id.main_mail_button);

        //configure Flurry for analysis
        //configureFlurry();

        //load toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //init drawer
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        //init navigationbar
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        if (navigationView != null) navigationView.setNavigationItemSelectedListener(this);

        loginBtn = (ImageView) findViewById(R.id.main_login_button);
        updateLoginButton();

        AccessTokenTracker tracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken newAccessToken) {
                if (newAccessToken != null) {
                    String userToken = newAccessToken.getToken();
                    setUserPreferences(null, null, userToken);
                }
                if (newAccessToken == null) {
                    //Facebook user logged out: name="" and userId=""
                    setUserPreferences("", "", "");
                    setProfileName("");
                    setProfilePicture(null);
                    updateLoginButton();
                }
            }
        };

        ProfileTracker profileTracker = new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(Profile oldProfile, Profile newProfile) {
                if (newProfile != null) {
                    String userId = newProfile.getId();
                    String name = newProfile.getName();

                    setUserPreferences(name, userId, null);

                    //not logged in as FB user: create db entry, GCM token, update login button
                    if (oldProfile == null && checkPlayServices() && isUserLoggedIn()) {
                        if (checkPlayServices() && isUserLoggedIn()) {
                            // Start IntentService to register this application with GCM.
                            Intent intent = new Intent(getApplicationContext(), RegistrationIntentService.class);
                            startService(intent);
                        }
                        updateLoginButton();
                    }
                    Intent loginComplete = new Intent(Constants.LOGIN_COMPLETE);
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(loginComplete);
                }
            }
        };
        tracker.startTracking();
        profileTracker.startTracking();

        mGcmRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                SharedPreferences sharedPreferences =
                        PreferenceManager.getDefaultSharedPreferences(context);
                boolean sentToken = sharedPreferences
                        .getBoolean(QuickstartPreferences.SENT_TOKEN_TO_SERVER, false);
                if (sentToken) {
                    Log.d("CONAN", "Token from GCM received");
                } else {
                    Log.d("CONAN", "Did not get a Token from GCM!");
                }
            }
        };

        registerGcmReceiver();

        if (checkPlayServices()) {
            // Start IntentService to register this application with GCM.
            Intent intent = new Intent(this, RegistrationIntentService.class);
            startService(intent);
        }

        mMessageBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                messagesBtn.setVisibility(View.VISIBLE);
                //show unread messages over app icon
                SharedPreferences stackUnread = getSharedPreferences(UNREAD_MESSAGES, 0);
                Map unreadMsgMap = stackUnread.getAll();
                ShortcutBadger.applyCount(getApplicationContext(), unreadMsgMap.size());
            }
        };

        setupMessageBroadcastReceiver();

        mLoginBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Toast.makeText(MainActivity.this, "BROADCAST LOGIN RECEIVED", Toast.LENGTH_SHORT).show();
            }
        };

        registerLoginReceiver();

        initSearchAgainButton();

        if (getIntent().getStringExtra(Constants.USER_ID_FROM_AD) != null) {
            String userId = getIntent().getStringExtra(Constants.USER_ID_FROM_AD);
            setMyAdsFlag(false);
            presenterLayer.searchForArticles(0, size,
                    null,
                    null,
                    Constants.DISTANCE_INFINITY,
                    null,
                    userId); //userId
        } else {

            setMyAdsFlag(false);
            getAds(Constants.TYPE_ALL);
        }
    }

    private void initSearchAgainButton() {
        searchAgainButton = (Button) findViewById(R.id.search_again);
        searchAgainButton.setOnClickListener(v -> {
            searchAgainButton.setVisibility(View.GONE);
            final Intent searchIntent = new Intent(this, SearchActivity.class);
            searchIntent.putExtra(Constants.TITLE, searchKeyword);
            searchIntent.putExtra(Constants.PRICE_FROM, searchPriceFrom);
            searchIntent.putExtra(Constants.PRICE_TO, searchPriceTo);
            startActivityForResult(searchIntent, Constants.REQUEST_ID_FOR_SEARCH);
        });
    }

    private void setupMessageBroadcastReceiver() {

        if (!isMessageBroadcastReceiver) {
            LocalBroadcastManager.getInstance(this).registerReceiver(mMessageBroadcastReceiver,
                    new IntentFilter("messageReceived"));
            isMessageBroadcastReceiver = true;
        }

    }

    public void checkLocationServiceEnabled() {
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (!manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            Toast.makeText(this, "Location Service ausgeschaltet", Toast.LENGTH_LONG).show();
            saveLocationServiceStatus(false);
        } else {
            saveLocationServiceStatus(true);
        }
    }


    private void getLastLocation() {
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (presenterLayer.subscription != null && !presenterLayer.subscription.isUnsubscribed()) {
            presenterLayer.subscription.unsubscribe();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerGcmReceiver();
        registerLoginReceiver();
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        sp.registerOnSharedPreferenceChangeListener(this);

        if (mListState != null) listView.onRestoreInstanceState(mListState);
    }

    @Override
    protected void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        mListState = listView.onSaveInstanceState();
        state.putParcelable(LIST_STATE, mListState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle state) {
        super.onRestoreInstanceState(state);
        mListState = state.getParcelable(LIST_STATE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mGcmRegistrationBroadcastReceiver);
        isGcmReceiverRegistered = false;

        LocalBroadcastManager.getInstance(this).unregisterReceiver(mLoginBroadcastReceiver);
        isLoginReceiverRegistered = false;

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        sp.unregisterOnSharedPreferenceChangeListener(this);

        //setMyAdsFlag(false);
        super.onPause();
    }

    private void registerLoginReceiver() {
        if (!isLoginReceiverRegistered) {
            LocalBroadcastManager.getInstance(this).registerReceiver(mLoginBroadcastReceiver,
                    new IntentFilter(Constants.LOGIN_COMPLETE));
            isLoginReceiverRegistered = true;
        }
    }

    private void registerGcmReceiver() {
        if (!isGcmReceiverRegistered) {
            LocalBroadcastManager.getInstance(this).registerReceiver(mGcmRegistrationBroadcastReceiver,
                    new IntentFilter(QuickstartPreferences.REGISTRATION_COMPLETE));
            isGcmReceiverRegistered = true;
        }
    }

    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, Constants.PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    public void updateAds(AdsAndBookmarks elements, String type, Integer priceFrom, Integer priceTo, Integer distance, String description, String userId) {

        rowItems = new ArrayList<>();
        for (RowItem e : elements.getAdsPage().getAds()) {
            rowItems.add(e);
        }

        page = elements.getAdsPage().getPage();
        size = elements.getAdsPage().getSize();
        pages = elements.getAdsPage().getPages();
        total = elements.getAdsPage().getTotal();

        showNumberOfAds(total);
        adapter = new MainListViewAdapter(
                this,
                getApplicationContext(),
                R.layout.list_item, rowItems,
                elements.getBookmarks());

        listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        listView.setOnItemClickListener((arg0, arg1, position, arg3) -> {
            final RowItem rowItem = (RowItem) listView.getItemAtPosition(position);
            final Intent intent = new Intent(getApplicationContext(), OpenAdActivity.class);
            //TODO more images anzeigen...
            intent.putExtra(Constants.URI_AS_LIST, rowItem.getUrl());
            intent.putExtra(Constants.ID, rowItem.getId());
            intent.putExtra(Constants.TITLE, rowItem.getTitle());
            intent.putExtra(Constants.DESCRIPTION, rowItem.getDescription());
            intent.putExtra(Constants.LAT, rowItem.getLocation().getCoordinates()[0]);
            intent.putExtra(Constants.LNG, rowItem.getLocation().getCoordinates()[1]);
            intent.putExtra(Constants.PHONE, rowItem.getPhone());
            //TODO DRECK!!!! Float.parse(String)
            intent.putExtra(Constants.PRICE, Float.parseFloat(rowItem.getPrice()));
            intent.putExtra(Constants.DATE, rowItem.getDate());
            intent.putExtra(Constants.VIEWS, rowItem.getViews());
            intent.putExtra(Constants.USER_ID_FROM_AD, rowItem.getUserId());
            intent.putExtra(Constants.USER_ID, getUserId());
            intent.putExtra(Constants.POSITION_IN_LIST, position);
            intent.putExtra(Constants.AD_URL, rowItem.getUrl());  //TODO einmal hier getURL dann oben nochmal.... aufräumen
            startActivityForResult(intent, Constants.REQUEST_ID_FOR_OPEN_AD);
        });

        View empty = findViewById(R.id.empty_list_view);
        listView.setEmptyView(empty);

        listView.setOnScrollListener(new EndlessScrollListener() {
            @Override
            public boolean onLoadMore(int page, int totalItemsCount) {
                // Triggered only when new data needs to be appended to the list
                // Add whatever code is needed to append new items to your AdapterView
                loadNextDataFromApi(page, type, priceFrom, priceTo, distance, description, userId);
                // or loadNextDataFromApi(totalItemsCount);
                return true; // ONLY if more data is actually being loaded; false otherwise.
            }
        });
    }

    // Append the next page of data into the adapter
    // This method probably sends out a network request and appends new data items to your adapter.
    public void loadNextDataFromApi(int offset, String type, Integer priceFrom, Integer priceTo, Integer distance, String description, String userId) {
        // Send an API request to retrieve appropriate paginated data
        //  --> Send the request including an offset value (i.e `page`) as a query parameter.
        //  --> Deserialize and construct new model objects from the API response
        //  --> Append the new data objects to the existing set of items inside the array of items
        //  --> Notify the adapter of the new items made with `notifyDataSetChanged()`
        Log.d("CONAN", "OFFSET: " + offset);
        if (offset <= pages) {
            page = page + 1;
            if (type == null) {
                presenterLayer.searchForArticles(page, size, priceFrom, priceTo, distance, description, userId);
            } else {
                presenterLayer.loadAdDataPage(page, size, type);
            }
        }
    }

    public void addMoreAdsToList(AdsAndBookmarks elements) {

        for (RowItem e : elements.getAdsPage().getAds()) {
            rowItems.add(e);
        }
        adapter.notifyDataSetChanged();
    }

    private void getAds(String type) {
        page = 0;
        if (adapter != null) {
            adapter.clear();
        }
        presenterLayer.loadAdDataPage(page, size, type);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case Constants.REQUEST_ID_FOR_NEW_AD: {
                //my articles -> edit one -> just go back -> still show my articles
                if (data != null && data.getStringExtra(Constants.IS_EDIT_MODE) != null) {
                    setMyAdsFlag(true);
                    getAds(Constants.TYPE_USER);
                    break;
                }
                //just show all
                setMyAdsFlag(false);
                getAds(Constants.TYPE_ALL);
                break;
            }
            case Constants.REQUEST_ID_FOR_LOGIN: {

                if (getUserType().equals(Constants.FACEBOOK_USER)) {
                    if (checkPlayServices() && isUserLoggedIn()) {
                        // Start IntentService to register this application with GCM.
                        Intent intent = new Intent(this, RegistrationIntentService.class);
                        startService(intent);
                    }
                }

                if (getUserType().equals(Constants.EMAIL_USER) && isUserLoggedIn()) {
                    //request Token from GCM and update in DB
                    if (checkPlayServices() && isUserLoggedIn()) {
                        // Start IntentService to register this application with GCM.
                        Intent intent = new Intent(this, RegistrationIntentService.class);
                        startService(intent);
                    }
                }

                if (getUserType().equals(Constants.GOOGLE_USER) && isUserLoggedIn()) {
                    presenterLayer.sendUserPicToServer(getUserProfilePic());

                    //request Token from GCM and update in DB
                    if (checkPlayServices() && isUserLoggedIn()) {
                        // Start IntentService to register this application with GCM.
                        Intent intent = new Intent(this, RegistrationIntentService.class);
                        startService(intent);
                    }
                }

                updateLoginButton();
                setProfileName(getUserName());
                if (!isUserLoggedIn()) {
                    setProfileName("Please login...");
                }
                if (!"".equals(getUserProfilePic())) {
                    setProfilePicture(Uri.parse(getUserProfilePic()));
                } else {
                    setProfilePicture(null);
                }
                Log.d("CONAN", "Return from login, userid: " + getUserId());

                setMyAdsFlag(false);

                getAds(Constants.TYPE_ALL);

                break;
                //end case UserLogin
            }
            case Constants.REQUEST_ID_FOR_OPEN_AD: {
                //in case article is deleted -> remove from list
                if (data != null) {
                    adapter.remove(adapter.getItem(data.getIntExtra(Constants.POSITION_IN_LIST, 0)));
                    adapter.notifyDataSetChanged();
                }
                //in case article is bookmarked -> show blue star
                // if (data.getIntExtra(Constants.POSITION_IN_LIST, 0) != 0 && data.getStringExtra(Constants.BOOKMARKED_FLAG == true)) {

                break;
            }
            case Constants.REQUEST_ID_FOR_SEARCH: {
                if (data != null) {
                    searchKeyword = data.getStringExtra(Constants.KEYWORDS);
                    searchPriceFrom = data.getStringExtra(Constants.PRICE_FROM);
                    searchPriceTo = data.getStringExtra(Constants.PRICE_TO);
                    int distance = data.getIntExtra(Constants.DISTANCE, Constants.DISTANCE_INFINITY);
                    setMyAdsFlag(false);

                    presenterLayer.searchForArticles(0, size,
                            searchPriceFrom.equals("") ? null : Integer.parseInt(searchPriceFrom),
                            searchPriceTo.equals("") ? null : Integer.parseInt(searchPriceTo),
                            distance,
                            searchKeyword,
                            null); //userId
                    drawer.closeDrawer(GravityCompat.START);
                    searchAgainButton.setVisibility(View.VISIBLE);
                }
                break;
            }
            case Constants.REQUEST_ID_FOR_SETTINGS: {
                updateLoginButton();
                setProfileName(getUserName());
                if (!isUserLoggedIn()) {
                    setProfileName("Please login...");
                }
                setProfilePicture(Uri.parse(getUserProfilePic()));
                break;
            }
        }
    }

    @Override
    public void onBackPressed() {
        //in case we are back from search and do not care to adapt search
        searchAgainButton.setVisibility(View.GONE);

        if (drawer != null && drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (isBookmarks) {
            setMyAdsFlag(false);
            isBookmarks = false;
            getAds(Constants.TYPE_ALL);
        } else if (isMyAds) {
            setMyAdsFlag(false);
            isMyAds = false;
            getAds(Constants.TYPE_ALL);
        } else if (isSearch) {
            setMyAdsFlag(false);
            isSearch = false;
            getAds(Constants.TYPE_ALL);

        } else {
            if (isTaskRoot()) {
                new ExitDialogFragment().show(getSupportFragmentManager(), null);
            } else {
                super.onBackPressed();
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        setProfileName(getUserName());
        if (!isUserLoggedIn()) {
            setProfileName("Please login...");
        }
        setProfilePicture(Uri.parse(getUserProfilePic()));
        return true;
    }

    private void updateLoginButton() {
        if (getUserId().equals("")) {
            Log.d("CONAN", "enable login button");
            loginBtn.setEnabled(true);
            loginBtn.setVisibility(View.VISIBLE);
            loginBtn.setOnClickListener((view) -> startLoginActivity());
            presenterLayer.getFacebookUserInfo();  //TODO richtig hier?
        } else {
            Log.d("CONAN", "disable login button");
            loginBtn.setEnabled(false);
            loginBtn.setVisibility(View.GONE);
            presenterLayer.getFacebookUserInfo();  //TODO richtig hier?
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        final String userId = getUserId();

        //in case we are back from search and do not care to adapt search
        searchAgainButton.setVisibility(View.GONE);

        switch (item.getItemId()) {
            case R.id.myads: {
                if (userId.equals("")) {
                    startLoginActivity();
                    return true;
                } else {
                    page = 0;
                    size = 10;
                    setMyAdsFlag(true);
                    isMyAds = true;
                    isBookmarks = false;
                    isSearch = false;
                    getAds(Constants.TYPE_USER);
                    if (drawer != null) drawer.closeDrawer(GravityCompat.START);
                    return true;
                }
            }
            case R.id.new_ad: {
                if (userId.equals("")) {
                    startLoginActivity();
                    return true;
                }
                final Intent intent = new Intent(this, NewAdActivity.class);
                intent.putExtra(Constants.USER_ID, userId);
                startActivityForResult(intent, Constants.REQUEST_ID_FOR_NEW_AD);
                return true;
            }
            case R.id.search: {
                isMyAds = false;
                isBookmarks = false;
                isSearch = true;
                searchAgainButton.setVisibility(View.GONE);
                //TODO alten search parameter mitgeben!
                final Intent searchIntent = new Intent(this, SearchActivity.class);
                startActivityForResult(searchIntent, Constants.REQUEST_ID_FOR_SEARCH);
                return true;
            }
            case R.id.settings: {
                final Intent i = new Intent(this, SettingsActivity.class);
                startActivityForResult(i, Constants.REQUEST_ID_FOR_SETTINGS);
                return true;
            }
            case R.id.refresh: {
                setMyAdsFlag(false);
                isMyAds = false;
                isBookmarks = false;
                isSearch = false;
                getAds(Constants.TYPE_ALL);
                if (drawer != null) drawer.closeDrawer(GravityCompat.START);
                return true;
            }
            case R.id.bookmarks: {
                if (userId.equals("")) {
                    startLoginActivity();
                    return true;
                } else {
                    setMyAdsFlag(false);
                    isBookmarks = true;
                    isMyAds = false;
                    isSearch = false;
                    getAds(Constants.TYPE_BOOKMARK);
                    if (drawer != null) drawer.closeDrawer(GravityCompat.START);
                    return true;
                }
            }
            case R.id.messages_from_user: {
                if (userId.equals("")) {
                    startLoginActivity();
                    return true;
                } else {
                    messagesBtn.setVisibility(View.GONE);
                    final Intent msgIntent = new Intent(getApplicationContext(), MessagesOverviewActivity.class);
                    msgIntent.putExtra(Constants.USER_ID, userId);
                    startActivityForResult(msgIntent, Constants.REQUEST_ID_FOR_MESSAGES);
                    return true;
                }
            }
        }

        if (drawer != null) drawer.closeDrawer(GravityCompat.START);
        return super.onOptionsItemSelected(item);
    }

    private void setMyAdsFlag(boolean isMyAds) {
        SharedPreferences settings = getSharedPreferences(SHOW_MY_ADS, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(Constants.IS_MY_ADS, isMyAds);
        editor.apply();
    }

    private void startLoginActivity() {
        final Intent facebookIntent = new Intent(this, LoginActivity.class);
        startActivityForResult(facebookIntent, Constants.REQUEST_ID_FOR_LOGIN);
    }

    public void setProfilePicture(Uri uri) {
        if (uri != null) {
            Log.d("CONAN", "Set profile picture: " + uri.toString());
        }
        ImageView proPic = (ImageView) findViewById(R.id.profile_image);
        if (uri != null && !"".equals(uri.toString())) {
            Picasso.with(getApplicationContext()).load(uri.toString()).into(proPic);
        } else {
            if (proPic != null) {
                proPic.setImageResource(R.drawable.applogo);
            }
        }
    }

    private void setUserPreferences(String name, String userId, String userToken) {
        SharedPreferences settings = getSharedPreferences(SHARED_PREFS_USER_INFO, 0);
        SharedPreferences.Editor editor = settings.edit();
        if (name != null) editor.putString(Constants.USER_NAME, name);
        if (userId != null) editor.putString(Constants.USER_ID, userId);
        if (userToken != null) editor.putString(Constants.USER_TOKEN, userToken);
        editor.apply();
    }

    public void setProfileName(String name) {
        TextView nav_user = (TextView) findViewById(R.id.username);
        if (nav_user != null) nav_user.setText(name);
    }

    private String getUserName() {
        return getSharedPreferences(SHARED_PREFS_USER_INFO, 0).getString(Constants.USER_NAME, "");
    }

    private String getUserId() {
        return getSharedPreferences(SHARED_PREFS_USER_INFO, 0).getString(Constants.USER_ID, "");
    }

    private String getUserProfilePic() {
        return getSharedPreferences(SHARED_PREFS_USER_INFO, 0).getString(Constants.USER_PICTURE, "");
    }

    private Boolean isUserLoggedIn() {
        return !getUserId().equals("");
    }

    private String getUserType() {
        return getSharedPreferences(SHARED_PREFS_USER_INFO, 0).getString(Constants.USER_TYPE, "");
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.d("CONAN", "KEY:" + key);
        if (key.equals(SHARED_PREFS_USER_INFO)) {
            Log.d("CONAN", "jooooooooooo");
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE);
        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (mLastLocation != null) {
            double lat = mLastLocation.getLatitude();
            double lng = mLastLocation.getLongitude();
            if (lat == 0 && lng == 0) {
                saveLocationServiceStatus(false);
            } else {
                saveLastPosition(lat, lng);
            }
        }
    }

    private void saveLocationServiceStatus(Boolean isEnabled) {
        SharedPreferences sp = getSharedPreferences(Constants.USERS_LOCATION, MODE_PRIVATE);
        SharedPreferences.Editor ed = sp.edit();
        ed.putBoolean(Constants.LOCATION_SERVICE_IS_ENABLED, isEnabled);
        ed.apply();
    }

    private void saveLastPosition(double lat, double lng) {
        SharedPreferences sp = getSharedPreferences(Constants.USERS_LOCATION, MODE_PRIVATE);
        SharedPreferences.Editor ed = sp.edit();
        ed.putLong(Constants.LAT, Double.doubleToRawLongBits(lat));
        ed.putLong(Constants.LNG, Double.doubleToRawLongBits(lng));
        ed.apply();
    }

    public void showNumberOfAds(int numberOfAds) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setSubtitle("Anzeigen: " + numberOfAds);
        }
    }

    public void setMainTitle(String title) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}