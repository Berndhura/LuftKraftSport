package de.wichura.camperapp.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;

public class SendHttpRequestTask extends AsyncTask<String, Void, String> {

	// use Context for getting Bitmap from media
	private final Context mContext;

	public SendHttpRequestTask(final Context context) {
		mContext = context;
	}

	@Override
	protected String doInBackground(final String... params) {
		final String url = params[0];
		final String param1 = params[1];
		final String param2 = params[2]; // bild URI

		final Uri uri = Uri.parse(param2);

		Bitmap bitmap = null;
		try {
			bitmap = MediaStore.Images.Media.getBitmap(
					mContext.getContentResolver(), uri);
		} catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bitmap.compress(CompressFormat.PNG, 0, baos);

		try {
			final HttpClient client = new HttpClient(url);
			client.connectForMultipart();
			client.addFormPart("keywords", param1);
			// client.addFormPart("param2", param2);
			client.addFilePart("image", "icon.png", baos.toByteArray());
			client.finishMultipart();
			final String data = client.getResponse();
		} catch (final Throwable t) {
			t.printStackTrace();
		}

		return null;
	}

	@Override
	protected void onPostExecute(final String data) {
		System.out.println("done");
		// item.setActionView(null);

	}

}
