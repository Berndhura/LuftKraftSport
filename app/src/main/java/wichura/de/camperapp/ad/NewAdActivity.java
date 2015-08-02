package wichura.de.camperapp.ad;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import wichura.de.camperapp.R;
import wichura.de.camperapp.http.HttpHelper;

public class NewAdActivity extends Activity {

	private EditText mTitleText;
	private EditText mDescText;
	private EditText mKeywords;

	private static final int SELECT_PHOTO = 100;
	private String mImage;
	private int pictureCount=1;

	private ImageView mImgOne;
	private ImageView mImgTwo;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.new_ad_acivity);

		mTitleText = (EditText) findViewById(R.id.title);
		mDescText = (EditText) findViewById(R.id.description);
		mKeywords = (EditText) findViewById(R.id.keywords);
		mImgOne = (ImageView) findViewById(R.id.picturOne);
		mImgTwo = (ImageView) findViewById(R.id.picturTwo);

		final Button submitButton = (Button) findViewById(R.id.submitButton);
		submitButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {

				final String titleString = mTitleText.getText().toString();
				final String descString = mDescText.getText().toString();
				final String keyWordsString = mKeywords.getText().toString();

				// Package ToDoItem data into an Intent
				final Intent data = new Intent();
				AdItem.packageIntent(data, titleString, descString,
						keyWordsString, mImage);

				// TODO - return data Intent and finish
				setResult(RESULT_OK, data);

				sendHttpToServer(data);

				finish();
			}
		});

		final Button getPictureButton = (Button) findViewById(R.id.uploadButton);

		getPictureButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {

				final Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
				photoPickerIntent.setType("image/*");
				startActivityForResult(photoPickerIntent, SELECT_PHOTO);

			}
		});

		final Button cancelButton = (Button) findViewById(R.id.cancelButton);

		cancelButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {

				// TODO - Set Activity's result with result code RESULT_OK
				// setResult(RESULT_OK, intent);
				// TODO - Finish the Activity
				finish();

			}
		});
	}

	@Override
	protected void onActivityResult(final int requestCode,
			final int resultCode, final Intent imageReturnedIntent) {
		super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

		switch (requestCode) {
		case SELECT_PHOTO:
			if (resultCode == RESULT_OK && pictureCount<4) {
				final Uri selectedImage = imageReturnedIntent.getData();
				//todo :works for one pic, need to work for more: array or comma separeted?
				mImage = selectedImage.toString();

				switch (pictureCount) {
					case 1: {
						mImgOne = (ImageView) findViewById(R.id.picturOne);
						mImgOne.setImageURI(selectedImage);
						pictureCount++;
						break;
					}
					case 2: {
					    mImgTwo = (ImageView) findViewById(R.id.picturTwo);
					    mImgTwo.setImageURI(selectedImage);
					    pictureCount++;
					    break;
					}
				}
			}
		}
	}

	private void sendHttpToServer(final Intent data) {

		final String url = "http://10.0.2.2:8080/2ndHandOz/saveNewAd/";
		final HttpHelper httpHelper = new HttpHelper(data, url, this);
		httpHelper.postData();
	}
}
