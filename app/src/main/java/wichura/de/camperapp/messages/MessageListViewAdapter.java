package wichura.de.camperapp.messages;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.List;

import wichura.de.camperapp.R;
import wichura.de.camperapp.mainactivity.Constants;

import static wichura.de.camperapp.mainactivity.Constants.SHARED_PREFS_USER_INFO;

/**
 * Created by ich on 31.05.2016.
 * Camper App
 */
public class MessageListViewAdapter extends ArrayAdapter<MsgRowItem> {

    private Context context;

    public MessageListViewAdapter(final Context context, final int resourceId, final List<MsgRowItem> items) {
        super(context, resourceId, items);
        this.context = context;
    }

    private class ViewHolder {
        TextView message;
        TextView date;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final LayoutInflater mInflater = (LayoutInflater) context
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

        MsgRowItem item = getItem(position);

        ViewHolder holder;

        String userId = getUserId();

       // Log.d("CONAN", "sender: " + item.getSender());
       // Log.d("CONAN", "userId: " + userId);
       // Log.d("CONAN", "message: " + item.getMessage());

        if (item.getSender() != null) {
            if (item.getSender().equals(userId)) {
                //sender -> ich
                convertView = mInflater.inflate(R.layout.chat_item_sent, null);
            } else {
                convertView = mInflater.inflate(R.layout.chat_item_rcv, null);
            }
        } else {
            //Sender null -> you just added an answer to the list -> sent
            convertView = mInflater.inflate(R.layout.chat_item_sent, null);
        }


        holder = new ViewHolder();
        holder.message = (TextView) convertView.findViewById(R.id.message);
        holder.date = (TextView) convertView.findViewById(R.id.message_date);
        convertView.setTag(holder);

        // getting ad data for the row
        final MsgRowItem rowItem = getItem(position);

        holder.message.setText(rowItem.getMessage());
        holder.date.setText(DateFormat.getDateTimeInstance().format(rowItem.getDate()));
        return convertView;
    }

    private String getUserId() {
        return context.getSharedPreferences(SHARED_PREFS_USER_INFO, 0).getString(Constants.USER_ID, "");
    }
}
