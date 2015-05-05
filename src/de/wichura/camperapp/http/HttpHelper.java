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

		final SendHttpRequestTask t = new SendHttpRequestTask(context);

		final String param1 = data.getStringExtra(AdItem.TITLE);
		final String param2 = data.getStringExtra(AdItem.FILENAME);

		final String[] params = new String[] { url, param1, param2 };
		t.execute(params);
	}

	public void getAds() {

		final SendHttpRequestTask t = new SendHttpRequestTask(context);
		t.execute(url);
	}

}
