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
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.flurry.android.FlurryAgent;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import rx.Subscription;
import wichura.de.camperapp.R;
import wichura.de.camperapp.ad.MyAdsActivity;
import wichura.de.camperapp.ad.NewAdActivity;
import wichura.de.camperapp.ad.OpenAdActivity;
import wichura.de.camperapp.gcm.QuickstartPreferences;
import wichura.de.camperapp.gcm.RegistrationIntentService;
import wichura.de.camperapp.http.MyVolley;
import wichura.de.camperapp.http.Service;
import wichura.de.camperapp.http.Urls;
import wichura.de.camperapp.http.VolleyService;
import wichura.de.camperapp.messages.MessagesOverviewActivity;
import wichura.de.camperapp.models.AdsAndBookmarks;
import wichura.de.camperapp.models.RowItem;
import wichura.de.camperapp.presentation.PresenterLayer;

public class MainActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener {

    private ListView listView;
    private List<RowItem> rowItems;
    private CustomListViewAdapter adapter;

    private static final String TAG = "CONAN";
    private ImageView loginBtn;

    CallbackManager callbackManager;
    private DrawerLayout drawer;

    //Google Cloud Messages
    private BroadcastReceiver mGcmRegistrationBroadcastReceiver;
    private ProgressBar mGcmRegistrationProgressBar;
    private boolean isGcmReceiverRegistered;

    //login
    private BroadcastReceiver mLoginBroadcastReceiver;
    private boolean isLoginReceiverRegistered;

    //Volley Http service
    private VolleyService volleyService;

    private Subscription subscription;

    private PresenterLayer presenterLayer;
    private Service service;

    public MainActivity() {
        volleyService = new VolleyService(MainActivity.this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize Volley
        MyVolley.init(this);

        service = new Service();

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
        getAds(Urls.MAIN_SERVER_URL + Urls.GET_ALL_ADS_URL);

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

        mGcmRegistrationProgressBar = (ProgressBar) findViewById(R.id.registrationProgressBar);
        mGcmRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mGcmRegistrationProgressBar.setVisibility(ProgressBar.GONE);
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
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerGcmReceiver();
        registerLoginReceiver();
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mGcmRegistrationBroadcastReceiver);
        isGcmReceiverRegistered = false;

        LocalBroadcastManager.getInstance(this).unregisterReceiver(mLoginBroadcastReceiver);
        isLoginReceiverRegistered = false;

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

    private void configureFlurry() {
        FlurryAgent.setLogEnabled(true);
        FlurryAgent.setCaptureUncaughtExceptions(true);
        FlurryAgent.setLogLevel(Log.ERROR);
        FlurryAgent.init(this, "3Q9GDM9TDX77WDGBN25S");
        FlurryAgent.logEvent("mainactivity started");
    }

    private void getFacebookUserInfo() {

        callbackManager = CallbackManager.Factory.create();
        GraphRequest request = GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(JSONObject object, GraphResponse response) {
                JSONObject json = response.getJSONObject();
                try {
                    if (json != null) {
                        //user id
                        Log.d("CONAN: ", "user id facebook: " + json.getString("id"));
                        String userId = json.getString("id");
                        String userName = json.getString("name");
                        Log.d("CONAN: ", "user name facebook: " + json.getString("name"));
                        setProfileName(userName);
                        //user profile picture
                        Profile profile = Profile.getCurrentProfile();
                        if (profile != null) {
                            Uri uri = profile.getProfilePictureUri(200, 200);
                            setProfilePicture(uri);
                        }
                        setUserPreferences(userName, userId);
                        getAds(Urls.MAIN_SERVER_URL + Urls.GET_ALL_ADS_URL);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.d("CONAN: ", "Do the login ");
                }
            }
        });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,link,picture");
        request.setParameters(parameters);
        request.executeAsync();
    }

