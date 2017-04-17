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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.wichura.lks.activity.NewAdActivity;
import de.wichura.lks.mainactivity.Constants;
import de.wichura.lks.models.FileNameParcelable;
import de.wichura.lks.models.Location;
import de.wichura.lks.models.RowItem;
import de.wichura.lks.util.BitmapHelper;
import okhttp3.MultipartBody;
import rx.Observable;
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

    private void removeIdFromImageList(Intent data, Long imageId) {

        String[] imageArray;
        String imageList = data.getStringExtra(Constants.AD_URL);

        if (!"".equals(imageList)) {
            imageArray = imageList.split(",");
        } else {
            //nothing to remove
            return;
        }

        //remove image id
        for (int i = 0; i < imageArray.length; i++) {
            if (imageId.toString().equals(imageArray[i])) {
                imageArray[i] = null;
            }
        }

        //store new imageList to data
        String newImageList = "";
        for (int i = 0; i < imageArray.length; i++) {
            if (imageArray[i] != null) {
                if ("".equals(newImageList)) {
                    newImageList = imageArray[i];
                } else {
                    newImageList = newImageList + "," + imageArray[i];
                }
            }
        }

        data.putExtra(Constants.AD_URL, newImageList);
    }

    public void updateArticle(Intent data, HashMap<Integer, Long> filesToDelete) {

        Integer articleId = data.getIntExtra(Constants.ARTICLE_ID, 0);

        if (filesToDelete.size() > 0) {
            //first delete old files
            List<Long> ids = new ArrayList<>();
            Set filesToDeleteSet = filesToDelete.entrySet();
            Iterator iterator = filesToDeleteSet.iterator();
            while (iterator.hasNext()) {
                Map.Entry entry = (Map.Entry) iterator.next();
                ids.add((Long) entry.getValue());

                //remove imageIds from list -> important for updating the article itself
                removeIdFromImageList(data, (Long) entry.getValue());
            }

            //delete removed images
            Observable.from(ids)
                    .flatMap(imageId -> service.deletePictureObserv(Long.parseLong(articleId.toString()), imageId, getUserToken()))
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Subscriber<String>() {
                        @Override
                        public void onCompleted() {
                            Log.d("CONAN", "delete images COMPLETE");
                            updateTextAndNewImages(data);
                        }

                        @Override
                        public void onError(Throwable e) {
                            Log.d("CONAN", "delete images ERROR" + e.toString());
                        }

                        @Override
                        public void onNext(String rowItem) {
                            Log.d("CONAN", "delete images ONNEXT " + rowItem);
                        }
                    });
        } else {
            //AD_URL adapted, images deleted -> now update article itself, upload possible new images
            updateTextAndNewImages(data);
        }
    }

    private void updateTextAndNewImages(Intent data) {

        final ArrayList<FileNameParcelable> newFilesForUpload = new ArrayList<>(data.getParcelableArrayListExtra(Constants.FILENAME));

        Integer articleId = data.getIntExtra(Constants.ARTICLE_ID, 0);

        RowItem item = new RowItem();
        item.setId(data.getIntExtra(Constants.ARTICLE_ID, 0));
        item.setTitle(data.getStringExtra(Constants.TITLE));
        item.setDescription(data.getStringExtra(Constants.DESCRIPTION));
        item.setPrice(Float.parseFloat(data.getStringExtra(Constants.PRICE)));
        item.setDate(data.getLongExtra(Constants.DATE, 0));

        double[] latlng = {data.getDoubleExtra(Constants.LAT, 0), data.getDoubleExtra(Constants.LNG, 0)};
        Location location = new Location();
        location.setCoordinates(latlng);
        location.setType("Point");
        item.setLocation(location);


        //no image changes/edits/delete -> only use old ones
        //if URLs string is empty do not set URLS -> URLs are NULL
        if (!"".equals(data.getStringExtra(Constants.AD_URL))) {
            item.setUrl(data.getStringExtra(Constants.AD_URL));
        }

        service.saveNewAdObserv(getUserToken(), item)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<RowItem>() {
                    @Override
                    public void onCompleted() {
                        view.hideProgress();
                        if (newFilesForUpload.size() > 0) {
                            view.hideMainProgress();
                            uploadPic(Long.parseLong(articleId.toString()), newFilesForUpload);
                        } else {
                            Toast.makeText(view, "Anzeige geändert!", Toast.LENGTH_SHORT).show();
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
                        Log.d("CONAN", "OnNExt in updating Article");
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
                            if (counter == imageFiles.size() - 1) {
                                Toast.makeText(context, "Neue Anzeige erstellt/geändert!", Toast.LENGTH_SHORT).show();
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