package wichura.de.camperapp.ad;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import wichura.de.camperapp.R;
import wichura.de.camperapp.bitmap.BitmapHelper;
import wichura.de.camperapp.http.MultipartRequest;
import wichura.de.camperapp.http.Urls;


public class NewAdActivity extends Activity {

    private EditText mTitleText;
    private EditText mDescText;
    private EditText mKeywords;

    private static final int SELECT_PHOTO = 100;
    private String mImage;
    private int pictureCount = 1;

    private ImageView mImgOne;
    private ImageView mImgTwo;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        //https://www.youtube.com/watch?v=4MFzuP1F-xQ

        setContentView(R.layout.new_ad_acivity);

        mTitleText = (EditText) findViewById(R.id.title);
        mDescText = (EditText) findViewById(R.id.description);
        mKeywords = (EditText) findViewById(R.id.keywords);
        mImgOne = (ImageView) findViewById(R.id.picturOne);
        mImgTwo = (ImageView) findViewById(R.id.picturTwo);


        final Button submitButton = (Button) findViewById(R.id.submitButton);
        submitButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {

                final String titleString = mTitleText.getText().toString();
                final String descString = mDescText.getText().toString();
                final String keyWordsString = mKeywords.getText().toString();

                // Package ToDoItem data into an Intent
                final Intent data = new Intent();
                AdItem.packageIntent(
                        data,
                        titleString,
                        descString,
                        keyWordsString,
                        mImage,
                        "TODO",
                        "PHONE"); //TODO : location

                // TODO - return data Intent and finish
                setResult(RESULT_OK, data);

                sendHttpToServer(data);

                finish();
            }
        });

        final Button getPictureButton = (Button) findViewById(R.id.uploadButton);

        getPictureButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {

                final Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, SELECT_PHOTO);

            }
        });

        final Button cancelButton = (Button) findViewById(R.id.cancelButton);

        cancelButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {

                // TODO - Set Activity's result with result code RESULT_OK
                // setResult(RESULT_OK, intent);
                // TODO - Finish the Activity
                finish();

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
                            mImgOne = (ImageView) findViewById(R.id.picturOne);
                            mImgOne.setImageURI(selectedImage);
                            pictureCount++;
                            break;
                        }
                        case 2: {
                            mImgTwo = (ImageView) findViewById(R.id.picturTwo);
                            mImgTwo.setImageURI(selectedImage);
                            pictureCount++;
                            break;
                        }
                    }
                }
        }
    }

    private void sendHttpToServer(final Intent data) {


        final String title = data.getStringExtra(AdItem.TITLE);
        final String description = data.getStringExtra(AdItem.DESC);
        final String keywords = data.getStringExtra(AdItem.KEYWORDS);
        final String picture = data.getStringExtra(AdItem.FILENAME);
        Log.d("query", title + description + keywords + picture);
        //alt
        //final HttpHelper httpHelper = new HttpHelper(data, url, this);
        //httpHelper.postData();
        //new

        final Uri uri = Uri.parse(picture);
        BitmapHelper bitmapHelper = new BitmapHelper(getApplicationContext());
        Bitmap thump = bitmapHelper.resize(uri.toString());

        Bitmap bitmap = null;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(
                    getApplicationContext().getContentResolver(), uri);
        } catch (final IOException e) {
            Log.i("MyActivity", "MyClass.getView() URLS " + "BITMAP FEHLER");
            e.printStackTrace();
        }

        multiPost(uri);
    }


    //http://stackoverflow.com/questions/18288864/how-to-multipart-data-using-android-volley
    public void multiPost(Uri bitmap) {

        RequestQueue volleyQueue = Volley.newRequestQueue(getApplicationContext());

        final Response.Listener<String> mListener = new Response.Listener<String>() {
             @Override
             public void onResponse(String response) {
                Log.i("volley Res from upload", response.toString());
             }
         };

        final Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        };

        final File mFilePart= new File (bitmap.getPath());
        final Map<String, String> mStringPart= new HashMap<>();
        mStringPart.put("image", "defender");

        MultipartRequest request = new MultipartRequest(
                Urls.MAIN_SERVER_URL+Urls.UPLOAD_NEW_AD_URL,
                errorListener,
                mListener,
                mFilePart,
                mStringPart);

        request.addStringBody("title", "title");
        request.addStringBody("description", "description");
        request.addStringBody("keywords", "keywords");

        volleyQueue.add(request);

    }
}



