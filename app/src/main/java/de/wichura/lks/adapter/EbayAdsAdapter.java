package de.wichura.lks.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import de.wichura.lks.R;
import de.wichura.lks.models.EbayAd;
import de.wichura.lks.util.Utility;

/**
 * Created by Bernd Wichura on 11.06.2017.
 * Luftkraftsport
 */

public class EbayAdsAdapter extends ArrayAdapter<EbayAd> {

    private Context context;

    public EbayAdsAdapter(final Context context, final int resourceId, final List<EbayAd> items) {
        super(context, resourceId, items);
        this.context = context;
    }

    private static class ViewHolder {
        TextView title;
        TextView location;
        TextView price;
        TextView timeLeft;
        ImageView thumbNail;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final LayoutInflater mInflater = (LayoutInflater) context
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

        EbayAdsAdapter.ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.ebay_row_item, parent, false);

            holder = new EbayAdsAdapter.ViewHolder();
            holder.title = (TextView) convertView.findViewById(R.id.ebay_title);
            holder.location = (TextView) convertView.findViewById(R.id.ebay_location);
            holder.price = (TextView) convertView.findViewById(R.id.ebay_price);
            holder.timeLeft = (TextView) convertView.findViewById(R.id.ebay_time_left);
            convertView.setTag(holder);
        } else
            holder = (EbayAdsAdapter.ViewHolder) convertView.getTag();

        // getting ad data for the row
        final EbayAd rowItem = getItem(position);

        holder.thumbNail = (ImageView) convertView.findViewById(R.id.ebay_thumbnail);
        Picasso.with(context).load(rowItem.getThumbNailUrl()).into(holder.thumbNail);

        holder.title.setText(rowItem.getTitle());
        holder.location.setText(rowItem.getLocation());
        holder.timeLeft.setText("noch: " + rowItem.getTimeLeft());
        holder.price.setText(Utility.getPriceString(rowItem.getPrice()));

        return convertView;
    }
}
