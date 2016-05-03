package wichura.de.camperapp.ad;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.List;

import wichura.de.camperapp.R;
import wichura.de.camperapp.http.Urls;
import wichura.de.camperapp.mainactivity.RowItem;

/**
 * Created by Bernd Wichura on 16.03.2016.
 *
 */
public class MyAdsListViewAdapter extends ArrayAdapter<RowItem> {

    private Context context;
    private List<RowItem> items;
    private Activity activity;

    public MyAdsListViewAdapter(final Activity activity, final Context context, final int resourceId,
                                final List<RowItem> items) {
        super(context, resourceId, items);
        this.context = context;
        this.items = items;
        this.activity = activity;
    }

    /* private view holder class */
    private class ViewHolder {
        TextView txtTitle;
        TextView txtPrice;
        TextView txtLocation;
        TextView txtDate;
        TextView txtViews;
        ImageView deleteButton;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final LayoutInflater mInflater = (LayoutInflater) context
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.my_ads_layout, parent, false);

            holder = new ViewHolder();
            holder.txtTitle = (TextView) convertView.findViewById(R.id.my_title);
            holder.txtLocation = (TextView) convertView.findViewById(R.id.my_location);
            holder.deleteButton = (ImageView) convertView.findViewById(R.id.my_ad_delete);
            holder.txtPrice = (TextView) convertView.findViewById(R.id.my_price);
            holder.txtDate = (TextView) convertView.findViewById(R.id.my_date);
            holder.txtViews = (TextView) convertView.findViewById(R.id.my_views);
            convertView.setTag(holder);
        } else
            holder = (ViewHolder) convertView.getTag();

        ImageView thumbNail = (ImageView) convertView.findViewById(R.id.my_icon);

        // getting ad data for the row
        final RowItem rowItem = getItem(position);

        Log.d("CONAN, get pict URLs: ", rowItem.getUrl());

        Picasso.with(context)
                .load(rowItem.getUrl())
                .resize(100, 100)
                .centerCrop()
                .into(thumbNail);

        holder.txtTitle.setText(rowItem.getTitle());
        holder.txtPrice.setText(rowItem.getPrice());
        holder.txtLocation.setText("Melbourne");
        holder.txtDate.setText(DateFormat.getDateInstance().format(rowItem.getDate()));
        holder.txtViews.setText(rowItem.getViews());

        holder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //get ad id and send delete request
                String adId = rowItem.getAdId();
                deleteAdRequest(adId, v);
            }
        });
        holder.deleteButton.setTag(position);


        return convertView;
    }

    private void deleteAdRequest(final String adId, final View view) {
        AlertDialog myQuittingDialogBox = new AlertDialog.Builder(activity)
                //set message, title, and icon
                .setTitle("Delete")
                .setMessage("Do you want to Delete")
                .setIcon(R.drawable.delete)

                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        //your deleting code
                        RequestQueue queue = Volley.newRequestQueue(context);
                        String url = Urls.MAIN_SERVER_URL + Urls.DELETE_AD_WITH_APID + "?adid=" + adId;
                        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                                new Response.Listener<String>() {
                                    @Override
                                    public void onResponse(String response) {
                                        String pos = view.getTag().toString();
                                        int position = Integer.parseInt(pos);
                                        remove(getItem(position));
                                        notifyDataSetChanged();
                                        //TODO: wenn leer, finish()
                                    }
                                }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Toast.makeText(activity, "Something went wrong...", Toast.LENGTH_LONG).show();
                            }
                        });
                        queue.add(stringRequest);
                        dialog.dismiss();
                    }
                })

                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();
        myQuittingDialogBox.show();
    }
}
