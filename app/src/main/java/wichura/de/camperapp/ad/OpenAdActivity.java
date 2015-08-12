package wichura.de.camperapp.ad;

import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;

import wichura.de.camperapp.R;
import wichura.de.camperapp.app.AppController;

public class OpenAdActivity extends Activity {

	private String pictureUri;
    private TextView mTitleText;
    private TextView mDescText;

    private NetworkImageView imgView;

    ImageLoader imageLoader = AppController.getInstance().getImageLoader();

	@Override
	protected void onCreate(final Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		setContentView(R.layout.open_ad_activity);

        TextView titelHeader = (TextView) findViewById(R.id.headerTitel);
        mTitleText = (TextView) findViewById(R.id.title);
        TextView desHeader = (TextView) findViewById(R.id.headerDesciption);
        mDescText = (TextView) findViewById(R.id.description);
        imgView = (NetworkImageView) findViewById(R.id.icon);

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



        NetworkImageView picture = (NetworkImageView) imgView
                .findViewById(R.id.icon);
       // picture.setMinimumWidth(width);
        picture.setImageUrl(pictureUri, imageLoader);





		Log.i("MyActivity", "MyClass.getView() OPEN " + pictureUri);
	}
}
