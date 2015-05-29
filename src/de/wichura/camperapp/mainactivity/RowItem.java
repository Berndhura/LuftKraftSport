package de.wichura.camperapp.mainactivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import com.google.gson.annotations.Expose;

import de.wichura.camperapp.http.HttpClient;

public class RowItem {
	private int imageId;
	@Expose
	private String title;
	@Expose
	private String keywords;
	@Expose
	private String urls;
	@Expose
	private String description;

	private Bitmap image;

	private CustomListViewAdapter adapter;

	public RowItem(final int imageId, final String title,
			final String keywords, final String url, final String des) {
		this.imageId = imageId;
		this.title = title;
		this.keywords = keywords;
		this.urls = url;
		this.description = des;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(final String description) {
		this.description = description;
	}

	public Bitmap getImage() {
		return image;
	}

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
		return urls;
	}

	public void setUrl(final String url) {
		this.urls = url;
	}

	public void setAdapter(final CustomListViewAdapter adp) {
		this.adapter = adp;
	}

	public CustomListViewAdapter getAdapter() {
		return this.adapter;
	}

	@Override
	public String toString() {
		return imageId + "\n" + title + "\n" + keywords + "\n" + urls;
	}

	public void loadImage(final CustomListViewAdapter adapter) {
		// HOLD A REFERENCE TO THE ADAPTER
		this.adapter = adapter;
		if (urls != null && !urls.equals("")) {
			final SendHttpRequestTask task = new SendHttpRequestTask();
			task.execute(urls);
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
			image = BitmapFactory.decodeByteArray(result, 0, result.length);
			adapter.notifyDataSetChanged();
		}
	}
}