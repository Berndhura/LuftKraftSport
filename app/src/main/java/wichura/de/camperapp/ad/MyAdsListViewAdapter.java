package wichura.de.camperapp.ad;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
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

import java.util.List;

import wichura.de.camperapp.R;
import wichura.de.camperapp.http.Urls;
import wichura.de.camperapp.mainactivity.RowItem;

/**
 * Created by Bernd Wichura on 16.03.2016.
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
        TextView txtDesc;
        TextView txtPrice;
        ImageButton deleteButton;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final LayoutInflater mInflater = (LayoutInflater) context
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.my_ads_layout, null);
        }
        holder = new ViewHolder();
        holder.txtDesc = (TextView) convertView.findViewById(R.id.desc);
        holder.txtTitle = (TextView) convertView.findViewById(R.id.my_title);
        holder.deleteButton = (ImageButton) convertView.findViewById(R.id.my_ad_delete);
        //holder.txtPrice = (TextView) convertView.findViewById(R.id.price);
        convertView.setTag(holder);
        //  } else
        //     holder = (ViewHolder) convertView.getTag();

        ImageView thumbNail = (ImageView) convertView.findViewById(R.id.my_icon);

        // getting ad data for the row
        final RowItem rowItem = getItem(position);

        Log.d("CONAN, get pict URLs: ", rowItem.getUrl());
        //--------------------

        Picasso.with(context)
                .load(rowItem.getUrl())
                .resize(100, 100)
                .centerCrop()
                .into(thumbNail);

        //____________________


        //set Keywords
        // holder.txtDesc.setText(rowItem.getKeywords());
        //set Title
        holder.txtTitle.setText(rowItem.getTitle());
        //set Price
       // holder.txtPrice.setText("99");

        holder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //get ad id and send delete request
                // String adId = getIntent().getStringExtra("id");
                String adId = rowItem.getAdId();
                deleteAdRequest(adId, v);
            }
        });
        holder.deleteButton.setTag(position);


        return convertView;
    }

    private void deleteAdRequest(String adId, final View view) {
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(context);
        String url = Urls.MAIN_SERVER_URL + Urls.DELETE_AD_WITH_APID + "?adid=" + adId;

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        items.remove((Integer) view.getTag());
                        ((MyAdsActivity) activity).refreshList();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(activity, "Something went wrong...", Toast.LENGTH_LONG).show();
            }
        });
        queue.add(stringRequest);
    }
}
