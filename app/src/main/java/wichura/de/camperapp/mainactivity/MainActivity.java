package wichura.de.camperapp.mainactivity;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import wichura.de.camperapp.R;
import wichura.de.camperapp.ad.MyAdsActivity;
import wichura.de.camperapp.ad.NewAdActivity;
import wichura.de.camperapp.ad.OpenAdActivity;
import wichura.de.camperapp.http.Urls;

public class MainActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private ListView listView;
    private List<RowItem> rowItems;
    private CustomListViewAdapter adapter;

    public static final int REQUEST_ID_FOR_NEW_AD = 1;
    public static final int REQUEST_ID_FOR_FACEBOOK_LOGIN = 2;
    public static final int REQUEST_ID_FOR_OPEN_AD = 3;
    public static final int REQUEST_ID_FOR_MY_ADS = 4;
    private static final int REQUEST_ID_FOR_SEARCH = 5;

    private String facebookId;
    private String userName;

    private GoogleApiClient mGoogleApiClient;


    CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setProgressBarIndeterminateVisibility(true);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.mipmap.ic_launcher);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //facebook login
        if (AccessToken.getCurrentAccessToken() != null) {
            getFacebookUserInfo();
        } else {
            Log.d("CONAN: ", "NOPE ");
            getFacebookUserInfo();
        }

        //google login
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestProfile()
                .requestId()
                .requestScopes(new Scope(Scopes.PLUS_ME))
                .requestScopes(new Scope(Scopes.PLUS_LOGIN))
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                //TODO: this does not work....
                .enableAutoManage(this, null /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

//        Intent startPageIntent = new Intent(getApplicationContext(), StartActivity.class);
//        startActivityForResult(startPageIntent,45);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null)
            mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {
//        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
//                mGoogleApiClient);
//        if (mLastLocation != null) {
//            mLatitudeText.setText(String.valueOf(mLastLocation.getLatitude()));
//            mLongitudeText.setText(String.valueOf(mLastLocation.getLongitude()));
//        }

        Log.d("CONAN: ", "google+ connected... ");
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

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
                        facebookId = json.getString("id");

                        //user name
                        userName = json.getString("name");
                        setProfileName(userName);

                        //user profile picture
                        Profile profile = Profile.getCurrentProfile();
                        if (profile != null) {
                            Uri uri = profile.getProfilePictureUri(200, 200);
                            setProfilePicture(uri);
                        } else {
                            //TODO: und nu
                        }
                        //TODO auch ohne login moeglich
                        getAdsJsonForKeyword(Urls.MAIN_SERVER_URL + Urls.GET_ALL_ADS_URL);
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

    private void setProfilePicture(Uri uri) {
        ImageView profilePic = (ImageView) findViewById(R.id.profile_image);
        Picasso.with(getApplicationContext()).load(uri.toString()).into(profilePic);
    }

    private void setProfileName(String name) {
        TextView nav_user = (TextView) findViewById(R.id.username);
        nav_user.setText(name);
    }

    private void getAdsJsonForKeyword(String url) {
        RequestQueue queue = Volley.newRequestQueue(this);

        JsonArrayRequest getAllAdsInJson = new JsonArrayRequest(
                Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                Context context = getApplicationContext();
                try {
                    final Gson gson = new GsonBuilder()
                            .excludeFieldsWithoutExposeAnnotation().create();

                    final JSONArray listOfAllAds = new JSONArray(response.toString());
                    rowItems = new ArrayList<RowItem>();
                    for (int i = 0; i < listOfAllAds.length(); i++) {
                        // get the titel information JSON object
                        final String title = listOfAllAds.getJSONObject(i)
                                .toString();
                        //use RowItem class to get from GSON
                        final RowItem rowItem = gson.fromJson(title, RowItem.class);

                        rowItems.add(rowItem);
                    }
                } catch (final JSONException e) {
                    e.printStackTrace();
                }

                listView = (ListView) findViewById(R.id.main_list);
                adapter = new CustomListViewAdapter(
                        context, R.layout.list_item, rowItems);
                listView.setAdapter(adapter);
                adapter.notifyDataSetChanged();

                Toast.makeText(getApplicationContext(), "Results: " + rowItems.size(), Toast.LENGTH_LONG).show();

                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                    @Override
                    public void onItemClick(final AdapterView<?> arg0,
                                            final View arg1, final int position, final long arg3) {

                        final RowItem rowItem = (RowItem) listView.getItemAtPosition(position);

                        //open new details page with sel. item
                        final Intent intent = new Intent(getApplicationContext(),
                                OpenAdActivity.class);
                        intent.putExtra(Constants.URI, rowItem.getUrl());
                        intent.putExtra(Constants.ID, rowItem.getAdId());
                        intent.putExtra(Constants.TITLE, rowItem.getTitle());
                        intent.putExtra(Constants.DESCRIPTION, rowItem.getDescription());
                        intent.putExtra(Constants.LOCATION, rowItem.getLocation());
                        intent.putExtra(Constants.PHONE, rowItem.getPhone());
                        intent.putExtra(Constants.PRICE, rowItem.getPrice());
                        intent.putExtra(Constants.DATE, rowItem.getDate());
                        intent.putExtra("userid", rowItem.getUserid());
                        intent.putExtra(Constants.USER_ID, facebookId); //TODO refactor to general user id (google+,facebook,myId)
                        startActivityForResult(intent, REQUEST_ID_FOR_OPEN_AD);
                    }
                });
                setProgressBarIndeterminateVisibility(false);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), "Missing network connection!\n" + error.toString(), Toast.LENGTH_LONG).show();
            }
        });
        queue.add(getAllAdsInJson);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        final int id = item.getItemId();
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ID_FOR_NEW_AD) {

            getAdsJsonForKeyword(Urls.MAIN_SERVER_URL + Urls.GET_ALL_ADS_URL);
        }
        //back from Facebock login/logout page
        if (requestCode == REQUEST_ID_FOR_FACEBOOK_LOGIN) {
            //TODO:possible null if user login is Google+
            //facebookId = data.getStringExtra(Constants.FACEBOOK_ID);
            //String fbToken = data.getStringExtra(Constants.FACEBOOK_ACCESS_TOKEN);
            Log.d("CONAN: ", "Return from Facebook login, userid: " + facebookId);

            //load new Options Menu cause of user is logged in now
            invalidateOptionsMenu();
            //set Profile pic with URL:
        }

        if (requestCode == REQUEST_ID_FOR_OPEN_AD) {
            getAdsJsonForKeyword(Urls.MAIN_SERVER_URL + Urls.GET_ALL_ADS_URL);
        }

        if (requestCode == REQUEST_ID_FOR_SEARCH) {
            String query = data.getStringExtra("KEYWORDS");
            getAdsJsonForKeyword(Urls.MAIN_SERVER_URL + Urls.GET_ADS_FOR_KEYWORD_URL + query);
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            drawer.closeDrawer(GravityCompat.START);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        int id = item.getItemId();
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        if (id == R.id.myads) {
            Intent intent = new Intent(getApplicationContext(), MyAdsActivity.class);
            intent.putExtra("userid", facebookId);
            startActivityForResult(intent, REQUEST_ID_FOR_MY_ADS);
        }
        if (id == R.id.new_ad) {
            final Intent intent = new Intent(this, NewAdActivity.class);
            intent.putExtra("id", facebookId);
            startActivityForResult(intent, REQUEST_ID_FOR_NEW_AD);
            return true;

        } else if (id == R.id.search) {
            final Intent searchIntent = new Intent(this, SearchActivity.class);
            startActivityForResult(searchIntent, REQUEST_ID_FOR_SEARCH);
            return true;

        } else if (id == R.id.login_out) {
            final Intent facebookIntent = new Intent(this, FbLoginActivity.class);
            startActivityForResult(facebookIntent, REQUEST_ID_FOR_FACEBOOK_LOGIN);
            return true;

        } else if (id == R.id.refresh) {
            getAdsJsonForKeyword(Urls.MAIN_SERVER_URL + Urls.GET_ALL_ADS_URL);
            drawer.closeDrawer(GravityCompat.START);
            return true;
        } else if (id == R.id.bookmarks) {
            getAdsJsonForKeyword(Urls.MAIN_SERVER_URL + Urls.GET_BOOKMARKED_ADS_URL + facebookId);
            drawer.closeDrawer(GravityCompat.START);
            return true;
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
