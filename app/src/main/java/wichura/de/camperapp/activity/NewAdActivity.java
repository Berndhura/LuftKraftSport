package wichura.de.camperapp.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.squareup.picasso.Picasso;

import wichura.de.camperapp.R;
import wichura.de.camperapp.http.FileUploadService;
import wichura.de.camperapp.mainactivity.Constants;


public class NewAdActivity extends AppCompatActivity {

    private EditText mDescText;
    private EditText mKeywords;
    private EditText mPrice;

    private static final int SELECT_PHOTO = 100;
    private String mImage;
    private int pictureCount = 1;

    private ImageView mImgOne;
    private ImageView mImgTwo;

    private ProgressBar progress;

    private FileUploadService fileUploadService;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.new_ad_acivity);

        Toolbar toolbar = (Toolbar) findViewById(R.id.new_ad_toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            toolbar.setNavigationOnClickListener((view) -> finish());
        }

        fileUploadService = new FileUploadService(getApplicationContext(), this);

        progress = (ProgressBar) findViewById(R.id.upload_ProgressBar);
        hideProgress();

        mDescText = (EditText) findViewById(R.id.description);
        mKeywords = (EditText) findViewById(R.id.keywords);
        mImgOne = (ImageView) findViewById(R.id.imageButton);
        mPrice = (EditText) findViewById(R.id.preis);

        final Button submitButton = (Button) findViewById(R.id.uploadButton);
        submitButton.setOnClickListener((v) -> {

            final String titleString = mKeywords.getText().toString();
            final String descString = mDescText.getText().toString();
            final String price = mPrice.getText().toString();
            final String keyWordsString = "zelt";
            final long date = System.currentTimeMillis();

            final Intent data = new Intent();
            data.putExtra(Constants.TITLE, titleString);
            data.putExtra(Constants.AD_ID, "apid");
            data.putExtra(Constants.DESCRIPTION, descString);
            data.putExtra(Constants.KEYWORDS, keyWordsString);
            data.putExtra(Constants.FILENAME, mImage);
            data.putExtra(Constants.LOCATION, "TODO");
            data.putExtra(Constants.PHONE, "PHONE");
            data.putExtra(Constants.PRICE, price);
            data.putExtra(Constants.DATE, date);

            fileUploadService.multiPost(data);
            setResult(RESULT_OK, data);
        });

        final ImageView getPictureButton = (ImageView) findViewById(R.id.imageButton);

        getPictureButton.setOnClickListener((v) -> {
            final Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
            photoPickerIntent.setType("image/*");
            startActivityForResult(photoPickerIntent, SELECT_PHOTO);
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
                            Picasso
                                    .with(getApplicationContext())
                                    .load(selectedImage)
                                    .fit()
                                    .into(mImgOne);
                            pictureCount++;
                            break;
                        }
                        case 2: {
                            mImgTwo = (ImageView) findViewById(R.id.imageButton);
                            Picasso
                                    .with(getApplicationContext())
                                    .load(selectedImage)
                                    .fit()
                                    .into(mImgTwo);
                            pictureCount++;
                            break;
                        }
                    }
                }
        }
    }


    public void showProgress() {
        progress.setVisibility(ProgressBar.VISIBLE);
    }

    public void hideProgress() {
        progress.setVisibility(ProgressBar.GONE);
    }
}


