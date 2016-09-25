package wichura.de.camperapp.mainactivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.NoSuchElementException;

import wichura.de.camperapp.R;
import wichura.de.camperapp.ad.NewAdActivity;


/**
 * Created by Bernd Wichura on 31.03.2016.
 * Camper App
 */
public class StartActivity extends Activity {

    private Button startButton;
    private Button showButton;
    private int displayWidth;
    private int displayHeight;

    private ImageView startImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.start_page_layout);

//        Toolbar toolbar = (Toolbar) findViewById(R.id.start_toolbar);
//        if (toolbar != null) {
//            setSupportActionBar(toolbar);
//        }

        startButton = initStartButton();
        showButton = initShowButton();




    }

    private Button initStartButton() {
        startButton = (Button) findViewById(R.id.firstStartButton);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (getUserId().equals("")) {
                    final Intent facebookIntent = new Intent(getApplicationContext(), FbLoginActivity.class);
                    startActivityForResult(facebookIntent, Constants.REQUEST_ID_FOR_FACEBOOK_LOGIN);
                } else {
                    final Intent intent = new Intent(getApplicationContext(), NewAdActivity.class);
                    intent.putExtra(Constants.USER_ID, getUserId());
                    startActivityForResult(intent, Constants.REQUEST_ID_FOR_NEW_AD);
                }
            }
        });
        return startButton;
    }

    private Button initShowButton() {
        showButton = (Button) findViewById(R.id.show_button);
        showButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                Intent intent = new Intent(StartActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
        return showButton;
    }

    private void getDisplayDimensions() {

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        displayWidth = size.x;
        displayHeight = size.y;
    }

    private void scaleImage(ImageView view) throws NoSuchElementException {
        // Get bitmap from the the ImageView.
        Bitmap bitmap = null;

        try {
            Drawable drawing = view.getDrawable();
            bitmap = ((BitmapDrawable) drawing).getBitmap();
        } catch (NullPointerException e) {
            throw new NoSuchElementException("No drawable on given view");
        } catch (ClassCastException e) {
            // Check bitmap is Ion drawable
            //bitmap = Ion.with(view).getBitmap();
        }

        // Get current dimensions AND the desired bounding box
        int width = 0;

        try {
            width = bitmap.getWidth();
        } catch (NullPointerException e) {
            throw new NoSuchElementException("Can't find bitmap on given view/drawable");
        }

        int height = bitmap.getHeight();
        int bounding = dpToPx(displayWidth);
        Log.i("Test", "original width = " + Integer.toString(width));
        Log.i("Test", "original height = " + Integer.toString(height));
        Log.i("Test", "bounding = " + Integer.toString(bounding));

        // Determine how much to scale: the dimension requiring less scaling is
        // closer to the its side. This way the image always stays inside your
        // bounding box AND either x/y axis touches it.
        float xScale = ((float) bounding) / width;
        float yScale = ((float) bounding) / height;
        float scale = (xScale <= yScale) ? xScale : yScale;
        Log.i("Test", "xScale = " + Float.toString(xScale));
        Log.i("Test", "yScale = " + Float.toString(yScale));
        Log.i("Test", "scale = " + Float.toString(scale));

        // Create a matrix for the scaling and add the scaling data
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);

        // Create a new bitmap and convert it to a format understood by the ImageView
        Bitmap scaledBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
        width = scaledBitmap.getWidth(); // re-use
        height = scaledBitmap.getHeight(); // re-use
        BitmapDrawable result = new BitmapDrawable(scaledBitmap);
        Log.i("Test", "scaled width = " + Integer.toString(width));
        Log.i("Test", "scaled height = " + Integer.toString(height));

        // Apply the scaled bitmap
        view.setImageDrawable(result);

        // Now change ImageView's dimensions to match the scaled image
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) view.getLayoutParams();
        params.width = width;
        params.height = height;
        view.setLayoutParams(params);

        Log.i("Test", "done");
    }

    private int dpToPx(int dp) {
        float density = getApplicationContext().getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }

    private String getUserId() {
        return getSharedPreferences("UserInfo", 0).getString(Constants.USER_ID, "");
    }
}
