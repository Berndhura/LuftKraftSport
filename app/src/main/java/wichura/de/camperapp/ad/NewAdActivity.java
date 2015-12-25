package wichura.de.camperapp.ad;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

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

    private final String twoHyphens = "--";
    private final String lineEnd = "\r\n";
    private final String boundary = "SwA";



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

        multiPost(picture);
    }

    public static String getRealPathFromUri(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
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
    public void multiPost(String bitmap) {
        HashMap<String, String> params = new HashMap<String, String>();

        Uri fileUri = Uri.parse(bitmap.toString());
        String fileString = getRealPathFromUri(getApplicationContext(),fileUri);

        params.put("title", "farti");
        params.put("description", "Some Param");
        params.put("keywords", "Some Param");

        File file = new File(fileString.toString());
        if (file.exists()) {
            Log.i("MyActivity", "FILE DAAAAAAAAAAAAAAAAAAAAAA:   "+ bitmap.toString());
        }
        else {
            Log.i("MyActivity", "FILE FEHLLLLLLLLLLLLLLLLLER:   "+ bitmap.toString());
        }
        //content://media/external/images/media/19





        MultipartRequest mr = new MultipartRequest(Urls.MAIN_SERVER_URL+"saveNewAd",
                new Response.Listener<String>(){

                    @Override
                    public void onResponse(String response) {
                        Log.d("response", response);
                    }

                },
                new Response.ErrorListener(){

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Volley Request Error", error.getLocalizedMessage());
            }

        },file, params);

        Volley.newRequestQueue(this).add(mr);

       //volleyQueue.add(multipartRequest);

    }
}



