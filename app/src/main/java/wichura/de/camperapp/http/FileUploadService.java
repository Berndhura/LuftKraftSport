package wichura.de.camperapp.http;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import wichura.de.camperapp.activity.NewAdActivity;
import wichura.de.camperapp.mainactivity.Constants;
import wichura.de.camperapp.models.RowItem;
import wichura.de.camperapp.util.BitmapHelper;

/**
 * Created by ich on 25.10.2016.
 * Camper App
 */

public class FileUploadService {

    private Context context;
    private NewAdActivity view;
    private Long adId;
    private Service service;

    public FileUploadService(Context context, NewAdActivity view) {
        this.context = context;
        this.view = view;
        this.service = new Service();
    }


    public void uploadNewArticle(Intent data) {

        String picture = data.getStringExtra(Constants.FILENAME);

        RowItem item = new RowItem();
        item.setTitle(data.getStringExtra(Constants.TITLE));
        item.setDescription(data.getStringExtra(Constants.DESCRIPTION));
        item.setPrice(data.getStringExtra(Constants.PRICE));
        item.setDate(data.getLongExtra(Constants.DATE, 0));
        item.setLat(getLat());
        item.setLng(getLng());

        service.saveNewAdObserv(getUserToken(), item)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<RowItem>() {
                    @Override
                    public void onCompleted() {
                        uploadPic(adId, picture);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d("CONAN", "error in upload new Article" + e.toString());
                    }

                    @Override
                    public void onNext(RowItem rowItem) {
                        adId = Long.parseLong(rowItem.getId().toString());
                        Log.d("CONAN", "new article added with id: " + adId);
                    }
                });
    }

    private void uploadPic(Long adId, String picture) {

        String fileString = getRealPathFromUri(context, Uri.parse(picture));
        File file = new File(fileString.toString());

        BitmapHelper bitmapHelper = new BitmapHelper(context);
        final File reducedPicture = bitmapHelper.saveBitmapToFile(file);

        RequestBody requestFile = RequestBody.create(MediaType.parse(context.getContentResolver().getType(Uri.parse(picture))), reducedPicture);

        MultipartBody.Part multiPartBody = MultipartBody.Part.createFormData("file", file.getName(), requestFile);

        service.uploadPictureObserv(adId, getUserToken(), multiPartBody)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<String>() {
                    @Override
                    public void onCompleted() {
                        view.hideProgress();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d("CONAN", "error in upload" + e.toString());
                    }

                    @Override
                    public void onNext(String status) {
                        Log.d("CONAN", "Picture uploaded");
                        Toast.makeText(context, "Upload...done!", Toast.LENGTH_SHORT).show();
                        Boolean deleted = reducedPicture.delete();
                        if (!deleted)
                            Toast.makeText(context, "Delete tempFile not possible", Toast.LENGTH_SHORT).show();
                        view.finish();
                    }
                });
    }

    private static String getRealPathFromUri(Context context, Uri contentUri) {
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

    private Double getLat() {
        SharedPreferences settings = context.getSharedPreferences(Constants.USERS_LOCATION, 0);
        return Double.longBitsToDouble(settings.getLong(Constants.LAT, 0));
    }

    private Double getLng() {
        SharedPreferences settings = context.getSharedPreferences(Constants.USERS_LOCATION, 0);
        return Double.longBitsToDouble(settings.getLong(Constants.LNG, 0));
    }

    private String getUserToken() {
        SharedPreferences settings = context.getSharedPreferences(Constants.SHARED_PREFS_USER_INFO, 0);
        return settings.getString(Constants.USER_TOKEN, "");
    }
}