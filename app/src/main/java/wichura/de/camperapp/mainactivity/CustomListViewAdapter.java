package wichura.de.camperapp.mainactivity;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import wichura.de.camperapp.R;

public class CustomListViewAdapter extends ArrayAdapter<RowItem> {

    private Context context;

    public CustomListViewAdapter(final Context context, final int resourceId, final List<RowItem> items) {
        super(context, resourceId, items);
        this.context = context;
    }

    /* private view holder class */
    private class ViewHolder {
        TextView txtTitle;
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
            holder.txtTitle = (TextView) convertView.findViewById(R.id.title);
            holder.txtPrice = (TextView) convertView.findViewById(R.id.price);
            convertView.setTag(holder);
        } else
            holder = (ViewHolder) convertView.getTag();

        ImageView thumbNail = (ImageView) convertView.findViewById(R.id.icon);

        // getting ad data for the row
        final RowItem rowItem = getItem(position);

        Picasso.with(context)
                .load(rowItem.getUrl())
                .resize(100, 100)
                .centerCrop()
                .into(thumbNail);

        Log.d("CONAN, get pic URLs: ", rowItem.getUrl());
        //set Title
        holder.txtTitle.setText(rowItem.getTitle());
        //set Price
        holder.txtPrice.setText("123 €");
        return convertView;
    }
}