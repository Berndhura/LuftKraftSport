package wichura.de.camperapp.ad;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Display;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;

import wichura.de.camperapp.R;
import wichura.de.camperapp.http.Service;
import wichura.de.camperapp.http.Urls;
import wichura.de.camperapp.http.VolleyService;
import wichura.de.camperapp.mainactivity.Constants;
import wichura.de.camperapp.login.LoginActivity;
import wichura.de.camperapp.models.Bookmarks;

import static wichura.de.camperapp.mainactivity.Constants.SHARED_PREFS_USER_INFO;

public class OpenAdActivity extends AppCompatActivity {

    // private static double longitute;
    //private static double latitude;
    private Button mBookmarkButton;
    private String mAdId;

    private int displayHeight;
    private int displayWidth;
    private boolean isBookmarked;

    private ProgressBar mOpenAdProgressBar;
    private VolleyService volleyService;

    private PresenterLayerOpenAd presenter;

    public OpenAdActivity() {
        volleyService = new VolleyService(OpenAdActivity.this);
    }


    @Override
    protected void onCreate(final Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.open_ad_activity);

        presenter = new PresenterLayerOpenAd(this, new Service(), getApplicationContext());

        Toolbar toolbar = (Toolbar) findViewById(R.id.open_ad_toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            toolbar.setNavigationOnClickListener((view) -> finish());
        }

        mOpenAdProgressBar = (ProgressBar) findViewById(R.id.open_Ad_ProgressBar);

        getDisplayDimensions();

        TextView mTitleText = (TextView) findViewById(R.id.title);
        TextView mPrice = (TextView) findViewById(R.id.price);
        TextView mDescText = (TextView) findViewById(R.id.description);
        TextView mDateText = (TextView) findViewById(R.id.ad_date);
        ImageView imgView = (ImageView) findViewById(R.id.imageView);
        Button mDelAndMsgButton = (Button) findViewById(R.id.delButton);

        //get data from Intent
        String pictureUri = getIntent().getStringExtra(Constants.URI);
        mTitleText.setText(getIntent().getStringExtra(Constants.TITLE));
        mPrice.setText(getIntent().getStringExtra(Constants.PRICE));
        mDescText.setText(getIntent().getStringExtra(Constants.DESCRIPTION));
        mDateText.setText(DateFormat.getDateInstance().format(getIntent().getLongExtra(Constants.DATE, 0)));
        mAdId = getIntent().getStringExtra(Constants.AD_ID);

        mBookmarkButton = (Button) findViewById(R.id.bookmarkButton);
        mBookmarkButton.setClickable(false);
        //loadBookmarks for user
        presenter.loadBookmarksForUser();

        sendRequestForViewCount(mAdId);

        int ratio = Math.round((float) displayWidth / (float) displayWidth);

