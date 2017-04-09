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
import java.util.ArrayList;

import de.wichura.lks.activity.NewAdActivity;
import de.wichura.lks.mainactivity.Constants;
import de.wichura.lks.models.FileNameParcelable;
import de.wichura.lks.models.Location;
import de.wichura.lks.models.RowItem;
import de.wichura.lks.util.BitmapHelper;
import okhttp3.MultipartBody;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Bernd Wichura on 25.10.2016.
 * Luftkrafsport
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
        //TODO mist oder
        item.setPrice(Float.parseFloat(data.getStringExtra(Constants.PRICE)));
        item.setDate(data.getLongExtra(Constants.DATE, 0));
        item.setUrl(data.getStringExtra(Constants.AD_URL));

        double[] latlng = {data.getDoubleExtra(Constants.LAT, 0), data.getDoubleExtra(Constants.LNG, 0)};
        Location location = new Location();
        location.setCoordinates(latlng);
        location.setType("Point");
        item.setLocation(location);

        final ArrayList<FileNameParcelable> mImage = new ArrayList<>(data.getParcelableArrayListExtra(Constants.FILENAME));

        service.saveNewAdObserv(getUserToken(), item)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<RowItem>() {
                    @Override
                    public void onCompleted() {
                        view.hideProgress();
                        if (data.getStringExtra(Constants.FILENAME) != null) {
                            view.hideMainProgress();
                            uploadPic(adId, mImage);
                        } else {
                            view.finish();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        view.hideProgress();
                        Log.d("CONAN", "error in updating Article" + e.toString());
                        Toast.makeText(view, "Problem beim Senden der Daten!" + e, Toast.LENGTH_SHORT).show();
                        String error;
                        if (e.toString().contains("SocketTimeoutException")) {
                            error = "Probleme mit der Verbindung";
                        } else {
                            error = e.toString();
                        }
                        view.showProblem(error);
                        view.enableUploadButton();
                    }

                    @Override
                    public void onNext(RowItem rowItem) {
                        adId = Long.parseLong(rowItem.getId().toString());
                    }
                });

    }

    public void uploadNewArticle(Intent data) {

        view.showMainProgress();
        view.hideProblem();

        final ArrayList<FileNameParcelable> mImage = new ArrayList<>(data.getParcelableArrayListExtra(Constants.FILENAME));

        RowItem item = new RowItem();
        item.setTitle(data.getStringExtra(Constants.TITLE));
        item.setDescription(data.getStringExtra(Constants.DESCRIPTION));
        //TODO Mist oder float.parse(...)
        item.setPrice(Float.parseFloat(data.getStringExtra(Constants.PRICE)));
        item.setDate(data.getLongExtra(Constants.DATE, 0));

        double[] latlng = {data.getDoubleExtra(Constants.LAT, 0), data.getDoubleExtra(Constants.LNG, 0)};
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
                        uploadPic(adId, mImage);
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

    private void uploadPic(Long adId, ArrayList<FileNameParcelable> imageFiles) {

        for (int i = 0; i < imageFiles.size(); i++) {

            view.showProgress();

            final int counter = i;

            String fileString = getRealPathFromUri(context, Uri.parse(imageFiles.get(i).getFileName()));

            File file = new File(fileString);

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
                            //finish activity when last file uploaded
                            if (counter == imageFiles.size()-1) {
                                Toast.makeText(context, "Neue Anzeige erstellt!", Toast.LENGTH_SHORT).show();
                                view.finish();
                            }
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
                            view.hideProgress();
                            Log.d("CONAN", "Picture uploaded");
                            //TODO unterscheiden ob new oder update -> Toast anpassen
                            Boolean deleted = reducedPicture.delete();
                            if (!deleted)
                                Toast.makeText(context, "Delete tempFile not possible", Toast.LENGTH_SHORT).show();
                        }
                    });
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