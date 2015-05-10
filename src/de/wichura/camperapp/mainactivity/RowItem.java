package de.wichura.camperapp.mainactivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import de.wichura.camperapp.http.HttpClient;

public class RowItem {
	private int imageId;
	private String title;
	private String keywords;
	private String url;
	private Bitmap image;
	private CustomListViewAdapter adapter;// EXCLUDE because GSON!! how???

	// private byte[] image;

	public RowItem(final int imageId, final String title,
			final String keywords, final String url) {
		this.imageId = imageId;
		this.title = title;
		this.keywords = keywords;
		this.url = url;
	}

	public Bitmap getImage() {
		return image;
	}

	/*
	 * public byte[] getImage() { return image; }
	 */

	/*
	 * public void setImage(final byte[] image) { this.image = image; }
	 */

	public int getImageId() {
		return imageId;
	}

	public void setImageId(final int imageId) {
		this.imageId = imageId;
	}

	public String getKeywords() {
		return keywords;
	}

	public void setKeywords(final String keyw) {
		this.keywords = keyw;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(final String title) {
		this.title = title;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(final String url) {
		this.url = url;
	}

	public void setAdapter(final CustomListViewAdapter adp) {
		this.adapter = adp;
	}

	public CustomListViewAdapter getAdapter() {
		return this.adapter;
	}

	@Override
	public String toString() {
		return imageId + "\n" + title + "\n" + keywords + "\n" + url;
	}

	public void loadImage(final CustomListViewAdapter adapter) {
		// HOLD A REFERENCE TO THE ADAPTER
		this.adapter = adapter;
		if (url != null && !url.equals("")) {
			new SendHttpRequestTask().execute(url);
		}
	}

	private class SendHttpRequestTask extends AsyncTask<String, Void, byte[]> {

		@Override
		protected byte[] doInBackground(final String... params) {
			final String url = params[0];
			// final String name = params[1];

			final HttpClient client = new HttpClient(url);
			final byte[] data = client.downloadImage();// name

			return data;
		}

		@Override
		protected void onPostExecute(final byte[] result) {
			if (result != null) {
				// Log.i("ImageLoadTask", "Successfully loaded " + name +
				// " image");
				image = BitmapFactory.decodeByteArray(result, 0, result.length);
				if (adapter != null) {
					// WHEN IMAGE IS LOADED NOTIFY THE ADAPTER
					adapter.notifyDataSetChanged();
				}
			} else {
				// TODO:log falls bild nicht geladen
				// Log.e("ImageLoadTask", "Failed to load " + name + " image");
			}
		}
	}
}