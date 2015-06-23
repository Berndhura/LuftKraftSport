package de.wichura.camperapp.mainactivity;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;


import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import de.wichura.camperapp.R;
import de.wichura.camperapp.ad.NewAdActivity;
import de.wichura.camperapp.ad.OpenAdActivity;
import de.wichura.camperapp.http.HttpClient;
import de.wichura.camperapp.http.JSONParser;

//farbcode bilder: #639bc5
public class MainActivity extends ActionBarActivity {

	private ListView listView;
	private List<RowItem> rowItems;
	private ImageView imgView;

	//static String WEBURL = "http://10.0.2.2:8080/2ndHandOz";
	static String WEBURL = "http://192.168.2.105:8080/2ndHandOz";

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		imgView = (ImageView) findViewById(R.id.imgView1);

        //neuer Volley Ansatz fuer JSON
		RequestQueue queue = Volley.newRequestQueue(this);

        JsonArrayRequest jsObjRequest = new JsonArrayRequest(
                Request.Method.GET, WEBURL+"/getAllAds", null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                //txtDisplay.setText("Response => " + response.toString());
                Log.i("MyActivity", "MyClass.getView() — JSON BERND2 " + response.toString());
                //findViewById(R.id.progressBar1).setVisibility(View.GONE);
                Context context=getApplicationContext();
				try {

					final Gson gson = new GsonBuilder()
							.excludeFieldsWithoutExposeAnnotation().create();

					final JSONArray listOfAllAds = new JSONArray(response.toString());

					rowItems = new ArrayList<RowItem>();
					for (int i = 0; i < listOfAllAds.length(); i++) {

						// get the titel information JSON object
						final String title = listOfAllAds.getJSONObject(i)
								.toString();
						// create java object from the JSON object, matscht alles in
						// die
						// RowItem class!geter seter...
						final RowItem rowItem = gson.fromJson(title, RowItem.class);
						rowItems.add(rowItem);
					}
				} catch (final JSONException e) {
					e.printStackTrace();
				}

				listView = (ListView) findViewById(R.id.list);
				final CustomListViewAdapter adapter = new CustomListViewAdapter(
						context, R.layout.list_item, rowItems);
				listView.setAdapter(adapter);

				listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

					@Override
					public void onItemClick(final AdapterView<?> arg0,
											final View arg1, final int position, final long arg3) {
						final Object o = listView.getItemAtPosition(position);
						final RowItem str = (RowItem) o;// As you are using Default
						// String Adapter
						// starte neuen intent mit sel. rowitem
						final Intent intent = new Intent(getApplicationContext(),
								OpenAdActivity.class);
						startActivityForResult(intent, 1);

						Toast.makeText(getApplicationContext(), str.getTitle(),
								Toast.LENGTH_SHORT).show();
					}
				});

				// neu gemacht: load alle bilder zu den URLs async
				for (final RowItem ri : rowItems) {
					ri.loadImage(adapter);
				}

			}
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // TODO Auto-generated method stub
                Log.i("MyActivity", "MyClass.getView() — JSON ERROR " + error.toString());
            }
        });
        queue.add(jsObjRequest);
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
		// get Ads in JSON -> alt, umstellen auf volley
		//new JSONParse(this).execute();
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
			imgView.setImageBitmap(img);
		}
	}
}