    public void updateAds(AdsAndBookmarks elements) {

        rowItems = new ArrayList<>();
        for (RowItem e : elements.getAds()) {
            rowItems.add(e);
        }

        showNumberOfAds(elements.getAds().size());

        listView = (ListView) findViewById(R.id.main_list);
        adapter = new CustomListViewAdapter(
                getApplicationContext(),
                R.layout.list_item, rowItems,
                elements.getBookmarks());

        listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> arg0, final View arg1, final int position, final long arg3) {

                final RowItem rowItem = (RowItem) listView.getItemAtPosition(position);
                final Intent intent = new Intent(getApplicationContext(), OpenAdActivity.class);
                intent.putExtra(Constants.URI, rowItem.getUrl());
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
            }
        });
        setProgressBarIndeterminateVisibility(false);
    }

    private void getAds(String url) {

        presenterLayer = new PresenterLayer(this, service, getApplicationContext());
        presenterLayer.loadAdData(url);

    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        //final int id = item.getItemId();
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
                getAds(Urls.MAIN_SERVER_URL + Urls.GET_ALL_ADS_URL);
                break;
            }
            case Constants.REQUEST_ID_FOR_FACEBOOK_LOGIN: {

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

                updateLoginButton();
                setProfileName(getUserName());
                if (!isUserLoggedIn()) {
                    setProfileName("Please login...");
                }
                setProfilePicture(null);
                Log.d("CONAN: ", "Return from login, userid: " + getUserId());
                break;
            }
            case Constants.REQUEST_ID_FOR_OPEN_AD: {
                getAds(Urls.MAIN_SERVER_URL + Urls.GET_ALL_ADS_URL);
                break;
            }
            case Constants.REQUEST_ID_FOR_SEARCH: {
                if (data != null) {
                    String query = data.getStringExtra(Constants.KEYWORDS);
                    getAds(Urls.MAIN_SERVER_URL + Urls.GET_ADS_FOR_KEYWORD_URL + query);
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
            loginBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startLoginActivity();
                }
            });
            getFacebookUserInfo();  //TODO richtig hier?
        } else {
            Log.d("CONAN: ", "disable login button");
            loginBtn.setEnabled(false);
            loginBtn.setVisibility(View.GONE);
            getFacebookUserInfo();  //TODO richtig hier?
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
                    Intent intent = new Intent(getApplicationContext(), MyAdsActivity.class);
                    intent.putExtra(Constants.USER_ID, userId);
                    startActivityForResult(intent, Constants.REQUEST_ID_FOR_MY_ADS);
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
                getAds(Urls.MAIN_SERVER_URL + Urls.GET_ALL_ADS_URL);
                if (drawer != null) drawer.closeDrawer(GravityCompat.START);
                return true;
            }
            case R.id.bookmarks: {
                if (userId.equals("")) {
                    startLoginActivity();
                    return true;
                } else {
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

    private void startLoginActivity() {
        final Intent facebookIntent = new Intent(this, FbLoginActivity.class);
        startActivityForResult(facebookIntent, Constants.REQUEST_ID_FOR_FACEBOOK_LOGIN);
    }

    private void setProfilePicture(Uri uri) {
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
        SharedPreferences settings = getSharedPreferences("UserInfo", 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(Constants.USER_NAME, name);
        editor.putString(Constants.USER_ID, userId);
        editor.apply();
    }

    private void setProfileName(String name) {
        TextView nav_user = (TextView) findViewById(R.id.username);
        if (nav_user != null) nav_user.setText(name);
    }

    private String getUserName() {
        return getSharedPreferences("UserInfo", 0).getString(Constants.USER_NAME, "");
    }

    private String getUserId() {
        return getSharedPreferences("UserInfo", 0).getString(Constants.USER_ID, "");
    }

    private Boolean isUserLoggedIn() {
        return !getUserId().equals("");
    }

    private String getUserType() {
        return getSharedPreferences("UserInfo", 0).getString(Constants.USER_TYPE, "");
    }
}