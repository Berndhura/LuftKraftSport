package de.wichura.lks.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import de.wichura.lks.R;
import de.wichura.lks.http.FileUploadService;
import de.wichura.lks.mainactivity.Constants;

import static de.wichura.lks.mainactivity.Constants.SHARED_PREFS_USER_INFO;


public class NewAdActivity extends AppCompatActivity {

    private EditText mDescText;
    private EditText mKeywords;
    private EditText mPrice;


    private static final int SELECT_PHOTO = 100;
    private String mImage;
    private int pictureCount = 1;

    private ImageView mImgOne;
    private ImageView errorImage;

    public ProgressBar progress;

    private FileUploadService fileUploadService;
    private Button submitButton;


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.new_ad_acivity);

        Toolbar toolbar = (Toolbar) findViewById(R.id.new_ad_toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setDisplayShowHomeEnabled(true);
            }
            toolbar.setNavigationOnClickListener((view) -> finish());
        }


        progress = (ProgressBar) findViewById(R.id.upload_ProgressBar);
        progress.setMax(100);
        hideProgress();
        fileUploadService = new FileUploadService(getApplicationContext(), this);

        mDescText = (EditText) findViewById(R.id.description);
        mKeywords = (EditText) findViewById(R.id.keywords);
        mImgOne = (ImageView) findViewById(R.id.imageButton);
        errorImage = (ImageView) findViewById(R.id.problem_during_upload);
        hideProblem();
        mPrice = (EditText) findViewById(R.id.preis);


        submitButton = (Button) findViewById(R.id.uploadButton);
        submitButton.setOnClickListener((v) -> {

            final String titleString = mKeywords.getText().toString();
            final String descString = mDescText.getText().toString();
            final String price = mPrice.getText().toString();
            final String keyWordsString = "zelt";
            final long date = System.currentTimeMillis();

            final Intent data = new Intent();
            data.putExtra(Constants.TITLE, titleString);
            data.putExtra(Constants.ARTICLE_ID, "arcticleId");
            data.putExtra(Constants.DESCRIPTION, descString);
            data.putExtra(Constants.KEYWORDS, keyWordsString);
            data.putExtra(Constants.FILENAME, mImage);
            data.putExtra(Constants.LOCATION, "TODO");
            data.putExtra(Constants.PHONE, "PHONE");
            data.putExtra(Constants.PRICE, price);
            data.putExtra(Constants.DATE, date);

            disableUploadButton();
            fileUploadService.uploadNewArticle(data);
            //setResult(RESULT_OK, data);
        });

        final ImageView getPictureButton = (ImageView) findViewById(R.id.imageButton);

        getPictureButton.setOnClickListener((v) -> {
            final Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
            photoPickerIntent.setType("image/*");
            startActivityForResult(photoPickerIntent, SELECT_PHOTO);


            // Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            //startActivityForResult(cameraIntent, SELECT_PHOTO);
            //TODO camera plus photo picker
            //http://stackoverflow.com/questions/5991319/capture-image-from-camera-and-display-in-activity
        });


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

    @Override
    public void onActivityResult(final int requestCode,
                                 final int resultCode, final Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        switch (requestCode) {
            case SELECT_PHOTO:
                if (resultCode == RESULT_OK && pictureCount < 4) {
                    final Uri selectedImage = imageReturnedIntent.getData();
                    mImage = selectedImage.toString();
                    switch (pictureCount) {
                        case 1: {
                            Picasso
                                    .with(getApplicationContext())
                                    .load(selectedImage)
                                    .fit()
                                    .into(mImgOne);
                            pictureCount++;
                            break;
                        }
                    }
                }
        }
    }

    public String getUserToken() {
        return getSharedPreferences(SHARED_PREFS_USER_INFO, 0).getString(Constants.USER_TOKEN, "");
    }
}



