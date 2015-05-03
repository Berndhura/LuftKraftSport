package de.wichura.camperapp.mainactivity;

/*
 * Copyright (C) 2013 Surviving with Android (http://www.survivingwithandroid.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpClient {
	private final String url;
	private HttpURLConnection con;
	private OutputStream os;

	private final String delimiter = "--";
	private final String boundary = "SwA"
			+ Long.toString(System.currentTimeMillis()) + "SwA";

	public HttpClient(final String url) {
		this.url = url;
	}

	public byte[] downloadImage(final String imgName) {
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			System.out.println("URL [" + url + "] - Name [" + imgName + "]");

			final HttpURLConnection con = (HttpURLConnection) (new URL(url))
					.openConnection();
			con.setRequestMethod("POST");
			con.setDoInput(true);
			con.setDoOutput(true);
			con.connect();
			con.getOutputStream().write(("name=" + imgName).getBytes());

			final InputStream is = con.getInputStream();
			final byte[] b = new byte[1024];

			while (is.read(b) != -1)
				baos.write(b);

			con.disconnect();
		} catch (final Throwable t) {
			t.printStackTrace();
		}

		return baos.toByteArray();
	}

	public void connectForMultipart() throws Exception {
		con = (HttpURLConnection) (new URL(url)).openConnection();
		// con.setRequestMethod("POST");
		con.setDoInput(true);
		con.setDoOutput(true);
		con.setRequestProperty("Connection", "Keep-Alive");
		con.setRequestProperty("Content-Type", "multipart/form-data; boundary="
				+ boundary);
		con.connect();
		os = con.getOutputStream();
	}

	public void addFormPart(final String paramName, final String value)
			throws Exception {
		writeParamData(paramName, value);
	}

	public void addFilePart(final String paramName, final String fileName,
			final byte[] data) throws Exception {
		os.write((delimiter + boundary + "\r\n").getBytes());
		os.write(("Content-Disposition: form-data; name=\"" + paramName
				+ "\"; filename=\"" + fileName + "\"\r\n").getBytes());
		os.write(("Content-Type: application/octet-stream\r\n").getBytes());
		os.write(("Content-Transfer-Encoding: binary\r\n").getBytes());
		os.write("\r\n".getBytes());

		os.write(data);

		os.write("\r\n".getBytes());
	}

	public void finishMultipart() throws Exception {
		os.write((delimiter + boundary + delimiter + "\r\n").getBytes());
	}

	public String getResponse() throws Exception {
		final InputStream is = con.getInputStream();
		final byte[] b1 = new byte[1024];
		final StringBuffer buffer = new StringBuffer();

		while (is.read(b1) != -1)
			buffer.append(new String(b1));

		con.disconnect();

		return buffer.toString();
	}

	private void writeParamData(final String paramName, final String value)
			throws Exception {

		os.write((delimiter + boundary + "\r\n").getBytes());
		os.write("Content-Type: text/plain\r\n".getBytes());
		os.write(("Content-Disposition: form-data; name=\"" + paramName + "\"\r\n")
				.getBytes());
		;
		os.write(("\r\n" + value + "\r\n").getBytes());

	}
}
