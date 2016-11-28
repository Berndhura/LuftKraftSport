package wichura.de.camperapp.mainactivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.flurry.android.FlurryAgent;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.squareup.picasso.Picasso;
import com.wang.avi.AVLoadingIndicatorView;

import java.util.ArrayList;
import java.util.List;

import rx.Subscription;
import wichura.de.camperapp.R;
import wichura.de.camperapp.activity.NewAdActivity;
import wichura.de.camperapp.activity.OpenAdActivity;
import wichura.de.camperapp.activity.SearchActivity;
import wichura.de.camperapp.activity.SettingsActivity;
import wichura.de.camperapp.adapter.CustomListViewAdapter;
import wichura.de.camperapp.gcm.QuickstartPreferences;
import wichura.de.camperapp.gcm.RegistrationIntentService;
import wichura.de.camperapp.http.MyVolley;
import wichura.de.camperapp.http.Service;
import wichura.de.camperapp.http.Urls;
import wichura.de.camperapp.http.VolleyService;
import wichura.de.camperapp.activity.LoginActivity;
import wichura.de.camperapp.activity.MessagesOverviewActivity;
import wichura.de.camperapp.models.AdsAndBookmarks;
import wichura.de.camperapp.models.RowItem;
import wichura.de.camperapp.presentation.MainPresenter;

import static wichura.de.camperapp.mainactivity.Constants.IS_MY_ADS;
import static wichura.de.camperapp.mainactivity.Constants.SHARED_PREFS_USER_INFO;
import static wichura.de.camperapp.mainactivity.Constants.SHOW_MY_ADS;

