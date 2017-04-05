package de.wichura.lks.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.wichura.lks.R;
import de.wichura.lks.dialogs.ZipDialogFragment;
import de.wichura.lks.http.FileUploadService;
import de.wichura.lks.http.Urls;
import de.wichura.lks.mainactivity.Constants;
import de.wichura.lks.models.FileNameParcelable;
import de.wichura.lks.presentation.NewArticlePresenter;

import static de.wichura.lks.mainactivity.Constants.SHARED_PREFS_USER_INFO;


public class NewAdActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        ZipDialogFragment.OnCompleteListener {

    private EditText mDescription;
    private EditText mTitle;
    private EditText mPrice;


    private static final int SELECT_PHOTO_ONE = 100;
    private static final int SELECT_PHOTO_TWO = 101;

    private ArrayList<FileNameParcelable> mImage;
    private int pictureCount = 0;

    private ImageView mImgOne;
    private ImageView mImgTwo;
    private ImageView errorImage;

    public ProgressBar progress;

    private NewArticlePresenter presenter;
    private FileUploadService fileUploadService;
    private Button submitButton;

    private Integer articleIdForEdit;
    private Boolean isEditMode;
    private Boolean isImageChanged;

    private LinearLayout emptyBackgroundLl;
    private LinearLayout mainLl;

    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private TextView locationName;
    private Boolean isLocationSet;
    private double lat;
    private double lng;


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        isLocationSet = false;
        mImage = new ArrayList<>();

        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        setContentView(R.layout.new_ad_acivity);

        Toolbar toolbar = (Toolbar) findViewById(R.id.new_ad_toolbar);
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
        isImageChanged = false;
        progress = (ProgressBar) findViewById(R.id.upload_ProgressBar);
        progress.setMax(100);
        hideProgress();

        emptyBackgroundLl = (LinearLayout) findViewById(R.id.upload_background);
        mainLl = (LinearLayout) findViewById(R.id.main_upload_linear_layout);

        fileUploadService = new FileUploadService(getApplicationContext(), this);
        presenter = new NewArticlePresenter(getApplicationContext(), this);

        mDescription = (EditText) findViewById(R.id.new_ad_description);
        mTitle = (EditText) findViewById(R.id.new_ad_title);
        mImgOne = (ImageView) findViewById(R.id.imageButton);
        mImgTwo = (ImageView) findViewById(R.id.imageButton2);
        errorImage = (ImageView) findViewById(R.id.problem_during_upload);
        hideProblem();
        mPrice = (EditText) findViewById(R.id.new_ad_price);

        mImgOne.setOnClickListener((v) -> {
            final Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
            photoPickerIntent.setType("image/*");
            photoPickerIntent.putExtra("imageOne", true);
            startActivityForResult(photoPickerIntent, SELECT_PHOTO_ONE);


            // Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            //startActivityForResult(cameraIntent, SELECT_PHOTO_ONE);
            //TODO camera plus photo picker
            //http://stackoverflow.com/questions/5991319/capture-image-from-camera-and-display-in-activity
        });

        mImgTwo.setOnClickListener(v -> {
            final Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
            photoPickerIntent.setType("image/*");
            photoPickerIntent.putExtra("imageTwo", true);
            startActivityForResult(photoPickerIntent, SELECT_PHOTO_TWO);
        });


        //edit my article:
        if (getIntent().getStringExtra(Constants.TITLE) != null) {
            isEditMode = true;
            getSupportActionBar().setTitle("Bearbeiten");
            mTitle.setText(getIntent().getStringExtra(Constants.TITLE));
            mDescription.setText(getIntent().getStringExtra(Constants.DESCRIPTION));
            mPrice.setText(getIntent().getStringExtra(Constants.PRICE));
            articleIdForEdit = getIntent().getIntExtra(Constants.ARTICLE_ID, 0);

            //TODO: mehrere bilder anzeige umstellen
            String pictureUri = Urls.MAIN_SERVER_URL_V3 + "pictures/" + (getIntent().getStringExtra(Constants.AD_URL));
            showProgress();
            Picasso.with(getApplicationContext())
                    .load(pictureUri)
                    .placeholder(R.drawable.empty_photo)
                    .fit()
                    .into(mImgOne, new Callback() {
                        @Override
                        public void onSuccess() {
                            hideProgress();
                        }

                        @Override
                        public void onError() {
                            hideProgress();
                            Toast.makeText(getApplicationContext(), "No network connection while loading picture!", Toast.LENGTH_SHORT).show();
                        }
                    });

            Log.d("CONAN", "edit: " + articleIdForEdit);
        }


