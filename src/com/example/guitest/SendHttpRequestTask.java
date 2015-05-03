package com.example.guitest;

import java.io.ByteArrayOutputStream;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.MenuItem;

public class SendHttpRequestTask extends AsyncTask<String, Void, String> {

	private MenuItem item;

	@Override
	protected String doInBackground(final String... params) {
		final String url = params[0];
		final String param1 = params[1];
		final String param2 = params[2];

		// altes bild (b) brauchen wir hier nicht...
		// b.compress(CompressFormat.PNG, 0, baos);
		final Bitmap b = BitmapFactory.decodeFile(param2);
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		b.compress(CompressFormat.JPEG, 0, baos);

		try {
			final HttpClient client = new HttpClient(url);
			client.connectForMultipart();
			client.addFormPart("titel", param1);
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
