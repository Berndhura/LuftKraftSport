package wichura.de.camperapp.ad;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.FileAsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.io.File;
import java.io.FileNotFoundException;

import cz.msebera.android.httpclient.Header;
import wichura.de.camperapp.bitmap.BitmapHelper;
import wichura.de.camperapp.http.Urls;
import wichura.de.camperapp.mainactivity.Constants;

/**
 * Created by ich on 25.10.2016.
 * Camper App
 */

public class FileUploadService {

    private Context context;
    private NewAdActivity view;

    FileUploadService(Context context, NewAdActivity view) {
        this.context = context;
        this.view = view;

    }

    public void multiPost(Intent data) {

        view.showProgress();

        String title = data.getStringExtra(Constants.TITLE);
        String description = data.getStringExtra(Constants.DESCRIPTION);
        String keywords = data.getStringExtra(Constants.KEYWORDS);
        String picture = data.getStringExtra(Constants.FILENAME);
        String price = data.getStringExtra(Constants.PRICE);
        long date = data.getLongExtra(Constants.DATE, 0);

        Uri fileUri = Uri.parse(picture.toString());
        Log.i("CONAN", "fileURI: " + fileUri);

        //thumbnail?
        // Bitmap ThumbImage = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(imagePath), THUMBSIZE, THUMBSIZE);

        String fileString = getRealPathFromUri(context, fileUri);
        Log.i("CONAN", "fileString: " + fileString);


        File file = new File(fileString.toString());
        Log.i("CONAN", "file: " + file);
        BitmapHelper bitmapHelper = new BitmapHelper(context);
        final File reducedPicture = bitmapHelper.saveBitmapToFile(file);
        Log.i("CONAN", "newFile: " + reducedPicture);


        //todo: add more pics here??
        RequestParams params = new RequestParams();
        try {
            params.put(Constants.IMAGE, reducedPicture);
        } catch (FileNotFoundException e) {
        }

        params.put(Constants.TITLE, title);
        params.put(Constants.DESCRIPTION, description);
        params.put(Constants.KEYWORDS, keywords);
        params.put("userid", getUserId());  //TODO: userid vs user_id in server!! server anpassen? -> jo eine stelle dort in saveNewAd
        params.put(Constants.PRICE, price);
        params.put(Constants.DATE, date);
        Log.d("CONAN", "userid bei upload: " + getUserId());

        AsyncHttpClient client = new AsyncHttpClient();
        client.post(Urls.MAIN_SERVER_URL + Urls.UPLOAD_NEW_AD_URL, params, new FileAsyncHttpResponseHandler(reducedPicture) {
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, File file) {
                Log.e("Volley Request Error", "no");
                Toast.makeText(context, "Upload did not work!\n", Toast.LENGTH_LONG).show();
                view.hideProgress();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, File file) {
                Log.d("CONAN", "Picture uploaded");
                Toast.makeText(context, "Upload...done!", Toast.LENGTH_SHORT).show();
                //jetzt bilder neu laden!! finish() geht zu früh zurück in mainactivity
                Boolean deletetd = reducedPicture.delete();
                if (!deletetd)
                    Toast.makeText(context, "Delete tempFile not possible", Toast.LENGTH_SHORT).show();
                view.hideProgress();
                view.finish();
            }
        });
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

    private String getUserId() {
        SharedPreferences settings = context.getSharedPreferences("UserInfo", 0);
        return settings.getString(Constants.USER_ID, "");
    }
}
