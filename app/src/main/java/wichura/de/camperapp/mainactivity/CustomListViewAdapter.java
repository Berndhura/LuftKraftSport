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
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Arrays;
import java.util.List;

import wichura.de.camperapp.R;
import wichura.de.camperapp.http.Urls;
import wichura.de.camperapp.models.RowItem;

public class CustomListViewAdapter extends ArrayAdapter<RowItem> {

    private Context context;
    private ViewHolder holder;
    private String[] bookmarks;

    public CustomListViewAdapter(final Context context, final int resourceId, final List<RowItem> items, final String bookmarks) {
        super(context, resourceId, items);
        this.context = context;
        this.bookmarks = bookmarks.split(",");
    }

    /* private view holder class */
    private class ViewHolder {
        TextView txtTitle;
        TextView txtPrice;
        TextView txtDate;
        ImageView bookmarkStar;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        final LayoutInflater mInflater = (LayoutInflater) context
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.list_item, parent, false);
            holder = new ViewHolder();
            holder.txtTitle = (TextView) convertView.findViewById(R.id.title);
            holder.txtPrice = (TextView) convertView.findViewById(R.id.price);
            holder.txtDate = (TextView) convertView.findViewById(R.id.creation_date);
            holder.bookmarkStar = (ImageView) convertView.findViewById(R.id.bookmark_star);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        ImageView thumbNail = (ImageView) convertView.findViewById(R.id.icon);

        // getting ad data for the row
        final RowItem rowItem = getItem(position);

        Picasso.with(context)
                .load(rowItem.getUrl())
                .resize(100, 100)
                .centerCrop()
                .into(thumbNail);

        // Log.d("CONAN, get pic URLs: ", rowItem.getUrl());
        holder.txtTitle.setText(rowItem.getTitle());
        holder.txtPrice.setText(rowItem.getPrice());
        holder.txtDate.setText(DateFormat.getDateInstance().format(rowItem.getDate()));

        //bookmark star full for bookmarked ad
        if (Arrays.asList(bookmarks).contains(rowItem.getAdId())) {
            holder.bookmarkStar.setImageResource(R.drawable.bockmark_star_full);
        } else {
            holder.bookmarkStar.setImageResource(R.drawable.bockmark_star_empty);
        }

        //click to bookmark/debookmark an ad
        holder.bookmarkStar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Arrays.asList(bookmarks).contains(rowItem.getAdId())) {
                    deleteBookmark(rowItem.getAdId(), getUserId());
                    holder.bookmarkStar.setImageResource(R.drawable.bockmark_star_empty);
                    notifyDataSetChanged();
                    Log.d("CONAN  ", position + "");
                    Log.d("CONAN", "bookmark weg");
                } else {
                    bookmarkAd(rowItem.getAdId(), getUserId());
                    holder.bookmarkStar.setImageResource(R.drawable.bockmark_star_full);
                    notifyDataSetChanged();
                    Log.d("CONAN", "bookmark dazu");
                    Log.d("CONAN  ", position + "");
                }
            }
        });

        return convertView;
    }


    private void deleteBookmark(String adId, String userId) {
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        String url = Urls.MAIN_SERVER_URL + Urls.BOOKMARK_DELETE + "?adId=" + adId + "&userId=" + userId;
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Toast.makeText(context, "Bookmark deleted!", Toast.LENGTH_SHORT).show();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(context, "Something went wrong...", Toast.LENGTH_LONG).show();
            }
        });
        requestQueue.add(stringRequest);

    }

    private void bookmarkAd(String adId, String userId) {
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        String url = Urls.MAIN_SERVER_URL + Urls.BOOKMARK_AD + "?adId=" + adId + "&userId=" + userId;
        Log.d("CONAN", url);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Toast.makeText(context, "Bookmarked!", Toast.LENGTH_SHORT).show();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(context, "Something went wrong...\n" + error.toString(), Toast.LENGTH_LONG).show();
            }
        });
        requestQueue.add(stringRequest);
    }

    private String getUserId() {
        return context.getSharedPreferences("UserInfo", 0).getString(Constants.USER_ID, "");
    }
}