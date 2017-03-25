package de.wichura.lks.http;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

import de.wichura.lks.activity.NewAdActivity;
import de.wichura.lks.mainactivity.Constants;
import de.wichura.lks.models.Location;
import de.wichura.lks.models.RowItem;
import de.wichura.lks.util.BitmapHelper;
import okhttp3.MultipartBody;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by ich on 25.10.2016.
 * Camper App
 */

public class FileUploadService implements ProgressRequestBody.UploadCallbacks {

    private Context context;
    private NewAdActivity view;
    private Long adId;
    private Service service;

    public FileUploadService(Context context, NewAdActivity view) {
        this.context = context;
        this.view = view;
        this.service = new Service();
    }

    public void updateArticle(Intent data) {

        RowItem item = new RowItem();
        item.setId(data.getIntExtra(Constants.ARTICLE_ID, 0));
        item.setTitle(data.getStringExtra(Constants.TITLE));
        item.setDescription(data.getStringExtra(Constants.DESCRIPTION));
        item.setPrice(data.getStringExtra(Constants.PRICE));
        item.setDate(data.getLongExtra(Constants.DATE, 0));

        double[] latlng = {getLat(), getLng()};
        Location location = new Location();
        location.setCoordinates(latlng);
        location.setType("Point");
        item.setLocation(location);

        service.saveNewAdObserv(getUserToken(), item)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<RowItem>() {
                    @Override
                    public void onCompleted() {
                        view.hideProgress();
                        view.finish();
                    }

                    @Override
                    public void onError(Throwable e) {
                        view.hideProgress();
                        Log.d("CONAN", "error in updating Article" + e.toString());
                        Toast.makeText(view, "Problem beim Senden der Daten!" + e, Toast.LENGTH_SHORT).show();
                        String error;
                        if (e.toString().contains("SocketTimeoutException")) {
                            error = "Timeout im Netzwerk";
                        } else {
                            error = e.toString();
                        }
                        view.showProblem(error);
                        view.enableUploadButton();
                    }

                    @Override
                    public void onNext(RowItem rowItem) {

                    }
                });

    }

    public void uploadNewArticle(Intent data) {

        view.showMainProgress();
        view.hideProblem();

        String picture = data.getStringExtra(Constants.FILENAME);

        RowItem item = new RowItem();
        item.setTitle(data.getStringExtra(Constants.TITLE));
        item.setDescription(data.getStringExtra(Constants.DESCRIPTION));
        item.setPrice(data.getStringExtra(Constants.PRICE));
        item.setDate(data.getLongExtra(Constants.DATE, 0));

        double[] latlng = {getLat(), getLng()};
        Location location = new Location();
        location.setCoordinates(latlng);
        location.setType("Point");
        item.setLocation(location);

        service.saveNewAdObserv(getUserToken(), item)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<RowItem>() {
                    @Override
                    public void onCompleted() {
                        view.hideMainProgress();
                        uploadPic(adId, picture);
                    }

                    @Override
                    public void onError(Throwable e) {
                        view.hideProgress();
                        Log.d("CONAN", "error in upload new Article" + e.toString());
                        Toast.makeText(view, "Problem beim Senden der Daten!" + e, Toast.LENGTH_SHORT).show();
                        String error;
                        if (e.toString().contains("SocketTimeoutException")) {
                            error = "Timeout im Netzwerk";
                        } else {
                            error = e.toString();
                        }
                        view.showProblem(error);
                        view.enableUploadButton();
                    }

                    @Override
                    public void onNext(RowItem rowItem) {
                        adId = Long.parseLong(rowItem.getId().toString());
                        Log.d("CONAN", "new article added with id: " + adId);
                    }
                });
    }

    private void uploadPic(Long adId, String picture) {

        view.showProgress();

        if (picture != null) {

            String fileString = getRealPathFromUri(context, Uri.parse(picture));

            File file = new File(fileString.toString());

            ExifInterface exif = null;
            try {
                exif = new ExifInterface(file.getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
            }
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_UNDEFINED);

            Log.d("CONAN", "picture orientation: " + orientation);

            BitmapHelper bitmapHelper = new BitmapHelper(context);
            final File reducedPicture = bitmapHelper.saveBitmapToFile(file);

            //RequestBody requestFile = RequestBody.create(MediaType.parse(context.getContentResolver().getType(Uri.parse(picture))), reducedPicture);
            ProgressRequestBody requestFile = new ProgressRequestBody(reducedPicture, this);


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
                            view.hideProgress();
                            Log.d("CONAN", "error in upload" + e.toString());
                            Toast.makeText(view, "Problem beim Senden der Daten!", Toast.LENGTH_SHORT).show();
                            String error;
                            if (e.toString().contains("SocketTimeoutException")) {
                                error = "Timeout im Netzwerk";
                            } else {
                                error = e.toString();
                            }
                            view.showProblem(error);
                            view.enableUploadButton();
                        }

                        @Override
                        public void onNext(String status) {
                            Log.d("CONAN", "Picture uploaded");
                            Toast.makeText(context, "Neue Anzeige erstellt!", Toast.LENGTH_SHORT).show();
                            Boolean deleted = reducedPicture.delete();
                            if (!deleted)
                                Toast.makeText(context, "Delete tempFile not possible", Toast.LENGTH_SHORT).show();
                            view.finish();
                        }
                    });
        } else {
            Toast.makeText(context, "Neue Anzeige erstellt!", Toast.LENGTH_SHORT).show();
            view.finish();
        }
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

    @Override
    public void onProgressUpdate(int percentage) {
        view.progress.setProgress(percentage);
    }

    @Override
    public void onError() {
    }

    @Override
    public void onFinish() {
        view.progress.setProgress(100);
    }
}