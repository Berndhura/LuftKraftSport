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

/**
 * Created by ich on 31.05.2016.
 *
 */
public class MessageListViewAdapter extends ArrayAdapter<MsgRowItem> {

    private Context context;

    public MessageListViewAdapter(final Context context, final int resourceId, final List<MsgRowItem> items) {
        super(context, resourceId, items);
        this.context = context;
    }

    private class ViewHolder {
        TextView message;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final LayoutInflater mInflater = (LayoutInflater) context
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.message_list_item, parent, false);
            holder = new ViewHolder();
            holder.message = (TextView) convertView.findViewById(R.id.message);
            convertView.setTag(holder);
        } else
            holder = (ViewHolder) convertView.getTag();

        // getting ad data for the row
        final MsgRowItem rowItem = getItem(position);

        holder.message.setText(rowItem.getMessage());
        // holder.txtDate.setText(DateFormat.getDateInstance().format(rowItem.getDate()));
        return convertView;
    }
}