        submitButton = (Button) findViewById(R.id.uploadButton);
        if (isEditMode) submitButton.setText("Speichern");
        submitButton.setOnClickListener((v) -> {

            final Intent data = new Intent();
            data.putExtra(Constants.TITLE, mTitle.getText().toString());
            data.putExtra(Constants.DESCRIPTION, mDescription.getText().toString());
            data.putParcelableArrayListExtra(Constants.FILENAME, mImage);
            data.putExtra(Constants.PRICE, mPrice.getText().toString());
            data.putExtra(Constants.DATE, System.currentTimeMillis());
            data.putExtra(Constants.LAT, lat);
            data.putExtra(Constants.LNG, lng);

            if (validateInputs() && !isEditMode) {
                disableUploadButton();
                fileUploadService.uploadNewArticle(data);
            }
            if (validateInputs() && isEditMode) {
                disableUploadButton();
                data.putExtra(Constants.ARTICLE_ID, articleIdForEdit);
                data.putExtra(Constants.AD_URL, getIntent().getStringExtra(Constants.AD_URL));
                data.putExtra(Constants.LAT, lat);
                data.putExtra(Constants.LNG, lng);
                data.putExtra(Constants.DATE, getIntent().getLongExtra(Constants.DATE, 0));
                if (isImageChanged) data.putExtra(Constants.FILENAME, mImage);

                fileUploadService.updateArticle(data);
            }
        });
    }

    @Override
    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    private void setupLocation() {

        locationName = (TextView) findViewById(R.id.create_location_name);

        //do not set location new if edit mode
        if (isEditMode) {
            lat = getIntent().getDoubleExtra(Constants.LAT, 0);
            lng = getIntent().getDoubleExtra(Constants.LNG, 0);
            presenter.getCityNameFromLatLng(lat, lng);
            isLocationSet = true;
        } else {
            if (!isLocationSet) {
                if (mLastLocation != null) {
                    lat = mLastLocation.getLatitude();
                    lng = mLastLocation.getLongitude();
                    presenter.getCityNameFromLatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                    isLocationSet = true;
                } else {
                    locationName.setText("Bitte auswählen");
                    isLocationSet = false;
                }
            }
        }

        locationName.setOnClickListener(v -> new ZipDialogFragment().show(getSupportFragmentManager(), null));

        ImageView location = (ImageView) findViewById(R.id.create_change_location);
        location.setOnClickListener(v -> new ZipDialogFragment().show(getSupportFragmentManager(), null));
    }

    @Override
    public void onZipCodeComplete(String zipCode) {
        Log.d("CONAN", "Zipcode from dialog: " + zipCode);
        getLatLngFromPlz(zipCode);
    }

    public void setCityName(String city) {
        locationName.setText(city);
    }

    public void showProblem(String error) {
        errorImage.setVisibility(View.VISIBLE);
        errorImage.setOnClickListener(v -> {
            Toast.makeText(this, "Problem beim Anlegen: " + error, Toast.LENGTH_LONG).show();
        });
    }

    public void hideProblem() {
        errorImage.setVisibility(View.GONE);
    }

    private void disableUploadButton() {
        submitButton.setEnabled(false);
    }

    public void enableUploadButton() {
        submitButton.setEnabled(true);
    }

    public void showProgress() {
        progress.setVisibility(ProgressBar.VISIBLE);
    }

    public void hideProgress() {
        progress.setVisibility(ProgressBar.GONE);
    }

    public void showMainProgress() {
        mainLl.setVisibility(View.GONE);
        emptyBackgroundLl.setVisibility(View.VISIBLE);
    }

    public void hideMainProgress() {
        emptyBackgroundLl.setVisibility(View.GONE);
        mainLl.setVisibility(View.VISIBLE);
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        switch (requestCode) {
            case SELECT_PHOTO_ONE:
                if (resultCode == RESULT_OK && pictureCount < 4) {
                    isImageChanged = true;
                    final Uri selectedImage = imageReturnedIntent.getData();
                    FileNameParcelable file = new FileNameParcelable(selectedImage.toString());
                    mImage.add(file);
                    switch (pictureCount) {
                        case 0: {
                            Picasso
                                    .with(getApplicationContext())
                                    .load(selectedImage)
                                    .fit()
                                    .into(mImgOne);
                            pictureCount++;
                            break;
                        }
                    }
                    break;
                }
            case SELECT_PHOTO_TWO: {
                if (resultCode == RESULT_OK) {
                    final Uri selectedImage = imageReturnedIntent.getData();
                    FileNameParcelable file = new FileNameParcelable(selectedImage.toString());
                    mImage.add(file);
                    Picasso
                            .with(getApplicationContext())
                            .load(selectedImage)
                            .fit()
                            .into(mImgTwo);
                    pictureCount++;
                    break;
                }
                break;
            }
        }
    }

    public void getLatLngFromPlz(String zip) {
        final Geocoder geocoder = new Geocoder(this);
        try {
            List<Address> addresses = geocoder.getFromLocationName(zip, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);

                //store lat lng for article
                lat = address.getLatitude();
                lng = address.getLongitude();

                //location is set -> for validation before upload new article
                isLocationSet = true;

                //show city name
                presenter.getCityNameFromLatLng(address.getLatitude(), address.getLongitude());
            } else {
                // Display appropriate message when Geocoder services are not available
                Toast.makeText(getApplicationContext(), "Hat leider nicht geklappt mit deiner PLZ, versuche nochmal!", Toast.LENGTH_LONG).show();
            }
        } catch (IOException e) {
            // handle exception
        }
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

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        int accessCoarseLoc = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION);
        if (accessCoarseLoc == PackageManager.PERMISSION_GRANTED)
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        //got the location -> get city name and show it on UI
        setupLocation();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}



