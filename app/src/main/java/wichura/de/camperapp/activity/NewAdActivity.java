package wichura.de.camperapp.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

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

    private SectionsPagerAdapter mSectionsPagerAdapter;

    private ViewPager mViewPager;

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

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

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
            data.putExtra(Constants.ARTICLE_ID, "arcticleId");
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


    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.searches_activity, container, false);
           // TextView textView = (TextView) rootView.findViewById(R.id.section_label);
           // textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));
            return rootView;
        }
    }


    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    private class SectionsPagerAdapter extends FragmentPagerAdapter {

        private SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Sell";
                case 1:
                    return "Seek";
            }
            return null;
        }
    }


    public void showProgress() {
        progress.setVisibility(ProgressBar.VISIBLE);
    }

    public void hideProgress() {
        progress.setVisibility(ProgressBar.GONE);
    }
}



