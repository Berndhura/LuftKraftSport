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
import android.preference.PreferenceManager;
import android.provider.Settings;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import de.wichura.lks.controller.AdListController;
import de.wichura.lks.controller.LoginUiController;
import de.wichura.lks.controller.MessageBadgeController;
import de.wichura.lks.controller.PushTokenController;
import de.wichura.lks.controller.SectionNavigationController;
import de.wichura.lks.util.SessionStore;

import de.wichura.lks.R;
import de.wichura.lks.activity.LoginActivity;
import de.wichura.lks.activity.MessagesOverviewActivity;
import de.wichura.lks.dialogs.WelcomeDialog;
import de.wichura.lks.http.Service;
import de.wichura.lks.models.AdsAndBookmarks;
import de.wichura.lks.presentation.MainPresenter;
import static de.wichura.lks.mainactivity.Constants.SHARED_PREFS_USER_INFO;

public class MainActivity extends AppCompatActivity implements
        SharedPreferences.OnSharedPreferenceChangeListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    public AbsListView listView;
    public CircularProgressIndicator progressBar;
    public SwipeRefreshLayout swipeContainer;

    private static final String TAG = "CONAN";
    private ImageView loginBtn;
    private ImageView messagesBtn;
    private DrawerLayout drawer;

    private PushTokenController pushTokenController;
    private MessageBadgeController messageBadgeController;
    private LoginUiController loginUiController;
    private AdListController adListController;
    private SectionNavigationController sectionNavigation;
    private SessionStore session;

    private ActivityResultLauncher<Intent> newAdLauncher;
    private ActivityResultLauncher<Intent> loginLauncher;
    private ActivityResultLauncher<Intent> openAdLauncher;
    private ActivityResultLauncher<Intent> searchLauncher;
    private ActivityResultLauncher<Intent> settingsLauncher;
    private ActivityResultLauncher<Intent> messagesLauncher;

    //login
    private BroadcastReceiver mLoginBroadcastReceiver;
    private boolean isLoginReceiverRegistered;

    private MainPresenter presenterLayer;

    private GoogleApiClient mGoogleApiClient;

    //empty view for network problems
    private View noResultsView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Result launchers must be registered before the Activity reaches STARTED.
        registerActivityLaunchers();

        Service service = Service.get();
        presenterLayer = new MainPresenter(this, service, getApplicationContext());
        pushTokenController = new PushTokenController(this, presenterLayer);
        session = new SessionStore(this);

        //Google Api client
        //initGoogleApiClient();
        if (MainApp.getGoogleApiHelper().isConnected()) {
            //Get google api client
            mGoogleApiClient = MainApp.getGoogleApiHelper().getGoogleApiClient();
            Log.d("CONAN", "google client connected in MainActivity!");
            mGoogleApiClient.connect();
        }

        checkLocationServiceEnabled();

        //TODO last location hier holen oder in onConnect nutzen?
        presenterLayer.getLastKnownLocation();

        //TODO: set active false in messageActivity in onDestroy, onStop, on???  BUT NOT HERE
        SharedPreferences sp = getSharedPreferences(Constants.MESSAGE_ACTIVITY, MODE_PRIVATE);
        SharedPreferences.Editor ed = sp.edit();
        ed.putBoolean("active", false);
        ed.putString("adId", "");
        ed.apply();

        //load main layout
        setContentView(R.layout.activity_main);
        listView = findViewById(R.id.main_list);
        noResultsView = findViewById(R.id.empty_list_view);

        //ProgressBar
        progressBar = findViewById(R.id.progressBar);
        messagesBtn = findViewById(R.id.main_mail_button);
        messageBadgeController = new MessageBadgeController(this, messagesBtn);

        messagesBtn.setOnClickListener(v -> {
            messageBadgeController.hideButton();
            final Intent msgIntent = new Intent(getApplicationContext(), MessagesOverviewActivity.class);
            msgIntent.putExtra(Constants.USER_ID, session.getUserId());
            messagesLauncher.launch(msgIntent);
        });

        //configure Flurry for analysis
        //configureFlurry();

        //load toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //init drawer
        drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        //init navigationbar
        loginBtn = findViewById(R.id.main_login_button);
        loginUiController = new LoginUiController(this, loginBtn, session, presenterLayer,
                this::startLoginActivity,
                () -> pushTokenController.refreshToken());
        loginUiController.refreshLoginButton();

        swipeContainer = findViewById(R.id.swipeContainer);
        adListController = new AdListController(this, presenterLayer, session,
                listView, swipeContainer, noResultsView, progressBar,
                () -> sectionNavigation.resetFlags(),
                openAdLauncher, newAdLauncher);

        sectionNavigation = new SectionNavigationController(this, drawer,
                findViewById(R.id.search_again), session, adListController,
                messageBadgeController, this::startLoginActivity,
                newAdLauncher, searchLauncher, settingsLauncher, messagesLauncher);
        NavigationView navigationView = findViewById(R.id.nav_view);
        if (navigationView != null) navigationView.setNavigationItemSelectedListener(sectionNavigation);

        pushTokenController.register();

        messageBadgeController.register();

        mLoginBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //TODO was hier??
            }
        };

        registerLoginReceiver();

        if (getIntent().getStringExtra(Constants.USER_ID_FROM_AD) != null) {
            String userId = getIntent().getStringExtra(Constants.USER_ID_FROM_AD);
            sectionNavigation.resetFlags();
            presenterLayer.searchForArticles(0, adListController.pageSize(),
                    null,
                    null,
                    Constants.DISTANCE_INFINITY,
                    null,
                    userId); //userId
        } else {
            sectionNavigation.resetFlags();
            adListController.loadType(Constants.TYPE_ALL);
        }

        pushTokenController.refreshToken();

        showWelcomeDialog();
    }

    private void showWelcomeDialog() {
        if (getSharedPreferences(Constants.SHARED_PREFS_WELCOME_DIALOG, 0).getBoolean(Constants.SHOW_WELCOME, true)) {
            new WelcomeDialog().show(getSupportFragmentManager(), null);
        }
    }

    public void checkLocationServiceEnabled() {
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (!manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            //TODO was macht das hier?? hab ich vergessen
            //Toast.makeText(this, "Location Service ausgeschaltet", Toast.LENGTH_LONG).show();
            saveLocationServiceStatus(false);
        } else {
            saveLocationServiceStatus(true);
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    public GoogleApiClient getGoogleApiClient() {
        return mGoogleApiClient;
    }

    protected void onStop() {

        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (loginUiController != null) loginUiController.destroy();
        if (presenterLayer.disposable != null && !presenterLayer.disposable.isDisposed()) {
            presenterLayer.disposable.dispose();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
        pushTokenController.register();
        registerLoginReceiver();
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        sp.registerOnSharedPreferenceChangeListener(this);
        adListController.onResume();
    }

    @Override
    protected void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        adListController.onSaveInstanceState(state);
    }

    @Override
    protected void onRestoreInstanceState(Bundle state) {
        super.onRestoreInstanceState(state);
        adListController.onRestoreInstanceState(state);
    }

    @Override
    protected void onPause() {
        super.onPause();
        pushTokenController.unregister();

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
        adListController.updateAds(elements, type, priceFrom, priceTo, distance, description, userId);
    }

    public void showProblem(String type) {
        adListController.showProblem(type);
    }

    public void hideEmptyView() {
        adListController.hideEmptyView();
    }

    public void addMoreAdsToList(AdsAndBookmarks elements) {
        adListController.addMoreAdsToList(elements);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    private void registerActivityLaunchers() {
        ActivityResultContracts.StartActivityForResult contract =
                new ActivityResultContracts.StartActivityForResult();

        newAdLauncher = registerForActivityResult(contract, result -> {
            Intent data = result.getData();
            // my articles -> edit one -> just go back -> still show my articles
            if (data != null && data.getStringExtra(Constants.IS_EDIT_MODE) != null) {
                sectionNavigation.setMyAdsFlag(true);
                adListController.loadType(Constants.TYPE_USER);
                return;
            }
            // Android M permission for Read/Write -> ask user again
            if (data != null && data.getStringExtra(Constants.PERMISSION_DENIED) != null) {
                sectionNavigation.closeDrawer();
                showRequestForPermission();
            }
            sectionNavigation.resetFlags();
            adListController.loadType(Constants.TYPE_ALL);
        });

        loginLauncher = registerForActivityResult(contract, result -> {
            if (session.isLoggedIn()) {
                if (Constants.GOOGLE_USER.equals(session.getUserType())) {
                    presenterLayer.sendUserPicToServer(session.getUserProfilePicture());
                }
                pushTokenController.refreshToken();
            }
            loginUiController.refreshLoginButton();
            loginUiController.refreshProfileHeader();
            Log.d("CONAN", "Return from login, userid: " + session.getUserId());

            sectionNavigation.resetFlags();
            hideEmptyView();
            adListController.loadType(Constants.TYPE_ALL);
        });

        openAdLauncher = registerForActivityResult(contract, result -> {
            Intent data = result.getData();
            // article was deleted -> remove from list
            if (data != null) {
                adListController.removeItemAt(data.getIntExtra(Constants.POSITION_IN_LIST, 0));
            }
        });

        searchLauncher = registerForActivityResult(contract, result -> {
            Intent data = result.getData();
            if (data == null) return;
            String keyword = data.getStringExtra(Constants.KEYWORDS);
            String priceFrom = data.getStringExtra(Constants.PRICE_FROM);
            String priceTo = data.getStringExtra(Constants.PRICE_TO);
            int distance = data.getIntExtra(Constants.DISTANCE, Constants.DISTANCE_INFINITY);
            sectionNavigation.rememberSearch(keyword, priceFrom, priceTo);
            sectionNavigation.resetFlags();

            presenterLayer.searchForArticles(0, adListController.pageSize(),
                    priceFrom.equals("") ? null : Integer.parseInt(priceFrom),
                    priceTo.equals("") ? null : Integer.parseInt(priceTo),
                    distance,
                    keyword,
                    null);
            sectionNavigation.closeDrawer();
            sectionNavigation.showSearchAgainButton();
        });

        settingsLauncher = registerForActivityResult(contract, result -> {
            loginUiController.refreshLoginButton();
            loginUiController.refreshProfileHeader();
        });

        messagesLauncher = registerForActivityResult(contract, result -> {
            // No-op: the badge/broadcast handles state changes on return.
        });
    }

    private void showRequestForPermission() {
        Snackbar snackbar = Snackbar.make(findViewById(R.id.snackbarPosition), "Berechtigungen bearbeiten?",
                Snackbar.LENGTH_LONG).setAction("Los!", view -> {
            Intent i = new Intent();
            i.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            i.setData(Uri.parse("package:" + getApplicationContext().getPackageName()));
            i.addCategory(Intent.CATEGORY_DEFAULT);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            startActivity(i);
        });
        snackbar.setActionTextColor(getResources().getColor(R.color.white_smoke));
        snackbar.show();
    }

    @Override
    public void onBackPressed() {
        if (sectionNavigation.handleBackPressed()) return;
        if (isTaskRoot()) {
            new ExitDialogFragment().show(getSupportFragmentManager(), null);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        loginUiController.refreshProfileHeader();
        return true;
    }

    private void startLoginActivity() {
        loginLauncher.launch(new Intent(this, LoginActivity.class));
    }

    /** Kept for callsites in adapters/dialogs that still need a public API. */
    public void setProfilePicture(Uri uri) {
        loginUiController.setProfilePicture(uri);
    }

    public void setProfileName(String name) {
        loginUiController.setProfileName(name);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.d("CONAN", "KEY:" + key);
        if (key.equals(SHARED_PREFS_USER_INFO)) {
            Log.d("CONAN", "jooooooooooo");
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
        adListController.showNumberOfAds(numberOfAds);
    }

    public void setMainTitle(String title) {
        adListController.setMainTitle(title);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d("CONAN", "in mainActivity in onConnected");
        ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE);
        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (mLastLocation != null) {
            double lat = mLastLocation.getLatitude();
            double lng = mLastLocation.getLongitude();
            if (lat == 0 && lng == 0) {
                saveLocationServiceStatus(false);
                Log.d("CONAN", "last position in onConnected: " + lat + " : " + lng);
            } else {
                Log.d("CONAN", "last position in onConnected: " + lat + " : " + lng);
                saveLastPosition(lat, lng);
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d("CONAN", "onConnectionSuspended: " + i);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d("CONAN", "onConnectionFailed: " + connectionResult.toString());
    }
}