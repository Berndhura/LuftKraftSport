package wichura.de.camperapp.mainactivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
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
import com.flurry.android.FlurryAgent;
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
        NavigationView.OnNavigationItemSelectedListener {

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
    private Boolean isUserLogedIn;

    private ImageView loginBtn;

    CallbackManager callbackManager;
    private ImageView profilePic;
    private DrawerLayout drawer;
    private String userIdForEmailUser;
    private String userNameForEmailUser;
    private String userType="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //get Facebook access token
        FacebookSdk.sdkInitialize(getApplicationContext());

        //load main layout
        setContentView(R.layout.activity_main);

        //configure Flurry for analysis
        configureFlurry();

        //load toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setLogo(R.mipmap.ic_launcher);
            getSupportActionBar().setDisplayUseLogoEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        //init drawer
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        //init navigationbar
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        if (navigationView != null) navigationView.setNavigationItemSelectedListener(this);

        //
        loginBtn = (ImageView) findViewById(R.id.login_button);

        updateLoginStatus();
        updateLoginButton();
        getAdsJsonForKeyword(Urls.MAIN_SERVER_URL + Urls.GET_ALL_ADS_URL);
    }

    private void updateLoginStatus() {
        SharedPreferences settings = getSharedPreferences("UserInfo", 0);
        userNameForEmailUser = settings.getString(Constants.USER_NAME, "");
        userIdForEmailUser = settings.getString(Constants.USER_ID, "");
        userType = settings.getString(Constants.USER_TYPE, "");
        isUserLogedIn = true;
        //setProfilePicture(null);
        //setProfileName(userNameForEmailUser);
        Log.d("CONAN: ", "user name, id: " + userNameForEmailUser + ", "+ userIdForEmailUser);
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
                        facebookId = json.getString("id");
                        userName = json.getString("name");
                        userType = Constants.FACEBOOK_USER;
                        Log.d("CONAN: ", "user name facebook: " + json.getString("name"));
                        setProfileName(userName);
                        //user profile picture
                        Profile profile = Profile.getCurrentProfile();
                        if (profile != null) {
                            Uri uri = profile.getProfilePictureUri(200, 200);
                            setProfilePicture(uri);
                            isUserLogedIn = true;
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
        ImageView proPic = (ImageView) findViewById(R.id.profile_image);
        if (uri != null) {
            Picasso.with(getApplicationContext()).load(uri.toString()).into(proPic);
        } else {
            proPic.setImageResource(R.drawable.applogo);
        }
    }

    private void setProfileName(String name) {
        TextView nav_user = (TextView) findViewById(R.id.username);
        if (nav_user != null) nav_user.setText(name);
    }

    private void getAdsJsonForKeyword(String url) {
        RequestQueue queue = Volley.newRequestQueue(this);
        JsonArrayRequest getAllAdsInJson = new JsonArrayRequest(
                Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                Context context = getApplicationContext();
                try {
                    final Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
                    final JSONArray listOfAllAds = new JSONArray(response.toString());
                    rowItems = new ArrayList<>();
                    for (int i = 0; i < listOfAllAds.length(); i++) {
                        // get the title information JSON object
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
                        intent.putExtra(Constants.AD_ID, rowItem.getAdId());
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
        //final int id = item.getItemId();
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ID_FOR_NEW_AD) {

            getAdsJsonForKeyword(Urls.MAIN_SERVER_URL + Urls.GET_ALL_ADS_URL);
        }
        //back from Facebook login/logout page
        if (requestCode == REQUEST_ID_FOR_FACEBOOK_LOGIN) {

            if (data != null) { //just back from login page without data
                if (data.getStringExtra(Constants.USER_TYPE).equals(Constants.EMAIL_USER)) {
                    userIdForEmailUser = data.getStringExtra(Constants.EMAIL_USR_ID);
                    userNameForEmailUser = data.getStringExtra(Constants.USER_NAME);
                    userType = Constants.EMAIL_USER;
                    Log.d("CONAN: ", "email user name: " + userNameForEmailUser);
                    Log.d("CONAN: ", "email user id: " + userIdForEmailUser);

                    //update picture and name in drawer
                    invalidateOptionsMenu();

                    //save login data into shared preferences
                    SharedPreferences settings = getSharedPreferences("UserInfo", 0);
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putString(Constants.USER_NAME, userNameForEmailUser);
                    editor.putString(Constants.USER_ID, userIdForEmailUser);
                    editor.putString(Constants.USER_TYPE, Constants.EMAIL_USER);
                    editor.apply();
                    isUserLogedIn = true;

                    //updateloginButton()
                    getAdsJsonForKeyword(Urls.MAIN_SERVER_URL + Urls.GET_ALL_ADS_URL);
                }

                if (data.getStringExtra(Constants.USER_TYPE).equals(Constants.FACEBOOK_USER)) {
                    userType = Constants.FACEBOOK_USER;
                    getFacebookUserInfo();
                }
            }
            updateLoginButton();
            Log.d("CONAN: ", "Return from login, userid: " + facebookId);
           // invalidateOptionsMenu();
        }

        if (requestCode == REQUEST_ID_FOR_OPEN_AD) {
            getAdsJsonForKeyword(Urls.MAIN_SERVER_URL + Urls.GET_ALL_ADS_URL);
        }

        if (requestCode == REQUEST_ID_FOR_SEARCH) {
            String query = data.getStringExtra("KEYWORDS");
            getAdsJsonForKeyword(Urls.MAIN_SERVER_URL + Urls.GET_ADS_FOR_KEYWORD_URL + query);
            drawer.closeDrawer(GravityCompat.START);
        }
    }
    @Override
    public boolean  onCreateOptionsMenu(Menu menu) {
       //TODO works only for email user now
        Log.d("CONAN: ", "user name, id: " + userNameForEmailUser + ", "+ userIdForEmailUser);
        setProfileName(userNameForEmailUser);
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
        if (AccessToken.getCurrentAccessToken() != null) {
            Log.d("CONAN: ", "Facebook access token ok");
            loginBtn.setEnabled(false);
            loginBtn.setVisibility(View.GONE);
            isUserLogedIn = true;
            getFacebookUserInfo();
        } else {
            Log.d("CONAN: ", "Facebook access token null");
            isUserLogedIn = false;
            loginBtn.setEnabled(true);
            loginBtn.setVisibility(View.VISIBLE);
            loginBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d("CONAN: ", "open login page");
                    Intent i = new Intent(getApplicationContext(), FbLoginActivity.class);
                    startActivityForResult(i, REQUEST_ID_FOR_FACEBOOK_LOGIN);
                }
            });
            getFacebookUserInfo();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.myads) {
            Intent intent = new Intent(getApplicationContext(), MyAdsActivity.class);
            if (userType.equals(Constants.EMAIL_USER)) {
                intent.putExtra(Constants.USER_ID, userIdForEmailUser);
                startActivityForResult(intent, REQUEST_ID_FOR_MY_ADS);
            } else if (userType.equals(Constants.FACEBOOK_USER)) {
                intent.putExtra(Constants.USER_ID, facebookId);
                startActivityForResult(intent, REQUEST_ID_FOR_MY_ADS);
            } else {
                Log.d("CONAN: ", "no login data");
                Toast.makeText(getApplicationContext(),"Log in please...",Toast.LENGTH_LONG).show();
            }
        }
        if (id == R.id.new_ad) {
            final Intent intent = new Intent(this, NewAdActivity.class);
            if (userType.equals(Constants.EMAIL_USER)) {
                intent.putExtra(Constants.USER_ID, userIdForEmailUser);
            }
            if (userType.equals(Constants.FACEBOOK_USER)) {
                intent.putExtra(Constants.USER_ID, facebookId);
            }
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
            if (drawer != null) drawer.closeDrawer(GravityCompat.START);
            return true;
        } else if (id == R.id.bookmarks) {
            getAdsJsonForKeyword(Urls.MAIN_SERVER_URL + Urls.GET_BOOKMARKED_ADS_URL + facebookId);
            if (drawer != null) drawer.closeDrawer(GravityCompat.START);
            return true;
        }
        if (drawer != null) drawer.closeDrawer(GravityCompat.START);
        return super.onOptionsItemSelected(item);
    }
}