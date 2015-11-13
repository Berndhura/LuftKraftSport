package wichura.de.camperapp.http;

import android.os.Environment;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;

public class HttpClient {
	private static final int PICTURESIZE = 3 * 1024 * 1024;
	private final String url;
	private HttpURLConnection con;
	private OutputStream os;

	private final String delimiter = "--";
	private final String boundary = "SwA"
			+ Long.toString(System.currentTimeMillis()) + "SwA";

	public HttpClient(final String url) {
		this.url = url;
	}

	// take out parameter: final String imgName
	public byte[] downloadImage() {
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			// System.out.println("URL [" + url + "] - Name [" + imgName + "]");

			final HttpURLConnection con = (HttpURLConnection) (new URL(url))
					.openConnection();
			con.setRequestMethod("POST");
			con.setDoInput(true);
			con.setDoOutput(true);
			con.connect();
			// con.getOutputStream().write(("name=" + imgName).getBytes());

			final InputStream is = con.getInputStream();
			// TODO groesse der pics!!
			final byte[] b = new byte[PICTURESIZE];

			while (is.read(b) != -1)
				baos.write(b);

			con.disconnect();
		} catch (final Throwable t) {
			t.printStackTrace();
		}

		return baos.toByteArray();
	}

	public void connectForMultipart() throws Exception {
		con = (HttpURLConnection) (new URL(url)).openConnection(Proxy.NO_PROXY);
		Log.d("FART:", con.toString());
				con.setRequestMethod("POST");
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
		String dir = Environment.getExternalStorageDirectory().getAbsolutePath();
		Log.d("_______________DIR: ", dir.toString());
		final InputStream is = con.getInputStream();
		final byte[] b1 = new byte[PICTURESIZE];
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
