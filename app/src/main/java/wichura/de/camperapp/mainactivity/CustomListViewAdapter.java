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

import java.text.DateFormat;
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
        TextView txtDate;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final LayoutInflater mInflater = (LayoutInflater) context
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.list_item, parent, false);
            holder = new ViewHolder();
            holder.txtTitle = (TextView) convertView.findViewById(R.id.title);
            holder.txtPrice = (TextView) convertView.findViewById(R.id.price);
            holder.txtDate = (TextView) convertView.findViewById(R.id.creation_date);
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
        holder.txtTitle.setText(rowItem.getTitle());
        holder.txtPrice.setText(rowItem.getPrice());
        holder.txtDate.setText(DateFormat.getDateInstance().format(rowItem.getDate()));
        return convertView;
    }
}