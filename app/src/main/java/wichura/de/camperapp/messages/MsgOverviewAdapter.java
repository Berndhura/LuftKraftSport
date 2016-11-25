package wichura.de.camperapp.messages;

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
import wichura.de.camperapp.mainactivity.Constants;

/**
 * Created by ich on 20.06.2016.
 * CamperApp
 */
public class MsgOverviewAdapter extends ArrayAdapter<GroupedMsgItem> {

    private Context context;

    public MsgOverviewAdapter(final Context context, final int resourceId, final List<GroupedMsgItem> items) {
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

        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.msg_overview_item, parent, false);

            holder = new ViewHolder();
            holder.title = (TextView) convertView.findViewById(R.id.ad_title);
            holder.name = (TextView) convertView.findViewById(R.id.user_name);
            holder.date = (TextView) convertView.findViewById(R.id.msg_date);
            convertView.setTag(holder);
        } else
            holder = (ViewHolder) convertView.getTag();

        // getting ad data for the row
        final GroupedMsgItem rowItem = getItem(position);

        ImageView thumbNail = (ImageView) convertView.findViewById(R.id.ad_image);
        Picasso.with(context).load(Urls.MAIN_SERVER_URL + Urls.GET_PICTURE_THUMB + rowItem.getUrl()).into(thumbNail);

        holder.title.setText(rowItem.getMessage());
        holder.name.setText(rowItem.getName());
        holder.date.setText(DateFormat.getDateInstance().format(rowItem.getDate()));
        return convertView;
    }
}
