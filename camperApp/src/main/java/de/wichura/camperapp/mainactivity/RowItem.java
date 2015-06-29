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

	@Override
	public String toString() {
		return imageId + "\n" + title + "\n" + keywords + "\n" + urls;
	}
}