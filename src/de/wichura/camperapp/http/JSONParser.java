package de.wichura.camperapp.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import android.util.Log;

public class JSONParser {

	static InputStream is = null;
	static JSONObject jObj = null;
	static String json = "";

	// constructor
	public JSONParser() {

	}

	public String getJSONFromUrl(final String url) {

		// Making HTTP request
		try {
			// defaultHttpClient
			final DefaultHttpClient httpClient = new DefaultHttpClient();
			final HttpPost httpPost = new HttpPost(url);

			final HttpResponse httpResponse = httpClient.execute(httpPost);
			final HttpEntity httpEntity = httpResponse.getEntity();
			is = httpEntity.getContent();

		} catch (final UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (final ClientProtocolException e) {
			e.printStackTrace();
		} catch (final IOException e) {
			e.printStackTrace();
		}

		try {
			final BufferedReader reader = new BufferedReader(
					new InputStreamReader(is, "iso-8859-1"), 8);
			final StringBuilder sb = new StringBuilder();
			String line = null;
			while ((line = reader.readLine()) != null) {
				sb.append(line + "n");
			}
			is.close();
			json = sb.toString();
		} catch (final Exception e) {
			Log.e("Buffer Error", "Error converting result " + e.toString());
		}

		// return JSON String
		return json;

	}
}
