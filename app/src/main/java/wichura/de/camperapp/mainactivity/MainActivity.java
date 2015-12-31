package wichura.de.camperapp.mainactivity;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONException;

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

    public static final int REQUEST_ID_FOR_NEW_AD= 1;
    public static final int REQUEST_ID_FOR_FACEBOOK_LOGIN= 2;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        getAdsJsonForKeyword(Urls.MAIN_SERVER_URL + Urls.GET_ALL_ADS_URL);
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
                Toast.makeText(getApplicationContext(),"Missing network connection!\n"+error.toString(), Toast.LENGTH_LONG).show();
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

        int searchPlateId = searchView.getContext().getResources().getIdentifier("android:id/search_src_text", null, null);
        EditText searchPlate = (EditText) searchView.findViewById(searchPlateId);
        searchPlate.setTextColor(getResources().getColor(R.color.com_facebook_blue));
        searchPlate.setBackgroundResource(R.drawable.ic_action_settings);
        searchPlate.setImeOptions(EditorInfo.IME_ACTION_SEARCH);

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
                    getAdsJsonForKeyword(Urls.MAIN_SERVER_URL + Urls.GET_ADS_FOR_KEYWORD_URL + query);
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
            startActivityForResult(intent, REQUEST_ID_FOR_NEW_AD);
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
            getAdsJsonForKeyword(Urls.MAIN_SERVER_URL+Urls.GET_ALL_ADS_URL);
        }
        if (requestCode == REQUEST_ID_FOR_FACEBOOK_LOGIN){
            int i=2;
            Log.d("Wo: ", "maul");
        }
    }
}