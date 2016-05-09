package wichura.de.camperapp.ad;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.FileAsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Calendar;
import java.util.Date;

import cz.msebera.android.httpclient.Header;
import wichura.de.camperapp.R;
import wichura.de.camperapp.bitmap.BitmapHelper;
import wichura.de.camperapp.http.Urls;
import wichura.de.camperapp.mainactivity.Constants;


public class NewAdActivity extends Activity {

    private EditText mTitleText;
    private EditText mDescText;
    private EditText mKeywords;
    private EditText mPrice;

    private static final int SELECT_PHOTO = 100;
    private String mImage;
    private int pictureCount = 1;

    private ImageView mImgOne;
    private ImageView mImgTwo;
    private String title;
    private String description;
    private String keywords;
    private String price;
    private String picture;
    private String userId;
    private long date;

    private ProgressDialog progress;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        //https://www.youtube.com/watch?v=4MFzuP1F-xQ

        setContentView(R.layout.new_ad_acivity);

        // mTitleText = (EditText) findViewById(R.id.title);
        mDescText = (EditText) findViewById(R.id.description);
        mKeywords = (EditText) findViewById(R.id.keywords);
        mImgOne = (ImageView) findViewById(R.id.imageButton);
        mPrice = (EditText) findViewById(R.id.preis);

        SharedPreferences settings = getSharedPreferences("UserInfo", 0);
        final String id = settings.getString(Constants.USER_ID, "");
        userId = id;


        final Button submitButton = (Button) findViewById(R.id.uploadButton);
        submitButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {

                final String titleString = mKeywords.getText().toString();
                final String descString = mDescText.getText().toString();
                final String price = mPrice.getText().toString();
                final String keyWordsString = "zelt";
                final long date = System.currentTimeMillis();

                // Package ToDoItem data into an Intent
                final Intent data = new Intent();
                AdItem.packageIntent(data, titleString, "apid", descString, keyWordsString, mImage,
                        "TODO", "PHONE", price, date);

                sendHttpToServer(data);
                setResult(RESULT_OK, data);
                //old finnish TODO
            }
        });

        final ImageButton getPictureButton = (ImageButton) findViewById(R.id.imageButton);

        getPictureButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {

                final Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, SELECT_PHOTO);
            }
        });
    }


    @Override
    protected void onActivityResult(final int requestCode,
                                    final int resultCode, final Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        switch (requestCode) {
            case SELECT_PHOTO:
                if (resultCode == RESULT_OK && pictureCount < 4) {
                    final Uri selectedImage = imageReturnedIntent.getData();
                    //todo :works for one pic, need to work for more: array or comma separeted?
                    mImage = selectedImage.toString();

                    switch (pictureCount) {
                        case 1: {
                            mImgOne = (ImageView) findViewById(R.id.imageButton);
                            mImgOne.setImageURI(selectedImage);
                            pictureCount++;
                            break;
                        }
                        case 2: {
                            mImgTwo = (ImageView) findViewById(R.id.imageButton);
                            mImgTwo.setImageURI(selectedImage);
                            pictureCount++;
                            break;
                        }
                    }
                }
        }
    }

    private void sendHttpToServer(final Intent data) {

        title = data.getStringExtra(AdItem.TITLE);
        description = data.getStringExtra(AdItem.DESC);
        keywords = data.getStringExtra(AdItem.KEYWORDS);
        picture = data.getStringExtra(AdItem.FILENAME);
        price = data.getStringExtra(AdItem.PRICE);
        date = data.getLongExtra(AdItem.DATE, 0);

        Log.d("query", title + description + keywords + picture);

        multiPost();
    }

    public static String getRealPathFromUri(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }


    //http://stackoverflow.com/questions/18288864/how-to-multipart-data-using-android-volley
    public void multiPost() {

        progress = new MyProgressDialog(this);
        progress.setCancelable(false);
        progress.show();

        //findViewById(R.id.avloadingIndicatorView).setVisibility(View.VISIBLE);


        Uri fileUri = Uri.parse(picture.toString());
        Log.i("CONAN", "fileURI: " + fileUri);

        //thumbnail?
        // Bitmap ThumbImage = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(imagePath), THUMBSIZE, THUMBSIZE);

        String fileString = getRealPathFromUri(getApplicationContext(), fileUri); //war bildCompresse
        Log.i("CONAN", "fileString: " + fileString);


        File file = new File(fileString.toString());
        Log.i("CONAN", "file: " + file);
        BitmapHelper bitmapHelper = new BitmapHelper(getApplicationContext());
        final File reducedPicture = bitmapHelper.saveBitmapToFile(file);
        Log.i("CONAN", "newFile: " + reducedPicture);


        //todo: add more pics here??
        RequestParams params = new RequestParams();
        try {
            params.put("image", reducedPicture);
        } catch (FileNotFoundException e) {
        }


        //todo constants? sind url paramater!! sollten aber passen
        params.put("title", title);
        params.put("description", description);
        params.put("keywords", keywords);
        params.put("userid", userId);
        params.put("price", price);
        params.put("date", date);
        Log.d("CONAN", "userid bei upload: "+userId);

        AsyncHttpClient client = new AsyncHttpClient();
        client.post(Urls.MAIN_SERVER_URL + Urls.UPLOAD_NEW_AD_URL, params, new FileAsyncHttpResponseHandler(reducedPicture) {
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, File file) {
                Log.e("Volley Request Error", "no");
                Toast.makeText(getApplicationContext(), "Upload did not work!\n", Toast.LENGTH_LONG).show();
                progress.dismiss();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, File file) {
                Log.d("CONAN", "Picture uploaded");
                Toast.makeText(getApplicationContext(), "Upload...done!", Toast.LENGTH_SHORT).show();
                //jetzt bilder neu laden!! finish() geht zu früh zurück in mainactivity
                Boolean deletetd = reducedPicture.delete();
                if (!deletetd)
                    Toast.makeText(getApplicationContext(), "Delete tempFile not possible", Toast.LENGTH_SHORT).show();
                progress.dismiss();
                finish();
            }
        });
    }
}



