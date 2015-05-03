package de.wichura.camperapp.http;

import de.wichura.camperapp.ad.AdItem;
import android.content.Intent;

public class HttpHelper {

	private final Intent data;
	private final String url = "http://10.0.2.2:8080/2ndHandOz/bildundtext";

	public HttpHelper(final Intent data) {
		this.data = data;
	}

	public void postData() {

		final SendHttpRequestTask t = new SendHttpRequestTask();

		final String param1 = data.getStringExtra(AdItem.TITLE);
		final String param2 = data.getStringExtra(AdItem.FILENAME);

		final String[] params = new String[] { url, param1, param2 };
		t.execute(params);
	}

}
