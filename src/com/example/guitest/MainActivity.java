package com.example.guitest;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

//farbcode bilder: #639bc5
public class MainActivity extends ActionBarActivity {

	ListView listView;
	List<RowItem> rowItems;

	public static final String[] titles = new String[] { "Strawberry",
			"Banana", "Orange", "Mixed" };

	public static final String[] descriptions = new String[] {
			"It is an aggregate accessory fruit",
			"It is the largest herbaceous flowering plant", "Citrus Fruit",
			"Mixed Fruits" };

	public static final Integer[] images = { R.drawable.applogo,
			R.drawable.applogo, R.drawable.applogo, R.drawable.applogo };

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Listview:
		// initList!!!
		rowItems = new ArrayList<RowItem>();
		for (int i = 0; i < titles.length; i++) {
			final RowItem item = new RowItem(images[i], titles[i],
					descriptions[i]);
			rowItems.add(item);
		}

		listView = (ListView) findViewById(R.id.list);
		final CustomListViewAdapter adapter = new CustomListViewAdapter(this,
				R.layout.list_item, rowItems);
		listView.setAdapter(adapter);

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
		final int id = item.getItemId();
		if (id == R.id.action_search) {
			Toast.makeText(MainActivity.this, "Maul " + item.toString(),
					Toast.LENGTH_LONG).show();

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

}
