package de.wichura.camperapp.mainactivity;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;

import de.wichura.camperapp.R;
import de.wichura.camperapp.app.AppController;

public class CustomListViewAdapter extends ArrayAdapter<RowItem> {

	Context context;
	ImageLoader imageLoader = AppController.getInstance().getImageLoader();

	public CustomListViewAdapter(final Context context, final int resourceId,
			final List<RowItem> items) {
		super(context, resourceId, items);
		this.context = context;
	}

	/* private view holder class */
	private class ViewHolder {
		ImageView imageView;
		TextView txtTitle;
		TextView txtDesc;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		ViewHolder holder = null;

		final LayoutInflater mInflater = (LayoutInflater) context
				.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.list_item, null);
			holder = new ViewHolder();
			holder.txtDesc = (TextView) convertView.findViewById(R.id.desc);
			holder.txtTitle = (TextView) convertView.findViewById(R.id.title);
			//use Volley!
			//holder.imageView = (ImageView) convertView.findViewById(R.id.icon);
			convertView.setTag(holder);
		} else
			holder = (ViewHolder) convertView.getTag();

        if (imageLoader == null)
            imageLoader = AppController.getInstance().getImageLoader();
        NetworkImageView thumbNail = (NetworkImageView) convertView
                .findViewById(R.id.icon);

        // getting ad data for the row
        final RowItem rowItem = getItem(position);
        //set image
        thumbNail.setImageUrl(rowItem.getUrl(), imageLoader);
		Log.i("IM_URLS: ",  rowItem.getUrl().toString());
        //set Keywords
		holder.txtDesc.setText(rowItem.getKeywords());
		//set Title
        holder.txtTitle.setText(rowItem.getTitle());
		//use Volley!
        //holder.imageView.setImageBitmap(rowItem.getImage());

		return convertView;
	}
}