        Picasso.with(getApplicationContext())
                .load(pictureUri)
                .placeholder(R.drawable.empty_photo)
                .resize((int) Math.round((float) displayWidth * 0.6), (int) Math.round((float) displayHeight * 0.6) * ratio)
                .centerCrop()
                .into(imgView, new Callback() {
                    @Override
                    public void onSuccess() {
                        mOpenAdProgressBar.setVisibility(ProgressBar.GONE);
                    }

                    @Override
                    public void onError() {
                        mOpenAdProgressBar.setVisibility(ProgressBar.GONE);
                        Toast.makeText(getApplicationContext(), "No network connection!", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });

        if (isOwnAd() && mDelAndMsgButton != null) {
            mDelAndMsgButton.setOnClickListener((view) -> {
                //get ad id and send delete request
                String adId = getIntent().getStringExtra(Constants.AD_ID);
                Log.i("CONAN", "AdId: " + mAdId);
                deleteAdRequest(adId);
            });
        } else {
            mDelAndMsgButton.setText("Send message");
            mDelAndMsgButton.setOnClickListener((view) -> {

                if (!getUserId().equals("")) {
                    //send a message to ad owner
                    String adId = getIntent().getStringExtra(Constants.AD_ID);
                    String ownerId = getIntent().getStringExtra(Constants.USER_ID_FROM_AD);
                    String sender = getUserId();
                    sendMessage(adId, ownerId, sender);
                } else {
                    final Intent facebookIntent = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivityForResult(facebookIntent, Constants.REQUEST_ID_FOR_FACEBOOK_LOGIN);
                }
            });
        }

        mBookmarkButton.setOnClickListener((view) -> {

            String adId = getIntent().getStringExtra(Constants.AD_ID);
            String userId = getIntent().getStringExtra(Constants.USER_ID);
            if (isBookmarked) {
                delBookmark(adId, userId);
            } else {
                bookmarkAd(adId, userId);
            }
        });

        Log.d("CONAN", "request Picture: " + pictureUri);

        //map fragment in app : https://developers.google.com/maps/documentation/android-api/start#die_xml-layoutdatei
        //TODO: show location on map fragment
        //TODO: get LatLng in JSON from  http://maps.google.com/maps/api/geocode/json?address=%22greifswald%22&sensor=false
        //getLocationInfo()
        //now get Lat and Lng  from  getLatLong()
        // this:  http://stackoverflow.com/questions/3574644/how-can-i-find-the-latitude-and-longitude-from-address

    }

    public void updateBookmarkButton(Bookmarks bm) {
        isBookmarked = false;
        if (bm.getBookmarks().contains(mAdId)) {
            mBookmarkButton.setText("Remove bookmark!");
            mBookmarkButton.setClickable(true);
            isBookmarked = true;
        } else {
            mBookmarkButton.setText("Bookmark");
            mBookmarkButton.setClickable(true);
        }
    }

    private void sendMessage(final String adId, final String ownerId, final String sender) {
        //send a message
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        final EditText edittext = new EditText(OpenAdActivity.this);
        alert.setTitle("Send a message");
        alert.setView(edittext);
        alert.setPositiveButton("Send", (dicalog, whichButton) -> {
            String message = edittext.getText().toString();
            sendMessageRequest(message, adId, ownerId, sender);
        });
        alert.setNegativeButton("not yet", (dismissDialog, whichButton) -> {
            //just go awa}
        });
        alert.show();
    }

    private void sendMessageRequest(String message, String adId, String ownerId, String sender) {
        String url = Urls.MAIN_SERVER_URL + Urls.SEND_MESSAGE +
                "?message=" + message.replaceAll(" ", "%20")
                + "&adId=" + adId
                + "&idFrom=" + sender + "&idTo=" + ownerId;

        Response.Listener<String> listener = (response) -> {
            //save message done
            if (response.equals("ok"))
                Toast.makeText(getApplicationContext(), "Message sent...", Toast.LENGTH_LONG).show();
        };

        Response.ErrorListener errorListener = (error) -> {
            //
            Log.d("CONAN", error.networkResponse.toString());
        };

        volleyService.sendStringGetRequest(url, listener, errorListener);
    }

    private void sendRequestForViewCount(String mAdId) {
        String url = Urls.MAIN_SERVER_URL + Urls.COUNT_VIEW + "?adId=" + mAdId;

        Response.Listener<String> listener = (response) -> {
            //increase view count
        };

        Response.ErrorListener errorListener = (error) -> {
            //error
        };

        volleyService.sendStringGetRequest(url, listener, errorListener);
    }

    private void bookmarkAd(String adId, String userId) {
        String url = Urls.MAIN_SERVER_URL + Urls.BOOKMARK_AD + "?adId=" + adId + "&userId=" + userId;

        Response.Listener<String> listener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Toast.makeText(getApplicationContext(), "Ad is bookmarked!", Toast.LENGTH_SHORT).show();
                mBookmarkButton.setText("Remove Bookmark");
                isBookmarked = true;
            }
        };

        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //Toast.makeText(OpenAdActivity.this, "Something went wrong...", Toast.LENGTH_LONG).show();
            }
        };

        volleyService.sendStringGetRequest(url, listener, errorListener);
    }

    private void delBookmark(String adId, String userId) {
        String url = Urls.MAIN_SERVER_URL + Urls.BOOKMARK_DELETE + "?adId=" + adId + "&userId=" + userId;

        Response.Listener<String> listener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Toast.makeText(getApplicationContext(), "Bookmark deleted!", Toast.LENGTH_SHORT).show();
                mBookmarkButton.setText("Bookmark");
                isBookmarked = false;
            }
        };

        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(OpenAdActivity.this, "Something went wrong...", Toast.LENGTH_LONG).show();
            }
        };

        volleyService.sendStringGetRequest(url, listener, errorListener);
    }

    private void deleteAdRequest(final String adId) {
        final String url = Urls.MAIN_SERVER_URL + Urls.DELETE_AD_WITH_APID + "?adid=" + adId;

        final Response.Listener<String> listener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                finish();
            }
        };

        final Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(OpenAdActivity.this, "Something went wrong...", Toast.LENGTH_LONG).show();
            }
        };

        AlertDialog myQuittingDialogBox = new AlertDialog.Builder(this)
                .setTitle("Delete Ad")
                .setMessage("Do you want to delete this ad?")
                .setIcon(R.drawable.delete)
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        volleyService.sendStringGetRequest(url, listener, errorListener);
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

    private String getUserId() {
        return getSharedPreferences(SHARED_PREFS_USER_INFO, 0).getString(Constants.USER_ID, "");
    }

    private boolean isOwnAd() {
        return getIntent().getStringExtra(Constants.USER_ID_FROM_AD).equals(getIntent().getStringExtra(Constants.USER_ID));
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


     /* public static boolean getLatLong(JSONObject jsonObject) {
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
    }*/


   /* public static JSONObject getLocationInfo(String address) {
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

            e.printStackTrace();
        }

        return jsonObject;
    }*/
}
