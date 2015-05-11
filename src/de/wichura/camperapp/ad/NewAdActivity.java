package de.wichura.camperapp.ad;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import de.wichura.camperapp.R;
import de.wichura.camperapp.http.HttpHelper;

public class NewAdActivity extends Activity {

	private EditText mTitleText;
	private EditText mDescText;
	private EditText mKeywords;

	private static final int SELECT_PHOTO = 100;
	private String mImage;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.new_ad_acivity);

		mTitleText = (EditText) findViewById(R.id.title);
		mDescText = (EditText) findViewById(R.id.description);
		mKeywords = (EditText) findViewById(R.id.keywords);

		final Button submitButton = (Button) findViewById(R.id.submitButton);
		submitButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {
				// TODO - Title
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

		final ImageButton uploadPicButton = (ImageButton) findViewById(R.id.uploadButton);

		uploadPicButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {

				final Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
				photoPickerIntent.setType("image/*");
				startActivityForResult(photoPickerIntent, SELECT_PHOTO);

			}
		});
	}

	@Override
	protected void onActivityResult(final int requestCode,
			final int resultCode, final Intent imageReturnedIntent) {
		super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

		switch (requestCode) {
		case SELECT_PHOTO:
			if (resultCode == RESULT_OK) {
				final Uri selectedImage = imageReturnedIntent.getData();
				mImage = selectedImage.toString();
				// content://media/external/images/media/18
				/*
				 * try { imageStream = getContentResolver().openInputStream(
				 * selectedImage); } catch (final FileNotFoundException e) { //
				 * TODO Auto-generated catch block e.printStackTrace(); } final
				 * Bitmap yourSelectedImage = BitmapFactory
				 * .decodeStream(imageStream);
				 */
				// ToDo:
				// show bitmap -> im xml fertig

				// image = (ImageView) findViewById(R.id.imageView1);
				// image.setImageResource(resId)
			}
		}
	}

	private void sendHttpToServer(final Intent data) {

		final String url = "http://10.0.2.2:8080/2ndHandOz/bildundtext";
		final HttpHelper httpHelper = new HttpHelper(data, url, this);
		httpHelper.postData();
	}
}
