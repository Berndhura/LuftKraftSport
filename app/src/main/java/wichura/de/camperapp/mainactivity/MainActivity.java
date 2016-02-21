package wichura.de.camperapp.mainactivity;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
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
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import wichura.de.camperapp.R;
import wichura.de.camperapp.ad.NewAdActivity;
import wichura.de.camperapp.ad.OpenAdActivity;
import wichura.de.camperapp.http.Urls;

//farbcode bilder: #639bc5
public class MainActivity extends ActionBarActivity {

    private ListView listView;
    private List<RowItem> rowItems;
    private CustomListViewAdapter adapter;

    public static final int REQUEST_ID_FOR_NEW_AD = 1;
    public static final int REQUEST_ID_FOR_FACEBOOK_LOGIN = 2;
    public static final int REQUEST_ID_FOR_OPEN_AD = 3;

    private String facebookId;
    private String fbProfilePicUrl;

    CallbackManager callbackManager;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFacebookUserInfos();

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        //Show an image in place of the titles
        ImageView imageView = new ImageView(this);
        //imageView.setImageResource(R.drawable.uploadbutton);
        String uri = "https://graph.facebook.com/10208246429418599/picture?height=100&width=100&migration_overrides=%7Boctober_2012%3Atrue%7D";
        Picasso.with(getApplicationContext()).load(uri.toString()).into(imageView);
        imageView.setScaleType(ImageView.ScaleType.CENTER);
        ActionBar.LayoutParams lp = new ActionBar.LayoutParams(
                ActionBar.LayoutParams.MATCH_PARENT,
                ActionBar.LayoutParams.MATCH_PARENT);
        actionBar.setCustomView(imageView, lp);
        actionBar.setWindowTitle("gok");
        actionBar.setDisplayShowTitleEnabled(true);

        setContentView(R.layout.activity_main);
        getAdsJsonForKeyword(Urls.MAIN_SERVER_URL + Urls.GET_ALL_ADS_URL);
    }

    private void getFacebookUserInfos() {
        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();
        GraphRequest request = GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(JSONObject object, GraphResponse response) {
                JSONObject json = response.getJSONObject();
                try {
                    if (json != null) {
                        String text = "<b>Name :</b> " + json.getString("name") + "<br><br><b>Email :</b> " +"email" + "<br><br><b>Profile link :</b> " + json.getString("link");
                        Log.d("CONAN: ", "Return from Facebook login, token: " + text);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();

                }
            }
        });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,link,picture");
        request.setParameters(parameters);
        request.executeAsync();
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

                listView = (ListView) findViewById(R.id.list);
                adapter = new CustomListViewAdapter(
                        context, R.layout.list_item, rowItems);
                listView.setAdapter(adapter);
                adapter.notifyDataSetChanged();

                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                    @Override
                    public void onItemClick(final AdapterView<?> arg0,
                                            final View arg1, final int position, final long arg3) {

                        final RowItem rowItem = (RowItem) listView.getItemAtPosition(position);

                        //open new details page with sel. item
                        final Intent intent = new Intent(getApplicationContext(),
                                OpenAdActivity.class);
                        intent.putExtra("uri", rowItem.getUrl());
                        intent.putExtra("id", rowItem.getAdId());
                        intent.putExtra("title", rowItem.getTitle());
                        intent.putExtra("description", rowItem.getDescription());
                        intent.putExtra("location", rowItem.getLocation());
                        intent.putExtra("phone", rowItem.getPhone());
                        intent.putExtra("userid", rowItem.getUserid());
                        startActivityForResult(intent, REQUEST_ID_FOR_OPEN_AD);

                        Toast.makeText(getApplicationContext(), rowItem.getAdId(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
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
    public boolean onCreateOptionsMenu(final Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        SearchManager sM = (SearchManager) getSystemService(Context.SEARCH_SERVICE);

        final MenuItem searchMenuItem = menu.findItem(R.id.menu_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchMenuItem);

        int searchPlateId = searchView.getContext().getResources().getIdentifier("android:id/search_src_text", null, null);
        EditText searchPlate = (EditText) searchView.findViewById(searchPlateId);
        searchPlate.setTextColor(getResources().getColor(R.color.com_facebook_blue));
        searchPlate.setBackgroundResource(R.drawable.ic_action_settings);
        searchPlate.setImeOptions(EditorInfo.IME_ACTION_SEARCH);

        searchView.setSearchableInfo(sM.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(true);
        searchView.setSubmitButtonEnabled(true);


        if (searchView != null) {
            searchView.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    Log.d("query: ", query);
                    getAdsJsonForKeyword(Urls.MAIN_SERVER_URL + Urls.GET_ADS_FOR_KEYWORD_URL + query);
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    return false;
                }
            });
        }

        MenuItem loginItem = menu.findItem(R.id.login);
        MenuItem profileItem = menu.findItem(R.id.profile);

        if (facebookId != null) {
            loginItem.setVisible(false);
            profileItem.setVisible(true);
        } else {
            loginItem.setVisible(true);
            profileItem.setVisible(false);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        final int id = item.getItemId();

        if (id == R.id.new_ad) {
            final Intent intent = new Intent(this, NewAdActivity.class);
            startActivityForResult(intent, REQUEST_ID_FOR_NEW_AD);
            return true;
        }

        if (id == R.id.refresh) {
            getAdsJsonForKeyword(Urls.MAIN_SERVER_URL + Urls.GET_ALL_ADS_URL);
            return true;
        }

        if (id == R.id.login) {
            final Intent facebookIntent = new Intent(this, FbLoginActivity.class);
            startActivityForResult(facebookIntent, REQUEST_ID_FOR_FACEBOOK_LOGIN);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ID_FOR_NEW_AD) {
            /*if (resultCode == RESULT_OK) {
                //here
                int i=0;
            }*/
            getAdsJsonForKeyword(Urls.MAIN_SERVER_URL + Urls.GET_ALL_ADS_URL);
        }
        //back from Facebock login/logout page
        if (requestCode == REQUEST_ID_FOR_FACEBOOK_LOGIN) {
            facebookId = data.getStringExtra(Constants.FACEBOOK_ID);
            fbProfilePicUrl = data.getStringExtra(Constants.FACEBOOK_PROFILE_PIC_URL);
            String fbToken = data.getStringExtra(Constants.FACEBOOK_ACCESS_TOKEN);
            Log.d("CONAN: ", "Return from Facebook login, userid: " + facebookId);
            Log.d("CONAN: ", "Return from Facebook login, token: " + fbToken);

            //load new Options Menu cause of user is logged in now
            invalidateOptionsMenu();

            //set Profile pic with URL:

        }

        if (requestCode == REQUEST_ID_FOR_OPEN_AD) {

            getAdsJsonForKeyword(Urls.MAIN_SERVER_URL + Urls.GET_ALL_ADS_URL);
        }
    }
}