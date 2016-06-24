package wichura.de.camperapp.messages;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import wichura.de.camperapp.R;
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
            holder.name = (TextView) convertView.findViewById(R.id.ad_name);
            convertView.setTag(holder);
        } else
            holder = (ViewHolder) convertView.getTag();

        // getting ad data for the row
        final GroupedMsgItem rowItem = getItem(position);

        holder.title.setText(rowItem.getAdTitle());
        holder.name.setText(rowItem.getName());
        return convertView;
    }

    private String getUserId() {
        return context.getSharedPreferences("UserInfo", 0).getString(Constants.USER_ID, "");
    }
}