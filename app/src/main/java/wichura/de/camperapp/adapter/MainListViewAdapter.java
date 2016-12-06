package wichura.de.camperapp.adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.IntDef;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import wichura.de.camperapp.R;
import wichura.de.camperapp.http.Service;
import wichura.de.camperapp.http.Urls;
import wichura.de.camperapp.mainactivity.Constants;
import wichura.de.camperapp.models.RowItem;

import static wichura.de.camperapp.mainactivity.Constants.IS_MY_ADS;
import static wichura.de.camperapp.mainactivity.Constants.SHARED_PREFS_USER_INFO;
import static wichura.de.camperapp.mainactivity.Constants.SHOW_MY_ADS;

public class MainListViewAdapter extends ArrayAdapter<RowItem> {

    private Context context;
    private ViewHolder holder;
    private ArrayList<String> bookmarks;
    private Activity activity;
    private Service service;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({LOCATION_STATUS_OK, LOCATION_STATUS_SERVER_DOWN, LOCATION_STATUS_SERVER_INVALID, LOCATION_STATUS_UNKNOWN, LOCATION_STATUS_INVALID})
    public @interface LocationStatus {
    }

    public static final int LOCATION_STATUS_OK = 0;
    public static final int LOCATION_STATUS_SERVER_DOWN = 1;
    public static final int LOCATION_STATUS_SERVER_INVALID = 2;
    public static final int LOCATION_STATUS_UNKNOWN = 3;
    public static final int LOCATION_STATUS_INVALID = 4;

    public MainListViewAdapter(final Activity activity, final Context context, final int resourceId,
                               final List<RowItem> items, final ArrayList<String> bookmarks) {
        super(context, resourceId, items);
        this.context = context;
        this.activity = activity;
        if (bookmarks != null) {
            this.bookmarks = bookmarks;
        } else {
            this.bookmarks = null;
        }
        service = new Service();
    }

    /* private view holder class */
    private class ViewHolder {
        TextView txtTitle;
        TextView txtPrice;
        TextView txtDate;
        ImageView bookmarkStar;
        LinearLayout myAdsView;
        LinearLayout mainLl;
        ImageView deleteButton;
        TextView txtViews;
        ImageView thumbNail;
        TextView txtNumberOfBookmarks;

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
            holder.myAdsView = (LinearLayout) convertView.findViewById(R.id.my_ads_view);
            holder.mainLl = (LinearLayout) convertView.findViewById(R.id.main_linear_layout);
            holder.deleteButton = (ImageView) convertView.findViewById(R.id.NEW_my_ad_delete);
            holder.txtViews = (TextView) convertView.findViewById(R.id.NEW_my_views);
            holder.txtNumberOfBookmarks = (TextView) convertView.findViewById(R.id.number_of_bookmarks);
            holder.thumbNail = (ImageView) convertView.findViewById(R.id.icon);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        // getting ad data for the row
        final RowItem rowItem = getItem(position);

        Picasso.with(context)
                .load(Urls.MAIN_SERVER_URL + Urls.GET_PICTURE_THUMB + rowItem.getUrl())
                .resize(100, 100)
                .centerCrop()
                .into(holder.thumbNail);

        // Log.d("CONAN, get pic URLs: ", rowItem.getUrl());
        holder.txtTitle.setText(rowItem.getTitle());
        holder.txtPrice.setText(rowItem.getPrice());
        holder.txtDate.setText(DateFormat.getDateInstance().format(rowItem.getDate()));

        //bookmark star full for bookmarked ad
        if (bookmarks == null) {
            holder.bookmarkStar.setImageResource(R.drawable.bockmark_star_empty);
        } else {
            if (bookmarks.contains(rowItem.getAdId())) {
                holder.bookmarkStar.setImageResource(R.drawable.bockmark_star_full);
            } else {
                holder.bookmarkStar.setImageResource(R.drawable.bockmark_star_empty);
            }
        }

        //remove linearlayout for delete. view count...
        if (!isMyAdsRequest()) {
            holder.mainLl.removeView(holder.myAdsView);
        } else {

            //Delete Button
            holder.deleteButton.setOnClickListener((view) -> {
                //get ad id and send delete request
                String adId = rowItem.getAdId();
                deleteAdRequest(adId, view);
            });
            holder.deleteButton.setTag(position);

            //Views
            holder.txtViews.setText(rowItem.getViews());
            holder.txtNumberOfBookmarks.setText(rowItem.getBookmarks());
        }

        //click to bookmark/debookmark an ad
        holder.bookmarkStar.setOnClickListener((view) -> {
            if (bookmarks != null && bookmarks.contains(rowItem.getAdId())) {
                deleteBookmark(rowItem.getAdId());
                holder.bookmarkStar.setImageResource(R.drawable.bockmark_star_empty);
                notifyDataSetChanged();
            } else {
                bookmarkAd(rowItem.getAdId());
                holder.bookmarkStar.setImageResource(R.drawable.bockmark_star_full);
                notifyDataSetChanged();
            }
        });

        return convertView;
    }


    private void deleteBookmark(String adId) {
        service.delBookmarkAdObserv(adId, getUserToken())
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<String>() {
                    @Override
                    public void onCompleted() {
                        Toast.makeText(context, "Bookmark deleted!", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d("CONAN", "error in bookmark ad: " + e.getMessage());
                    }

                    @Override
                    public void onNext(String result) {
                        Log.d("CONAN", "bookmark deleted: " + result);
                    }
                });

    }

    private void bookmarkAd(String adId) {
        service.bookmarkAdObserv(adId, getUserToken())
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<String>() {
                    @Override
                    public void onCompleted() {
                        Toast.makeText(context, "Ad is bookmarked!", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d("CONAN", "error in bookmark ad: " + e.getMessage());
                    }

                    @Override
                    public void onNext(String result) {
                        Log.d("CONAN", "bookmark ad: " + result);
                    }
                });
    }

    private void deleteAdRequest(final String adId, final View view) {
        AlertDialog myQuittingDialogBox = new AlertDialog.Builder(activity)
                //set message, title, and icon
                .setTitle("Delete")
                .setMessage("Do you want to Delete")
                .setIcon(R.drawable.ic_delete_blue_grey_600_24dp)
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                        Service service = new Service();
                        service.deleteAdObserv(adId)
                                .subscribeOn(Schedulers.newThread())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Subscriber<String>() {
                                    @Override
                                    public void onCompleted() {
                                        Log.d("CONAN", "Ad deleted");
                                        String pos = view.getTag().toString();
                                        int position = Integer.parseInt(pos);
                                        remove(getItem(position));
                                        notifyDataSetChanged();
                                        dialog.dismiss();
                                    }

                                    @Override
                                    public void onError(Throwable e) {
                                        Log.d("CONAN", "error in deleting ad: " + e.getMessage());
                                    }

                                    @Override
                                    public void onNext(String result) {
                                        Log.d("CONAN", "delete ad: " + result);
                                    }
                                });
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

    private boolean isMyAdsRequest() {
        return context.getSharedPreferences(SHOW_MY_ADS, 0).getBoolean(IS_MY_ADS, false);
    }

    private String getUserToken() {
        return context.getSharedPreferences(SHARED_PREFS_USER_INFO, 0).getString(Constants.USER_TOKEN, "");
    }

    private String getUserId() {
        return context.getSharedPreferences(SHARED_PREFS_USER_INFO, 0).getString(Constants.USER_ID, "");
    }
}