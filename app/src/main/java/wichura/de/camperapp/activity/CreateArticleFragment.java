package wichura.de.camperapp.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import wichura.de.camperapp.R;
import wichura.de.camperapp.http.FileUploadService;
import wichura.de.camperapp.http.Service;
import wichura.de.camperapp.mainactivity.Constants;
import wichura.de.camperapp.util.Utility;

import static android.app.Activity.RESULT_OK;
import static com.facebook.FacebookSdk.getApplicationContext;
import static wichura.de.camperapp.mainactivity.Constants.SHARED_PREFS_USER_INFO;

/**
 * Created by ich on 11.12.2016.
 * Camper App
 */

public class CreateArticleFragment extends Fragment {

    private Service service;
    private Utility utils;

    private EditText mDescText;
    private EditText mKeywords;
    private EditText mPrice;


    private static final int SELECT_PHOTO = 100;
    private String mImage;
    private int pictureCount = 1;

    private ImageView mImgOne;
    private ImageView mImgTwo;

    //private ProgressBar progress;

    private FileUploadService fileUploadService;

    public CreateArticleFragment() {
        service = new Service();
        utils = new Utility(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        return inflater.inflate(R.layout.create_article_fragemnt, container, false);
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        //progress = (ProgressBar) view.findViewById(R.id.upload_ProgressBar);
        //hideProgress();
        fileUploadService = new FileUploadService(getActivity().getApplicationContext(), (NewAdActivity) getActivity());

        mDescText = (EditText) view.findViewById(R.id.description);
        mKeywords = (EditText) view.findViewById(R.id.keywords);
        mImgOne = (ImageView) view.findViewById(R.id.imageButton);
        mPrice = (EditText) view.findViewById(R.id.preis);
        mImgOne = (ImageView) view.findViewById(R.id.imageButton);


        final Button submitButton = (Button) view.findViewById(R.id.uploadButton);
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

            fileUploadService.multiPost(data);
            //setResult(RESULT_OK, data);
        });

        final ImageView getPictureButton = (ImageView) view.findViewById(R.id.imageButton);

        getPictureButton.setOnClickListener((v) -> {
            final Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
            photoPickerIntent.setType("image/*");
            startActivityForResult(photoPickerIntent, SELECT_PHOTO);
        });
    }

   /* public void showProgress() {
        progress.setVisibility(ProgressBar.VISIBLE);
    }

    public void hideProgress() {
        progress.setVisibility(ProgressBar.GONE);
    }*/

    @Override
    public void onActivityResult(final int requestCode,
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
                            Picasso
                                    .with(getApplicationContext())
                                    .load(selectedImage)
                                    .fit()
                                    .into(mImgOne);
                            pictureCount++;
                            break;
                        }
                        case 2: {
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

    public String getUserToken() {
        return getActivity().getSharedPreferences(SHARED_PREFS_USER_INFO, 0).getString(Constants.USER_TOKEN, "");
    }
}
