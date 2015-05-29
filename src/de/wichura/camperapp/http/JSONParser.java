package de.wichura.camperapp.http;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

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

		try {
			// Making HTTP request
			final HttpURLConnection con = (HttpURLConnection) (new URL(url))
					.openConnection();
			// con.setRequestMethod("GET");
			con.setRequestProperty("Accept", "application/json");
			con.setConnectTimeout(1000);
			con.setUseCaches(false);
			con.setRequestProperty("Connection", "close");
			// con.connect();
			// con.getOutputStream().write(("name=" + imgName).getBytes());

			final InputStream is = con.getInputStream();

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
			con.disconnect();

		} catch (final Throwable t) {
			t.printStackTrace();
		}

		// return JSON String
		return json;

	}
}
