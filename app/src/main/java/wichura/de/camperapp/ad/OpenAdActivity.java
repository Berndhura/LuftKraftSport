package wichura.de.camperapp.ad;

import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import wichura.de.camperapp.R;
import wichura.de.camperapp.app.AppController;
import wichura.de.camperapp.http.Urls;

public class OpenAdActivity extends Activity {

    private String pictureUri;
    private TextView mTitleText;
    private TextView mDescText;
    private TextView mLocationText;
    private TextView mPhoneText;
    private Button mDelButton;
    private String mAdId;

    private NetworkImageView imgView;

    //TODO
    //use swipe function from CustomSwipeAdapter to swipe pictures
    private CustomSwipeAdapter adapter;
    private NetworkImageView viewPager;

    ImageLoader imageLoader = AppController.getInstance().getImageLoader();

    @Override
    protected void onCreate(final Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.open_ad_activity);

        //TODO
        //swipe the pictures, preparation
        adapter = new CustomSwipeAdapter(this);
        //error! this has to be a viewpager obj in layout xml file! TODO!
        viewPager = (NetworkImageView) findViewById(R.id.icon);
        //viewPager.setAdapter(adapter);


        TextView titelHeader = (TextView) findViewById(R.id.headerTitel);
        mTitleText = (TextView) findViewById(R.id.title);
        TextView desHeader = (TextView) findViewById(R.id.headerDesciption);
        mDescText = (TextView) findViewById(R.id.description);
        mLocationText = (TextView) findViewById(R.id.location);
        mPhoneText = (TextView) findViewById(R.id.phone);
        imgView = (NetworkImageView) findViewById(R.id.icon);
        mDelButton = (Button) findViewById(R.id.delButton);

        // Fetch screen height and width, to use as our max size when loading images as this
        // activity runs full screen
        final DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        final int height = displayMetrics.heightPixels;
        final int width = displayMetrics.widthPixels;

        //get data from Intent
        pictureUri = getIntent().getStringExtra("uri");

        mTitleText.setText(getIntent().getStringExtra("title"));
        mDescText.setText(getIntent().getStringExtra("description"));
        mLocationText.setText(getIntent().getStringExtra("location"));
        mPhoneText.setText(getIntent().getStringExtra("phone"));
        mAdId = getIntent().getStringExtra("id");


        NetworkImageView picture = (NetworkImageView) imgView
                .findViewById(R.id.icon);
        // picture.setMinimumWidth(width);
        picture.setImageUrl(pictureUri, imageLoader);

        mDelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //get ad id and send delete request
                String adId = getIntent().getStringExtra("id");
                Log.i("CONAN", "ApId: " + mAdId);
                deleteAdRequest(adId);
            }
        });

        Log.i("MyActivity", "MyClass.getView() OPEN " + pictureUri);
    }

    private void deleteAdRequest(String adId) {
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = Urls.MAIN_SERVER_URL + Urls.DELET_AD_WITH_APID + "?adid=" + adId;

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

        queue.add(stringRequest);
    }


}
