package de.wichura.lks.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.wichura.lks.R;
import de.wichura.lks.dialogs.ZipDialogFragment;
import de.wichura.lks.http.FileUploadService;
import de.wichura.lks.http.Urls;
import de.wichura.lks.mainactivity.Constants;
import de.wichura.lks.models.FileNameParcelable;
import de.wichura.lks.presentation.NewArticlePresenter;
import de.wichura.lks.util.SharedPrefsHelper;
import de.wichura.lks.util.Utility;
import jp.wasabeef.picasso.transformations.CropSquareTransformation;

import static de.wichura.lks.mainactivity.Constants.SHARED_PREFS_USER_INFO;


public class NewAdActivity extends AppCompatActivity implements ZipDialogFragment.OnCompleteListener {

    private EditText mDescription;
    private EditText mTitle;
    private EditText mPrice;

    private ArrayList<FileNameParcelable> fileNameParcelables;
    private FileNameParcelable[] mImageBuffer;
    private ArrayList<ImageView> imageView;
    private Boolean[] changedImages;
    private List<String> IMAGES;
    private HashMap<Integer, Long> deleteFilesList;

    private ArrayList<ImageView> removeImgButton;

    public ArrayList<ProgressBar> progress;

    private NewArticlePresenter presenter;
    private FileUploadService fileUploadService;
    private Button submitButton;

    private Integer articleIdForEdit;
    private Boolean isEditMode;

    private LinearLayout emptyBackgroundLl;
    private LinearLayout mainLl;

    private TextView locationName;
    public Boolean isLocationSet;
    private double lat;
    private double lng;

    private LinearLayout main;

    private SharedPrefsHelper sharedPrefsHelper;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedPrefsHelper = new SharedPrefsHelper(this);

        setContentView(R.layout.new_ad_acivity);
        main = findViewById(R.id.main_create_layout);
        main.setVisibility(View.GONE);

