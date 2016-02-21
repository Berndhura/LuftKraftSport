package wichura.de.camperapp.mainactivity;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;

import java.util.List;

import wichura.de.camperapp.R;
import wichura.de.camperapp.app.AppController;

public class CustomListViewAdapter extends ArrayAdapter<RowItem> {

    private Context context;
    private ImageLoader imageLoader = AppController.getInstance().getImageLoader();

    public CustomListViewAdapter(final Context context, final int resourceId,
                                 final List<RowItem> items) {
        super(context, resourceId, items);
        this.context = context;
    }

    /* private view holder class */
    private class ViewHolder {
        TextView txtTitle;
        TextView txtDesc;
        TextView txtPrice;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final LayoutInflater mInflater = (LayoutInflater) context
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.list_item, null);
            holder = new ViewHolder();
            holder.txtDesc = (TextView) convertView.findViewById(R.id.desc);
            holder.txtTitle = (TextView) convertView.findViewById(R.id.title);
            holder.txtPrice = (TextView) convertView.findViewById(R.id.price);
            convertView.setTag(holder);
        } else
            holder = (ViewHolder) convertView.getTag();

        if (imageLoader == null)
            imageLoader = AppController.getInstance().getImageLoader();

        //From Volley, also use this in layout xml!
        NetworkImageView thumbNail = (NetworkImageView) convertView
                .findViewById(R.id.icon);

        // getting ad data for the row
        final RowItem rowItem = getItem(position);
        //set image
        thumbNail.setImageUrl(rowItem.getUrl(), imageLoader);
        Log.i("IM_URLS: ", rowItem.getUrl());
        //set Keywords
        holder.txtDesc.setText(rowItem.getKeywords());
        //set Title
        holder.txtTitle.setText(rowItem.getTitle());
        //set Price
        holder.txtPrice.setText("99");

        return convertView;
    }
}