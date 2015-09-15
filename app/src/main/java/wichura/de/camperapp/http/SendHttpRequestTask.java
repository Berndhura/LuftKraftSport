package wichura.de.camperapp.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.util.Log;

import wichura.de.camperapp.bitmap.BitmapHelper;

public class SendHttpRequestTask extends AsyncTask<String, Void, String> {

	// use Context for getting Bitmap from media
	private final Context context;

	public SendHttpRequestTask(final Context context) {
		this.context = context;
	}

	@Override
	protected String doInBackground(final String... params) {

		final String url = params[0];
		// final String param1 = params[1];
		final String bildURI = params[4]; // bild URI

		final Uri uri = Uri.parse(bildURI);
		//TODO: resize Bitmap: path vs. URI funktioniert noch nicht
        BitmapHelper bitmapHelper = new BitmapHelper(context);
        Bitmap thump=bitmapHelper.resize(uri.toString());

		Bitmap bitmap = null;
		try {
			bitmap = MediaStore.Images.Media.getBitmap(
					context.getContentResolver(), uri);
		} catch (final IOException e) {
			Log.i("MyActivity", "MyClass.getView() URLS " +"BITMAP FEHLER");
			e.printStackTrace();
		}

		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        thump.compress(CompressFormat.PNG, 0, baos);
        //was bitmap
		try {
			Log.i("MyActivity", "MyClass.getView() URLS " +"BITMAP URL: "+url);
			final HttpClient client = new HttpClient(url);
			client.connectForMultipart();
			// from HttpHelper: url, title, description, keywords, picture
			client.addFormPart("title", params[1]);
			client.addFormPart("description", params[2]);
			client.addFormPart("keywords", params[3]);
			client.addFilePart("image", "icon.png", baos.toByteArray());
			client.finishMultipart();
			final String data = client.getResponse();
			Log.i("MyActivity", "MyClass.getView() URLS " +"los");
		} catch (final Throwable t) {
			Log.i("MyActivity", "MyClass.getView() URLS " +"BITMAP URLPROB");
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