        //Android 6 and higher: request permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkReadWritePermission();
        } else {
            main.setVisibility(View.VISIBLE);
            initGui();
        }
    }

    private void initGui() {
        isLocationSet = false;
        fileNameParcelables = new ArrayList<>();
        IMAGES = new ArrayList<>();
        deleteFilesList = new HashMap<>();
        imageView = new ArrayList<>();
        progress = new ArrayList<>();
        removeImgButton = new ArrayList<>();
        mImageBuffer = new FileNameParcelable[5];
        changedImages = new Boolean[5];
        for (int i = 0; i < 5; i++) changedImages[i] = false;

        Toolbar toolbar = findViewById(R.id.new_ad_toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setDisplayShowHomeEnabled(true);
            }
            toolbar.setNavigationOnClickListener(view -> {
                if (isEditMode) {
                    Intent i = new Intent();
                    i.putExtra(Constants.IS_EDIT_MODE, "fromEditArticle");
                    setResult(RESULT_OK, i);
                    finish();
                }
                finish();
            });
        }

        isEditMode = false;

        setupLocation();

        initProgressBars();

        emptyBackgroundLl = findViewById(R.id.upload_background);
        mainLl = findViewById(R.id.main_upload_linear_layout);

        fileUploadService = new FileUploadService(getApplicationContext(), this);
        presenter = new NewArticlePresenter(getApplicationContext(), this);

        mDescription = findViewById(R.id.new_ad_description);
        mTitle = findViewById(R.id.new_ad_title);
        mPrice = findViewById(R.id.new_ad_price);

        initImageViews();

        initRemoveImgButtons();

        //edit my article:
        if (getIntent().getStringExtra(Constants.TITLE) != null) {
            isEditMode = true;
            getSupportActionBar().setTitle("Bearbeiten");
            mTitle.setText(getIntent().getStringExtra(Constants.TITLE));
            mDescription.setText(getIntent().getStringExtra(Constants.DESCRIPTION));

            Float price = getIntent().getFloatExtra(Constants.PRICE, 0);
            mPrice.setText(Utility.getPriceWithoutEuro(price));

            articleIdForEdit = getIntent().getIntExtra(Constants.ARTICLE_ID, 0);

            String pictureUris = getIntent().getStringExtra(Constants.AD_URL);
            if (pictureUris != null) {
                String[] uris = pictureUris.split(",");
                int size = uris.length;
                Log.d("CONAN", "size: " + size);

                for (int i = 0; i < size; i++) {
                    final int picture = i;
                    IMAGES.add(i, uris[i]);
                    imageView.get(i).setVisibility(View.VISIBLE);
                    removeImgButton.get(i).setVisibility(View.VISIBLE);
                    showProgressForPicture(i);
                    Picasso.with(getApplicationContext())
                            .load(Urls.MAIN_SERVER_URL_V3 + "pictures/" + IMAGES.get(i))
                            .placeholder(R.drawable.empty_photo)
                            .skipMemoryCache()
                            .fit()
                            .transform(new CropSquareTransformation())
                            .into(imageView.get(i), new Callback() {
                                @Override
                                public void onSuccess() {
                                    hideProgressForPicture(picture);
                                }

                                @Override
                                public void onError() {
                                    hideProgressForPicture(picture);
                                    Toast.makeText(getApplicationContext(), "No network connection while loading picture!", Toast.LENGTH_SHORT).show();
                                }
                            });
                }
                //show one more empty image view for user -> more images to add but no remove button
                if (size < 5) imageView.get(size).setVisibility(View.VISIBLE);

            } else {
                IMAGES.add(0, "");
            }
            Log.d("CONAN", "edit: " + articleIdForEdit);
        }

        submitButton = findViewById(R.id.uploadButton);
        if (isEditMode) submitButton.setText("Speichern");
        submitButton.setOnClickListener((v) -> {

            //copy FileNameParcelable[] mImageBuffer to ArrayList<FileNameParcelable> for intent
            prepareImageList();

            final Intent data = new Intent();
            data.putExtra(Constants.TITLE, mTitle.getText().toString());
            data.putExtra(Constants.DESCRIPTION, mDescription.getText().toString());
            data.putParcelableArrayListExtra(Constants.FILENAME, fileNameParcelables);
            data.putExtra(Constants.PRICE, mPrice.getText().toString());  //is Float in fileUploadService!!!
            data.putExtra(Constants.DATE, System.currentTimeMillis());
            data.putExtra(Constants.LAT, lat);
            data.putExtra(Constants.LNG, lng);

            if (validateInputs() && !isEditMode) {
                disableUploadButton();
                fileUploadService.uploadNewArticle(data);
            }
            if (validateInputs() && isEditMode) {
                disableUploadButton();
                prepareFilesToDelete(IMAGES);
                data.putExtra(Constants.ARTICLE_ID, articleIdForEdit);
                data.putExtra(Constants.LAT, lat);
                data.putExtra(Constants.LNG, lng);
                data.putExtra(Constants.DATE, getIntent().getLongExtra(Constants.DATE, 0));
                data.putExtra(Constants.AD_URL, getIntent().getStringExtra(Constants.AD_URL));
                //TODO sind das alle geänderten und evetl hinzugekommen bilder?
                data.putParcelableArrayListExtra(Constants.FILENAME, fileNameParcelables);

                fileUploadService.updateArticle(data, deleteFilesList);
            }
        });
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void checkReadWritePermission() {

        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            //permission granted, just go on
            main.setVisibility(View.VISIBLE);
            initGui();
        } else {
            if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                Toast.makeText(this, "Die App benötigt Lese- und Schreiberechtigungen, um Anzeigen erstellen zu können!", Toast.LENGTH_LONG).show();
                Intent i = new Intent();
                i.putExtra(Constants.PERMISSION_DENIED, "permission");
                setResult(RESULT_OK, i);
                finish();
            } else {
                //nothing to do here -> user wants to use upload first time
            }
            //ask for permission
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE}, Constants.REQUEST_ID_FOR_FILE_PERMISSION);
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {

            //TODO der ablauf ist mir noch nicht ganz klar...
            case Constants.REQUEST_ID_FOR_FILE_PERMISSION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //permission granted, just go on
                    main.setVisibility(View.VISIBLE);
                    initGui();
                } else {
                    //permission not granted -> go back
                    finish();
                    //Toast.makeText(this, "Ohne Zustimmung können leider keine eigenen Anzeigen erstellt werden!", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private void initProgressBars() {
        progress.add(findViewById(R.id.upload_ProgressBar1));
        progress.add(findViewById(R.id.upload_ProgressBar2));
        progress.add(findViewById(R.id.upload_ProgressBar3));
        progress.add(findViewById(R.id.upload_ProgressBar4));
        progress.add(findViewById(R.id.upload_ProgressBar5));

        for (ProgressBar pb : progress) {
            pb.setVisibility(View.GONE);
            pb.setMax(100);
        }
    }

    private void initImageViews() {
        imageView.add(findViewById(R.id.imageButton));
        imageView.add(findViewById(R.id.imageButton2));
        imageView.add(findViewById(R.id.imageButton3));
        imageView.add(findViewById(R.id.imageButton4));
        imageView.add(findViewById(R.id.imageButton5));

        for (int i = 0; i < 5; i++) {
            final Integer COUNTER = i;
            imageView.get(i).setOnClickListener((v) -> {
                final Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                photoPickerIntent.putExtra("image", true);
                startActivityForResult(photoPickerIntent, COUNTER);
            });
        }
    }

    private void initRemoveImgButtons() {
        removeImgButton.add(findViewById(R.id.removeImage1));
        removeImgButton.add(findViewById(R.id.removeImage2));
        removeImgButton.add(findViewById(R.id.removeImage3));
        removeImgButton.add(findViewById(R.id.removeImage4));
        removeImgButton.add(findViewById(R.id.removeImage5));

        for (int i = 0; i < 5; i++) {
            final Integer COUNTER = i;
            removeImgButton.get(i).setOnClickListener(v -> {
                removeImageAndUpdate(COUNTER);
            });
        }
    }

    private void removeImageAndUpdate(Integer counter) {
        imageView.get(counter).setImageResource(R.drawable.empty_photo);
        removeImgButton.get(counter).setVisibility(View.GONE);
        //wenn altes bild -> add to remove list
        changedImages[counter] = true;
    }

    private void prepareFilesToDelete(List<String> images) {
        deleteFilesList.clear();
        for (int i = 0; i < 5; i++) {
            if (changedImages[i]) {
                //if image.size() is smaller than i -> new picture was added, nothing old to delete
                if (images.size() > i) {
                    if (!"".equals(images.get(i))) {
                        deleteFilesList.put(i, Long.parseLong(images.get(i)));
                    }
                }
            }
        }
    }

    private void prepareImageList() {
        fileNameParcelables.clear();
        for (int i = 0; i < 5; i++) {
            if (mImageBuffer[i] != null)
                fileNameParcelables.add(mImageBuffer[i]);
        }
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        Log.d("CONAN", "requestCode: " + requestCode);
        int pictureCount = requestCode;

        if (resultCode == RESULT_OK) {
            final Uri selectedImage = imageReturnedIntent.getData();
            FileNameParcelable file = new FileNameParcelable(selectedImage.toString());
            mImageBuffer[pictureCount] = file;
            Picasso.with(getApplicationContext())
                    .load(selectedImage)
                    .transform(new CropSquareTransformation())
                    .skipMemoryCache()
                    .fit()
                    .into(imageView.get(pictureCount), new Callback() {
                        @Override
                        public void onSuccess() {
                            //only show view for 5 images, after this no more new views!
                            if (pictureCount < 4) {
                                imageView.get(pictureCount + 1).setVisibility(View.VISIBLE);
                            }
                            //show remove image view when new image is shown
                            removeImgButton.get(pictureCount).setVisibility(View.VISIBLE);

                            HorizontalScrollView s = (HorizontalScrollView) findViewById(R.id.horizontal_scroll_view);
                            s.postDelayed(new Runnable() {
                                public void run() {
                                    s.fullScroll(HorizontalScrollView.FOCUS_RIGHT);
                                }
                            }, 500L);
                        }

                        @Override
                        public void onError() {
                        }
                    });
            //which image is changed
            if (isEditMode) {
                changedImages[pictureCount] = true;
            }
        }
    }

    private void setupLocation() {

        locationName = findViewById(R.id.create_location_name);

        //do not set location new if edit mode
        if (isEditMode) {
            lat = getIntent().getDoubleExtra(Constants.LAT, 0);
            lng = getIntent().getDoubleExtra(Constants.LNG, 0);
            String location = getIntent().getStringExtra(Constants.LOCATION_NAME);
            setCityName(location);
            isLocationSet = true;
        } else {
            if (!sharedPrefsHelper.getLastLocationName().equals("")) {
                lat = sharedPrefsHelper.getLastLat();
                lng = sharedPrefsHelper.getLastLng();
                setCityName(sharedPrefsHelper.getLastLocationName());
                isLocationSet = true;
            } else {
                locationName.setText("Bitte auswählen");
                isLocationSet = false;
            }
        }
        locationName.setOnClickListener(v -> new ZipDialogFragment().show(getSupportFragmentManager(), null));

        ImageView location = findViewById(R.id.create_change_location);
        location.setOnClickListener(v -> new ZipDialogFragment().show(getSupportFragmentManager(), null));
    }

    @Override
    public void onZipCodeComplete(String zipCode) {
        Log.d("CONAN", "Zipcode from dialog: " + zipCode);
        getLatLngFromLocation(zipCode);
    }

    public void setCityName(String city) {
        locationName.setText(city);
    }

    public void disableUploadButton() {
        submitButton.setEnabled(false);
    }

    public void enableUploadButton() {
        submitButton.setEnabled(true);
    }

    public void showProgressForPicture(int i) {
        progress.get(i).setVisibility(ProgressBar.VISIBLE);
    }

    public void hideProgressForPicture(int i) {
        progress.get(i).setVisibility(ProgressBar.GONE);
    }

    public void showMainProgress() {
        mainLl.setVisibility(View.GONE);
        emptyBackgroundLl.setVisibility(View.VISIBLE);
    }

    public void hideMainProgress() {
        emptyBackgroundLl.setVisibility(View.GONE);
        mainLl.setVisibility(View.VISIBLE);
    }

    public void getLatLngFromLocation(String location) {
        //TODO unterscheiden ob plz oder ortsname?
        /*if (location.matches("[0-9]+") && location.length() > 2) {
            presenter.getLatLngFromZip(location);
        } else {
            presenter.getLatLngFromAddress(location);
        }*/
        presenter.getLatLngFromAddress(location);
    }

    public boolean validateInputs() {
        boolean valid = true;

        String title = mTitle.getText().toString();
        if (title.isEmpty()) {
            mTitle.setError("Der Titel darf nicht leer sein!");
            valid = false;
        } else {
            mTitle.setError(null);
        }

        String desc = mDescription.getText().toString();
        if (desc.isEmpty()) {
            mDescription.setError("Die Beschreibung darf nicht leer sein!");
            valid = false;
        } else {
            mDescription.setError(null);
        }

        String price = mPrice.getText().toString();
        if (price.isEmpty()) {
            mPrice.setError("Der Preis darf nicht leer sein!");
            valid = false;
        } else if (Integer.parseInt(price) >= Integer.MAX_VALUE) {
            mPrice.setError("Komm schon, etwas teuer oder?");
            valid = false;
        } else {
            mPrice.setError(null);
        }

        if (!isLocationSet) {
            locationName.setError("Location nicht gesetzt!");
            valid = false;
        }

        return valid;
    }

    public String getUserToken() {
        return getSharedPreferences(SHARED_PREFS_USER_INFO, 0).getString(Constants.USER_TOKEN, "");
    }
}