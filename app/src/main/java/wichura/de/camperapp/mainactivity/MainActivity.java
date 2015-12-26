package wichura.de.camperapp.mainactivity;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.text.InputType;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.facebook.FacebookSdk;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONException;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import wichura.de.camperapp.R;
import wichura.de.camperapp.ad.NewAdActivity;
import wichura.de.camperapp.ad.OpenAdActivity;
import wichura.de.camperapp.http.Urls;

//farbcode bilder: #639bc5
public class MainActivity extends ActionBarActivity  {

    private ListView listView;
    private List<RowItem> rowItems;
    private CustomListViewAdapter adapter;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FacebookSdk.sdkInitialize(getApplicationContext());
        //appid=535532649933816

        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    "wichura.de.camperapp",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {

        } catch (NoSuchAlgorithmException e) {

        }
        setContentView(R.layout.activity_main);
        getAdsJsonForKeyword(Urls.MAIN_SERVER_URL+Urls.GET_ALL_ADS_URL);
    }

    private void getAdsJsonForKeyword(String url) {
        RequestQueue queue = Volley.newRequestQueue(this);

        JsonArrayRequest getAllAdsInJson = new JsonArrayRequest(
                Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                Context context=getApplicationContext();
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
                        intent.putExtra("uri",rowItem.getUrl());
                        intent.putExtra("title",rowItem.getTitle());
                        intent.putExtra("description",rowItem.getDescription());
                        intent.putExtra("location",rowItem.getLocation());
                        intent.putExtra("phone",rowItem.getPhone());
                        startActivity(intent);

                        Toast.makeText(getApplicationContext(), rowItem.getTitle(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // TODO Auto-generated method stub
            }
        });
        queue.add(getAllAdsInJson);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
       // Inflate the menu; this adds items to the action bar if it is present.
       getMenuInflater().inflate(R.menu.main, menu);

       SearchManager sM =(SearchManager) getSystemService(Context.SEARCH_SERVICE);

       final MenuItem searchMenuItem = menu.findItem(R.id.menu_search);
       final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchMenuItem);

        searchView.setSearchableInfo(sM.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(true);
        searchView.setSubmitButtonEnabled(true);


        if(searchView != null)
        {
            searchView.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    Log.d("query: ", query);
                    getAdsJsonForKeyword(Urls.MAIN_SERVER_URL+Urls.GET_ADS_FOR_KEYWORD_URL+query);
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    return false;
                }
            });
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
            startActivityForResult(intent, 1);
            return true;
        }

        if (id == R.id.login) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}