public class MainActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener,
        SharedPreferences.OnSharedPreferenceChangeListener {

    public ListView listView;
    private int page;
    private int size;
    private int pages;
    private int total;

    private static final String TAG = "CONAN";
    private ImageView loginBtn;
    private DrawerLayout drawer;

    //Google Cloud Messages
    private BroadcastReceiver mGcmRegistrationBroadcastReceiver;
    private boolean isGcmReceiverRegistered;

    public AVLoadingIndicatorView progressBar;

    //login
    private BroadcastReceiver mLoginBroadcastReceiver;
    private boolean isLoginReceiverRegistered;

    //Volley Http service
    private VolleyService volleyService;

    private Subscription subscription;

    private MainPresenter presenterLayer;
    private Service service;
    private CustomListViewAdapter adapter;
    private List<RowItem> rowItems;

    public MainActivity() {
        volleyService = new VolleyService(MainActivity.this);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize Volley
        MyVolley.init(this);

        page = 0;
        size = 10;
        pages = 0;
        total = 0;

        service = new Service();
        presenterLayer = new MainPresenter(this, service, getApplicationContext());

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

        //ProgressBar
        progressBar = (AVLoadingIndicatorView) findViewById(R.id.progressBar);

        //configure Flurry for analysis
        configureFlurry();

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

        loginBtn = (ImageView) findViewById(R.id.login_button);

        updateLoginButton();

        AccessTokenTracker tracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken newAccessToken) {
                if (newAccessToken == null) {
                    //Facebook user logged out: name="" and userId=""
                    setUserPreferences("", "");
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

                    setUserPreferences(name, userId);

                    //not logged in as FB user: create db entry, GCM token, update login button
                    if (oldProfile == null && checkPlayServices() && isUserLoggedIn()) {
                        updateUserInDb(getUserName(), getUserId());
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

        mLoginBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Toast.makeText(MainActivity.this, "BROADCAST LOGIN RECEIVED", Toast.LENGTH_SHORT).show();
            }
        };

        registerLoginReceiver();

        //BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.navigation_view);

       //getApplicationContext());

        //TODO: setMyAdsFlag(true) when GET_ADS_FOR_USER, refactor
        setMyAdsFlag(false);
        getAds(Urls.MAIN_SERVER_URL + Urls.GET_ALL_ADS_URL);

        Toast.makeText(this, "ONCREATE!!!!!!!!!!", Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
        }
        Toast.makeText(this, "ONDestroy!!!!!!!!!!", Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerGcmReceiver();
        registerLoginReceiver();
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        sp.registerOnSharedPreferenceChangeListener(this);
        setMyAdsFlag(false);
        Toast.makeText(this, "ONresume!!!!!!!!!!", Toast.LENGTH_LONG).show();
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

        setMyAdsFlag(false);
        Toast.makeText(this, "ONpause!!!!!!!!!!", Toast.LENGTH_LONG).show();
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

    private void configureFlurry() {
        FlurryAgent.setLogEnabled(true);
        FlurryAgent.setCaptureUncaughtExceptions(true);
        FlurryAgent.setLogLevel(Log.ERROR);
        FlurryAgent.init(this, "3Q9GDM9TDX77WDGBN25S");
        FlurryAgent.logEvent("mainactivity started");
    }

    /*
        Updates the empty list view with contextually relevant information that the user can
        use to determine why they aren't seeing ads.
     */
    public void showEmptyView() {
        //if ( adapter.getCount() == 0 ) {
        TextView tv = (TextView) findViewById(R.id.recyclerview_ads_list_empty);
        tv.setVisibility(View.VISIBLE);

        // if cursor is empty, why? do we have an invalid location
        Log.d("CONAN", "nix");
    }

    public void hideEmptyView() {
        //if ( adapter.getCount() == 0 ) {
        TextView tv = (TextView) findViewById(R.id.recyclerview_ads_list_empty);
        tv.setVisibility(View.GONE);

        // if cursor is empty, why? do we have an invalid location
        Log.d("CONAN", "nix");
    }


    public void updateAds(AdsAndBookmarks elements) {

        rowItems = new ArrayList<>();
        for (RowItem e : elements.getAds().getAds()) {
            rowItems.add(e);
        }

        page = elements.getAds().getPage();
        size = elements.getAds().getSize();
        pages = elements.getAds().getPages();
        total = elements.getAds().getTotal();

        showNumberOfAds(total);

        listView = (ListView) findViewById(R.id.main_list);
        adapter = new CustomListViewAdapter(
                this,
                getApplicationContext(),
                R.layout.list_item, rowItems,
                elements.getBookmarks());

        listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        listView.setOnItemClickListener((arg0, arg1, position, arg3) -> {
            final RowItem rowItem = (RowItem) listView.getItemAtPosition(position);
            final Intent intent = new Intent(getApplicationContext(), OpenAdActivity.class);
            intent.putExtra(Constants.URI, Urls.MAIN_SERVER_URL + "getBild?id=" + rowItem.getUrl());
            intent.putExtra(Constants.AD_ID, rowItem.getAdId());
            intent.putExtra(Constants.TITLE, rowItem.getTitle());
            intent.putExtra(Constants.DESCRIPTION, rowItem.getDescription());
            intent.putExtra(Constants.LOCATION, rowItem.getLocation());
            intent.putExtra(Constants.PHONE, rowItem.getPhone());
            intent.putExtra(Constants.PRICE, rowItem.getPrice());
            intent.putExtra(Constants.DATE, rowItem.getDate());
            intent.putExtra(Constants.VIEWS, rowItem.getViews());
            intent.putExtra(Constants.USER_ID_FROM_AD, rowItem.getUserId());
            intent.putExtra(Constants.USER_ID, getUserId());
            startActivityForResult(intent, Constants.REQUEST_ID_FOR_OPEN_AD);
        });

        listView.setOnScrollListener(new EndlessScrollListener() {
            @Override
            public boolean onLoadMore(int page, int totalItemsCount) {
                // Triggered only when new data needs to be appended to the list
                // Add whatever code is needed to append new items to your AdapterView
                loadNextDataFromApi(page);
                // or loadNextDataFromApi(totalItemsCount);
                return true; // ONLY if more data is actually being loaded; false otherwise.
            }
        });
    }

    // Append the next page of data into the adapter
    // This method probably sends out a network request and appends new data items to your adapter.
    public void loadNextDataFromApi(int offset) {
        // Send an API request to retrieve appropriate paginated data
        //  --> Send the request including an offset value (i.e `page`) as a query parameter.
        //  --> Deserialize and construct new model objects from the API response
        //  --> Append the new data objects to the existing set of items inside the array of items
        //  --> Notify the adapter of the new items made with `notifyDataSetChanged()`
        Log.d("CONAN", "OFFSET: "+offset);
        if (offset<=pages) {
            page = page + 1;
            if (isMyAdsRequest()) {
                presenterLayer.getAdsForUser(page, size, getUserToken());
            } else {
                presenterLayer.loadAdDataPage(page, size);
            }
        }
    }

    public void addMoreAdsToList(AdsAndBookmarks elements) {

        for (RowItem e : elements.getAds().getAds()) {
            rowItems.add(e);
        }
        adapter.notifyDataSetChanged();
    }

    private void getAds(String url) {
        page=0;
        if (adapter != null) {
            adapter.clear();
        }
        presenterLayer.loadAdDataPage(page, size);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    private void showNumberOfAds(int numberOfAds) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setSubtitle("Ads: " + numberOfAds);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case Constants.REQUEST_ID_FOR_NEW_AD: {
                setMyAdsFlag(false);
                getAds(Urls.MAIN_SERVER_URL + Urls.GET_ALL_ADS_URL);
                break;
            }
            case Constants.REQUEST_ID_FOR_LOGIN: {

                if (getUserType().equals(Constants.FACEBOOK_USER)) {
                    //create new user in DB in case of first login
                    updateUserInDb(getUserName(), getUserId());

                    if (checkPlayServices() && isUserLoggedIn()) {
                        // Start IntentService to register this application with GCM.
                        Intent intent = new Intent(this, RegistrationIntentService.class);
                        startService(intent);
                    }
                }

                if (getUserType().equals(Constants.EMAIL_USER) && isUserLoggedIn()) {
                    //create new user in DB in case of first login
                    updateUserInDb(getUserName(), getUserId());

                    //request Token from GCM and update in DB
                    if (checkPlayServices() && isUserLoggedIn()) {
                        // Start IntentService to register this application with GCM.
                        Intent intent = new Intent(this, RegistrationIntentService.class);
                        startService(intent);
                    }
                }

                if (getUserType().equals(Constants.GOOGLE_USER) && isUserLoggedIn()) {
                    //create new user in DB in case of first login
                    updateUserInDb(getUserName(), getUserId());

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
                Log.d("CONAN: ", "Return from login, userid: " + getUserId());
                break;
            }
            case Constants.REQUEST_ID_FOR_OPEN_AD: {
                setMyAdsFlag(false);
                getAds(Urls.MAIN_SERVER_URL + Urls.GET_ALL_ADS_URL);
                break;
            }
            case Constants.REQUEST_ID_FOR_SEARCH: {
                if (data != null) {
                    String keyword = data.getStringExtra(Constants.KEYWORDS);
                    String priceFrom = data.getStringExtra(Constants.PRICE_FROM);
                    String priceTo = data.getStringExtra(Constants.PRICE_TO);
                    setMyAdsFlag(false);
                    getAds(Urls.MAIN_SERVER_URL + Urls.GET_ADS_FOR_KEYWORD_URL + keyword
                            + "&priceFrom=" + priceFrom + "&priceTo=" + priceTo);
                    drawer.closeDrawer(GravityCompat.START);
                }
                break;
            }
            case Constants.REQUEST_ID_FOR_SETTINGS: {
                updateLoginButton();
                setProfileName(getUserName());
                if (!isUserLoggedIn()) {
                    setProfileName("Please login...");
                }
                setProfilePicture(null);
                break;
            }
        }
    }


    private void updateUserInDb(String name, String userId) {
        String url = Urls.MAIN_SERVER_URL + Urls.CREATE_USER + "?name=" + name.replaceAll(" ", "%20") + "&id=" + userId;
        volleyService.sendStringGetRequest(url);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        setProfileName(getUserName());
        if (!isUserLoggedIn()) {
            setProfileName("Please login...");
        }
        setProfilePicture(null);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawer != null) {
            if (drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START);
            } else {
                super.onBackPressed();
            }
        }
    }

    private void updateLoginButton() {
        if (getUserId().equals("")) {
            Log.d("CONAN: ", "enable login button");
            loginBtn.setEnabled(true);
            loginBtn.setVisibility(View.VISIBLE);
            loginBtn.setOnClickListener((view) -> startLoginActivity());
            presenterLayer.getFacebookUserInfo();  //TODO richtig hier?
        } else {
            Log.d("CONAN: ", "disable login button");
            loginBtn.setEnabled(false);
            loginBtn.setVisibility(View.GONE);
            presenterLayer.getFacebookUserInfo();  //TODO richtig hier?
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        final String userId = getUserId();

        switch (item.getItemId()) {
            case R.id.myads: {
                if (userId.equals("")) {
                    startLoginActivity();
                    return true;
                } else {
                    page = 0;
                    size = 10;

                    /*
                    http://raent.de:9876/api/V2/ads/my?token=EAAHnEGldaZCgBAGiqRhQ6Bx1Eoyt3IvnIOFjUxPLfVsYdKqUKmUCVIYkVoGf0SZBdqEmDC09lalsopHWQ6aooD50d2YTh3fe1efLTmP2XK5FiAWb4QeDcJbcJbitCByKenukwkz63BVb9QnK1TobqrCQPgupAZD&page=0&size=10
                     */
                    //getAds(Urls.MAIN_SERVER_URL + Urls.GET_ALL_ADS_FROM_USER + userId);
                    setMyAdsFlag(true);
                    presenterLayer.getAdsForUser(page, size, getUserToken());
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
                getAds(Urls.MAIN_SERVER_URL + Urls.GET_ALL_ADS_URL);
                if (drawer != null) drawer.closeDrawer(GravityCompat.START);
                return true;
            }
            case R.id.bookmarks: {
                if (userId.equals("")) {
                    startLoginActivity();
                    return true;
                } else {
                    setMyAdsFlag(false);
                    getAds(Urls.MAIN_SERVER_URL + Urls.GET_BOOKMARKED_ADS_URL + userId);
                    if (drawer != null) drawer.closeDrawer(GravityCompat.START);
                    return true;
                }
            }
            case R.id.messages_from_user: {
                if (userId.equals("")) {
                    startLoginActivity();
                    return true;
                } else {
                    final Intent msgIntent = new Intent(this, MessagesOverviewActivity.class);
                    msgIntent.putExtra(Constants.USER_ID, userId);
                    startActivityForResult(msgIntent, Constants.REQUEST_ID_FOR_MESSAGES);
                    return true;
                }
            }
        }

        if (drawer != null) drawer.closeDrawer(GravityCompat.START);
        return super.onOptionsItemSelected(item);
    }

    private String getUserToken() {
        return getSharedPreferences(SHARED_PREFS_USER_INFO, 0).getString(Constants.USER_TOKEN, "");
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
        ImageView proPic = (ImageView) findViewById(R.id.profile_image);
        if (uri != null) {
            Picasso.with(getApplicationContext()).load(uri.toString()).into(proPic);
        } else {
            if (proPic != null) {
                proPic.setImageResource(R.drawable.applogo);
            }
        }
    }

    private void setUserPreferences(String name, String userId) {
        SharedPreferences settings = getSharedPreferences(SHARED_PREFS_USER_INFO, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(Constants.USER_NAME, name);
        editor.putString(Constants.USER_ID, userId);
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

    private boolean isMyAdsRequest() {
        return getSharedPreferences(SHOW_MY_ADS, 0).getBoolean(IS_MY_ADS, false);
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
}