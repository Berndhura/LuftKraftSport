package de.wichura.camperapp.http;

import android.content.Context;
import android.content.Intent;
import de.wichura.camperapp.ad.AdItem;

public class HttpHelper {

	private final Context context;
	private final Intent data;
	private final String url;

	public HttpHelper(final Intent data, final String url, final Context context) {
		this.data = data;
		this.url = url;
		this.context = context;
	}

	public void postData() {

		final SendHttpRequestTask sendTask = new SendHttpRequestTask(context);

		final String title = data.getStringExtra(AdItem.TITLE);
		final String description = data.getStringExtra(AdItem.DESC);
		final String keywords = data.getStringExtra(AdItem.KEYWORDS);
		final String picture = data.getStringExtra(AdItem.FILENAME);

		final String[] params = new String[] { url, title, description,
				keywords, picture };
		// anders uebergeben?? reihenfolge der parameter merken ist nicht gut
		// TODO
		sendTask.execute(params);
	}

	public void getAds() {

		final SendHttpRequestTask t = new SendHttpRequestTask(context);
		t.execute(url);
	}

}
