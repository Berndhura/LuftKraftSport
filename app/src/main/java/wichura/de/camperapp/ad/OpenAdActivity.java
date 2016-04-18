package wichura.de.camperapp.ad;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;

import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.client.ClientProtocolException;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import wichura.de.camperapp.R;
import wichura.de.camperapp.http.Urls;
import wichura.de.camperapp.mainactivity.Constants;

public class OpenAdActivity extends Activity {

    private static double longitute;
    private static double latitude;
    private String pictureUri;
    private TextView mTitleText;
    private TextView mPrice;
    private TextView mDescText;
    private TextView mDateText;
    private Button mDelButton;
    private Button mBookmarkButton;
    private String mAdId;

    private ImageView imgView;

    private int displayHeight;
    private int displayWidth;
    private RequestQueue requestQueue;
    private boolean isBookmarked;
    private boolean isMyAd;


    @Override
    protected void onCreate(final Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.open_ad_activity);
        getDisplayDimensions();

        //Volley request queue for delete, bookmark...
        requestQueue = Volley.newRequestQueue(getApplicationContext());

        mTitleText = (TextView) findViewById(R.id.title);
        mPrice = (TextView) findViewById(R.id.price);
        mDescText = (TextView) findViewById(R.id.description);
        mDateText = (TextView) findViewById(R.id.ad_date);
        imgView = (ImageView) findViewById(R.id.imageView);
        mDelButton = (Button) findViewById(R.id.delButton);
        mBookmarkButton = (Button) findViewById(R.id.bookmarkButton);

        //TODO: check DB if bookmarked!
        isBookmarked = false;
        if (isBookmarked) {
            mBookmarkButton.setText("Remove bookmark!");
        } else {
            mBookmarkButton.setText("Bookmark");
        }
        //TODO check if user owns this ad
        isMyAd = true;


        //get data from Intent
        pictureUri = getIntent().getStringExtra(Constants.URI);
        mTitleText.setText(getIntent().getStringExtra(Constants.TITLE));
        mPrice.setText(getIntent().getStringExtra(Constants.PRICE));
        mDescText.setText(getIntent().getStringExtra(Constants.TITLE));
        mDateText.setText(DateFormat.getDateInstance().format(getIntent().getLongExtra(Constants.DATE,0)));
        mAdId = getIntent().getStringExtra("id");

        int ratio = Math.round((float) displayWidth / (float) displayWidth);


        Picasso.with(getApplicationContext())
                .load(pictureUri)
                .resize((int) Math.round((float) displayWidth * 0.6), (int) Math.round((float) displayHeight * 0.6) * ratio)
                .centerCrop()
                .into(imgView);

        mDelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //get ad id and send delete request
                String adId = getIntent().getStringExtra(Constants.ID);
                Log.i("CONAN", "ApId: " + mAdId);
                deleteAdRequest(adId);
            }
        });

        mBookmarkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO : user Constants
                String adId = getIntent().getStringExtra(Constants.ID);
                String userId = getIntent().getStringExtra(Constants.USER_ID);
                if (isBookmarked) {
                    delBookmark(adId, userId);
                } else {
                    bookmarkAd(adId, userId);
                }
            }
        });

        Log.d("CONAN", "MyClass.getView() OPEN " + pictureUri);

        //map fragment in app : https://developers.google.com/maps/documentation/android-api/start#die_xml-layoutdatei
        //TODO: show location on map fragment
        //TODO: get LatLng in JSON from  http://maps.google.com/maps/api/geocode/json?address=%22greifswald%22&sensor=false
        //getLocationInfo()
        //now get Lat and Lng  from  getLatLong()
        // this:  http://stackoverflow.com/questions/3574644/how-can-i-find-the-latitude-and-longitude-from-address

    }

    private void bookmarkAd(String adId, String userId) {

        //TODO: check parameter with capital I is in user
        String url = Urls.MAIN_SERVER_URL + Urls.BOOKMARK_AD + "?adId=" + adId + "&userId=" + userId;
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Toast.makeText(getApplicationContext(), "Ad is bookmarked!", Toast.LENGTH_SHORT).show();
                        mBookmarkButton.setText("Remove Bookmark");
                        isBookmarked = true;
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(OpenAdActivity.this, "Something went wrong...", Toast.LENGTH_LONG).show();
            }
        });
        requestQueue.add(stringRequest);

    }

    private void delBookmark(String adId, String userId) {

        //TODO: check parameter with capital I is in user
        String url = Urls.MAIN_SERVER_URL + Urls.BOOKMARK_DELETE + "?adId=" + adId + "&userId=" + userId;
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Toast.makeText(getApplicationContext(), "Bookmark deleted!", Toast.LENGTH_SHORT).show();
                        mBookmarkButton.setText("Bookmark");
                        isBookmarked = false;
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(OpenAdActivity.this, "Something went wrong...", Toast.LENGTH_LONG).show();
            }
        });
        requestQueue.add(stringRequest);

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

    private void deleteAdRequest(final String adId) {
        AlertDialog myQuittingDialogBox = new AlertDialog.Builder(this)
                .setTitle("Delete Ad")
                .setMessage("Do you want to delete this ad?")
                .setIcon(R.drawable.delete)
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        String url = Urls.MAIN_SERVER_URL + Urls.DELETE_AD_WITH_APID + "?adid=" + adId;
                        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                                new Response.Listener<String>() {
                                    @Override
                                    public void onResponse(String response) {
                                        finish();
                                    }
                                }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Toast.makeText(OpenAdActivity.this, "Something went wrong...", Toast.LENGTH_LONG).show();
                            }
                        });
                        requestQueue.add(stringRequest);
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();
        myQuittingDialogBox.show();
    }

    public static JSONObject getLocationInfo(String address) {
        StringBuilder stringBuilder = new StringBuilder();
        try {

            address = address.replaceAll(" ", "%20");

            HttpPost httppost = new HttpPost("http://maps.google.com/maps/api/geocode/json?address=" + address + "&sensor=false");
            HttpClient client = new DefaultHttpClient();
            HttpResponse response;
            stringBuilder = new StringBuilder();


            response = client.execute(httppost);
            HttpEntity entity = response.getEntity();
            InputStream stream = entity.getContent();
            int b;
            while ((b = stream.read()) != -1) {
                stringBuilder.append((char) b);
            }
        } catch (ClientProtocolException e) {
        } catch (IOException e) {
        }

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject = new JSONObject(stringBuilder.toString());
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return jsonObject;
    }

    public static boolean getLatLong(JSONObject jsonObject) {

        try {

            longitute = ((JSONArray) jsonObject.get("results")).getJSONObject(0)
                    .getJSONObject("geometry").getJSONObject("location")
                    .getDouble("lng");

            latitude = ((JSONArray) jsonObject.get("results")).getJSONObject(0)
                    .getJSONObject("geometry").getJSONObject("location")
                    .getDouble("lat");

        } catch (JSONException e) {
            return false;

        }

        return true;
    }
}
