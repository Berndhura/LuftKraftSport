package wichura.de.camperapp.ad;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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


        final Button submitButton = (Button) findViewById(R.id.uploadButton);
        submitButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {

                final String titleString = mKeywords.getText().toString();
                final String descString = mDescText.getText().toString();
                final String price = mPrice.getText().toString();
                final String keyWordsString = "zelt";

                // Package ToDoItem data into an Intent
                final Intent data = new Intent();
                AdItem.packageIntent(
                        data,
                        titleString,
                        "apid",
                        descString,
                        keyWordsString,
                        mImage,
                        "TODO",
                        "PHONE",
                        price);

                setResult(RESULT_OK, data);

                sendHttpToServer(data);

                finish();
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

        progress = new ProgressDialog(this);
        progress.setTitle("Please Wait!!");
        progress.setMessage("Wait!!");
        progress.setCancelable(false);
        progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progress.show();

        HashMap<String, String> params = new HashMap<String, String>();

        Uri fileUri = Uri.parse(picture.toString());
        Log.i("CONAN", "fileURI: " + fileUri);

        //thumbnail?
        // Bitmap ThumbImage = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(imagePath), THUMBSIZE, THUMBSIZE);

        String fileString = getRealPathFromUri(getApplicationContext(), fileUri); //war bildCompresse
        Log.i("CONAN", "fileString: " + fileString);

        params.put("title", title);
        params.put("description", description);
        params.put("keywords", keywords);
        params.put("userid", "conan");
        // params.put("price", price);

        File file = new File(fileString.toString());
        Log.i("CONAN", "file: " + file);
        File reducedPicture = saveBitmapToFile(file);
        Log.i("CONAN", "newFile: " + reducedPicture);

        MultipartRequest multipartRequest = new MultipartRequest(Urls.MAIN_SERVER_URL + Urls.UPLOAD_NEW_AD_URL,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        Log.d("response", response);
                        Toast.makeText(getApplicationContext(), "Upload...done!", Toast.LENGTH_SHORT).show();
                    }

                },
                new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Volley Request Error", error.getLocalizedMessage());
                        Toast.makeText(getApplicationContext(), "Upload did not work!\n" + error.toString(), Toast.LENGTH_LONG).show();
                    }

                }, reducedPicture, params);

        Volley.newRequestQueue(this).add(multipartRequest);
    }



    public File saveBitmapToFile(File file){
        try {

            // BitmapFactory options to downsize the image
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            o.inSampleSize = 6;
            // factor of downsizing the image

            FileInputStream inputStream = new FileInputStream(file);
            //Bitmap selectedBitmap = null;
            BitmapFactory.decodeStream(inputStream, null, o);
            inputStream.close();

            // The new size we want to scale to
            final int REQUIRED_SIZE=75;

            // Find the correct scale value. It should be the power of 2.
            int scale = 1;
            while(o.outWidth / scale / 2 >= REQUIRED_SIZE &&
                    o.outHeight / scale / 2 >= REQUIRED_SIZE) {
                scale *= 2;
            }

            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            inputStream = new FileInputStream(file);

            Bitmap selectedBitmap = BitmapFactory.decodeStream(inputStream, null, o2);
            inputStream.close();

            //TODO save a local copy, upload and onSuccess delete the local copy!!!!
            // here i override the original image file
            file.createNewFile();
            FileOutputStream outputStream = new FileOutputStream(file);

            selectedBitmap.compress(Bitmap.CompressFormat.JPEG, 100 , outputStream);

            return file;
        } catch (Exception e) {
            return null;
        }
    }
}



