package de.wichura.camperapp.mainactivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.ListView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import de.wichura.camperapp.R;
import de.wichura.camperapp.ad.AdItem;
import de.wichura.camperapp.ad.NewAdActivity;
import de.wichura.camperapp.http.HttpClient;

//farbcode bilder: #639bc5
public class MainActivity extends ActionBarActivity {

	private ListView listView;
	private List<RowItem> rowItems;
	private ImageView imgView;
	private JSONObject j;

	// public static final byte[] imagesDb =
	Set<byte[]> imagesDb = new TreeSet<byte[]>();
	// Bitmap bmp = BitmapFactory.decodeByteArray(blob, 0, blob.length);

	public static final String[] titles = new String[] { "Strawberry",
			"Banana", "Orange", "Mixed", "Mixed" };

	public static final String[] descriptions = new String[] {
			"It is an aggregate accessory fruit",
			"It is the largest herbaceous flowering plant", "Citrus Fruit",
			"Mixed Fruits", "Citrus Fruit" };

	public static final Integer[] images = { R.drawable.applogo,
			R.drawable.applogo, R.drawable.applogo, R.drawable.applogo,
			R.drawable.applogo };

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// TODO: hole im ersten schritt alle einträge aus der datenbank bzw vom
		// webserver
		// coursor?von android?
		// Listview:
		// initList!!!

		//ein bild holen für den startbildschirm
		//getListWithAds();

		// download JSON formated zeug vom Server
		final JSONObject jsonobject;
		// jsonobject = JSONfunctions
		// .getJSONfromURL("http://localhost:8080/2ndHandOz/getAllAds");

		imgView = (ImageView) findViewById(R.id.imgView1);
		// test http

		// get MOCK JSON
		try {
			j = getJson();
			System.out.println(j.toString());
		} catch (final JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		JSONObject responseObj = null;

		try {

			final Gson gson = new GsonBuilder()
					.excludeFieldsWithoutExposeAnnotation().create();
			responseObj = new JSONObject(j.toString());
			final JSONArray titleListObj = responseObj.getJSONArray("zeug");

			rowItems = new ArrayList<RowItem>();
			for (int i = 0; i < titleListObj.length(); i++) {

				// get the country information JSON object
				final String title = titleListObj.getJSONObject(i).toString();
				// create java object from the JSON object, matscht alles in die
				// RowItem class!geter seter...
				final RowItem country = gson.fromJson(title, RowItem.class);
				// add to country array list
				// countryList.add(country);
				rowItems.add(country);
				System.out.println("maul: " + title);
			}
		} catch (final JSONException e) {
			e.printStackTrace();
		}

		/*
		 * rowItems = new ArrayList<RowItem>(); for (int i = 0; i <
		 * titles.length; i++) { final RowItem item = new RowItem(images[i],
		 * titles[i], descriptions[i], "urlDUMMY"); // TODO image einfügen
		 * rowItems.add(item); }
		 */

		listView = (ListView) findViewById(R.id.list);
		final CustomListViewAdapter adapter = new CustomListViewAdapter(this,
				R.layout.list_item, rowItems);
		listView.setAdapter(adapter);

		// neu gemacht: load alle bilder zu den URLs async
		for (final RowItem ri : rowItems) {
			ri.loadImage(adapter);
		}

	}

	private JSONObject getJson() throws JSONException {

		final JSONObject jo1 = new JSONObject();
		jo1.put("title", "Kocher");
		jo1.put("keywords", "kocher");
		jo1.put("url", "http://10.0.2.2:8080/2ndHandOz/getBild?id=0");

		final JSONObject jo2 = new JSONObject();
		jo2.put("title", "Zelt");
		jo2.put("keywords", "zelt");
		jo2.put("url", "http://10.0.2.2:8080/2ndHandOz/getBild?id=1");

		final JSONArray ja = new JSONArray();
		ja.put(jo1);
		ja.put(jo2);

		final JSONObject mainObj = new JSONObject();
		mainObj.put("zeug", ja);

		return mainObj;

	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.

		// Toast hier sinnlos, nur zur demo, deshalb raus damit
		final int id = item.getItemId();
		if (id == R.id.action_search) {
			// Toast.makeText(MainActivity.this, "Maul " + item.toString(),
			// Toast.LENGTH_LONG).show();
			// aufruf der New Ad Seite
			final Intent intent = new Intent(this, NewAdActivity.class);
			// intent daten neuer ad, siehe todo App weil statuscode noch unklar
			startActivityForResult(intent, 1);

			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	// daten der neuen Ad in aus Item
	@Override
	protected void onActivityResult(final int requestCode,
			final int resultCode, final Intent data) {

		{
			final AdItem adItem = new AdItem(data);
			System.out.println(adItem.toString());

			// adapter für listview daten aus dem adItem werden hinzugefügt:
			// daten, bild?!
			// mAdapter.add(newItem);
		}
	}

	private void getListWithAds() {
		final String url = "http://10.0.2.2:8080/2ndHandOz/getBild?id=0";
		final SendHttpRequestTask task = new SendHttpRequestTask();
		task.execute(url);
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
			final Bitmap img = BitmapFactory.decodeByteArray(result, 0,
					result.length);

			// Hintergrundbild setzen, später nur Listview zu sehen oder
			// transparent
			imgView.setImageBitmap(img);
			// item.setActionView(null);
		}
	}
}