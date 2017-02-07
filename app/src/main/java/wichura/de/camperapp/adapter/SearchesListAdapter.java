package wichura.de.camperapp.adapter;

import android.app.Activity;
import android.content.Context;
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
import wichura.de.camperapp.http.Urls;
import wichura.de.camperapp.models.GroupedMsgItem;
import wichura.de.camperapp.models.SearchItem;

/**
 *
 * Created by ich on 07.02.2017.
 */

public class SearchesListAdapter extends ArrayAdapter<SearchItem> {

    private Context context;

    public SearchesListAdapter(final Context context, final int resourceId, final List<SearchItem> items) {
        super(context, resourceId, items);
        this.context = context;
    }

    private class ViewHolder {
        TextView title;
        TextView name;
        TextView date;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final LayoutInflater mInflater = (LayoutInflater) context
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

        SearchesListAdapter.ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.searches_overview_item, parent, false);

            holder = new SearchesListAdapter.ViewHolder();
            holder.title = (TextView) convertView.findViewById(R.id.search_title);
            holder.name = (TextView) convertView.findViewById(R.id.search_distance);
            holder.date = (TextView) convertView.findViewById(R.id.search_price);
            convertView.setTag(holder);
        } else
            holder = (SearchesListAdapter.ViewHolder) convertView.getTag();

        // getting ad data for the row
        final SearchItem searchItem = getItem(position);

        holder.title.setText(searchItem.getDescription());
       // holder.name.setText(searchItem.getDistance());
       // holder.date.setText(DateFormat.getDateInstance().format(searchItem.getDate()));
        return convertView;
    }
}
