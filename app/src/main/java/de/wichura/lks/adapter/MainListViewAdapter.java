package de.wichura.lks.adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
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
import com.squareup.picasso.Transformation;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import de.wichura.lks.R;
import de.wichura.lks.activity.NewAdActivity;
import de.wichura.lks.http.Service;
import de.wichura.lks.http.Urls;
import de.wichura.lks.mainactivity.Constants;
import de.wichura.lks.mainactivity.MainActivity;
import de.wichura.lks.models.RowItem;
import de.wichura.lks.presentation.MainPresenter;
import de.wichura.lks.util.Utility;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static de.wichura.lks.mainactivity.Constants.IS_BOOKMARKS;
import static de.wichura.lks.mainactivity.Constants.IS_MY_ADS;
import static de.wichura.lks.mainactivity.Constants.SHARED_PREFS_USER_INFO;
import static de.wichura.lks.mainactivity.Constants.SHOW_BOOKMARKS;
import static de.wichura.lks.mainactivity.Constants.SHOW_MY_ADS;
import static de.wichura.lks.mainactivity.Constants.USERS_LOCATION;

public class MainListViewAdapter extends ArrayAdapter<RowItem> {

    private Context context;
    private ArrayList<Long> bookmarks;
    private Activity activity;
    private Service service;
    private MainPresenter presenter;
    private Boolean isLocationSet;

    public MainListViewAdapter(final Activity activity, final Context context, final int resourceId,
                               final List<RowItem> items, final ArrayList<Long> bookmarks) {
        super(context, resourceId, items);
        this.context = context;
        this.activity = activity;
        if (bookmarks != null) {
            this.bookmarks = bookmarks;
        } else {
            this.bookmarks = null;
        }
        service = new Service();
        presenter = new MainPresenter((MainActivity) activity, service, context);

        isLocationSet = isLocationOn();
    }

    /* private view holder class */
    private static class ViewHolder {
        TextView txtTitle;
        TextView txtPrice;
        TextView txtDate;
        TextView distance;
        ImageView bookmarkStar;
        LinearLayout myAdsView;
        LinearLayout mainLl;
        ImageView deleteButton;
        ImageView editButton;
        TextView txtViews;
        ImageView thumbNail;
        TextView txtNumberOfBookmarks;
        ImageView new_ad_marker;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        //final LayoutInflater mInflater = (LayoutInflater) getContext().getSystemService(context.LAYOUT_INFLATER_SERVICE);
        final LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

        ViewHolder holder;

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.list_item_new, parent, false);
            holder = new ViewHolder();
            holder.txtTitle = convertView.findViewById(R.id.title);
            holder.txtPrice = convertView.findViewById(R.id.price);
            holder.distance = convertView.findViewById(R.id.distance);
            holder.txtDate = convertView.findViewById(R.id.creation_date);
            holder.bookmarkStar = convertView.findViewById(R.id.bookmark_star);
            holder.myAdsView = convertView.findViewById(R.id.my_ads_view);
            holder.mainLl = convertView.findViewById(R.id.main_linear_layout);
            holder.deleteButton = convertView.findViewById(R.id.NEW_my_ad_delete);
            holder.editButton = convertView.findViewById(R.id.edit_my_article);
            holder.txtViews = convertView.findViewById(R.id.article_views);
            holder.txtNumberOfBookmarks = convertView.findViewById(R.id.number_of_bookmarks);
            holder.thumbNail = convertView.findViewById(R.id.icon);
            holder.new_ad_marker = convertView.findViewById(R.id.new_ad_marker);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        // getting ad data for the row
        final RowItem rowItem = getItem(position);

        Transformation transformation = new Transformation() {

            @Override
            public Bitmap transform(Bitmap source) {
                int targetWidth = holder.thumbNail.getWidth();

                double aspectRatio = (double) source.getHeight() / (double) source.getWidth();
                int targetHeight = (int) (targetWidth * aspectRatio);
                Bitmap result = Bitmap.createScaledBitmap(source, targetWidth, targetHeight, false);
                if (result != source) {
                    // Same bitmap is returned if sizes are the same
                    source.recycle();
                }
                return result;
            }

            @Override
            public String key() {
                return "transformation" + " desiredWidth";
            }
        };

        Picasso.with(context)
                .load(Urls.MAIN_SERVER_URL_V3 + "pictures/" + ((rowItem.getPictureIds().length > 0) ? rowItem.getPictureIds()[0] : ""))
                .placeholder(R.drawable.lks_app_logo)
                //.transform(transformation)
                .fit()
                //.networkPolicy(NetworkPolicy.OFFLINE)
                .into(holder.thumbNail);

        Log.d("LA", Urls.MAIN_SERVER_URL_V3 + "pictures/" + ((rowItem.getPictureIds().length > 0) ? rowItem.getPictureIds()[0] : ""));

        holder.txtTitle.setText(rowItem.getTitle());

        holder.txtPrice.setText(Utility.getPriceString(rowItem.getPrice()));
        holder.txtDate.setText("Erstellt am "+ new SimpleDateFormat("dd. MMM").format(rowItem.getDate()));

        if (isMyAdsRequest()) {
            holder.distance.setText(rowItem.getLocationName());
        } else if (!isLocationServiceEnabled()) {
            holder.distance.setText(rowItem.getLocationName());
        } else if (!isLocationSet) {
            holder.distance.setText(rowItem.getLocationName());
        } else {
            holder.distance.setText(getDistAndLocation(rowItem));
        }

        //bookmark star full for bookmarked ad
        if (bookmarks == null) {
            holder.bookmarkStar.setImageResource(R.drawable.bockmark_star_empty);
        } else {
            if (bookmarks.contains(Long.parseLong(rowItem.getId().toString()))) {
                holder.bookmarkStar.setImageResource(R.drawable.bockmark_star_full);
            } else {
                holder.bookmarkStar.setImageResource(R.drawable.bockmark_star_empty);
            }
        }

        //Views
        holder.txtViews.setText(rowItem.getViews()+ " Aufrufe");
        holder.txtNumberOfBookmarks.setText(rowItem.getBookmarks());

        //no login -> disable bookmark button
        if ("".equals(getUserToken())) {
            holder.bookmarkStar.setEnabled(true);
            holder.bookmarkStar.setOnClickListener(v -> Toast.makeText(context, "Bitte anmelden!", Toast.LENGTH_LONG).show());
        } else {
            holder.bookmarkStar.setOnClickListener((view) -> {
                Integer id = rowItem.getId();
                if (bookmarks != null && bookmarks.contains(Long.parseLong(id.toString()))) {
                    LinearLayout vwParentRow = (LinearLayout) view.getParent();
                    ((ImageView) vwParentRow.getChildAt(2)).setImageResource(R.drawable.bockmark_star_empty);
                    vwParentRow.refreshDrawableState();
                    removeFromBookmark(id);
                    presenter.deleteBookmark(id);
                    //remove from list if bookmark list is open
                    if (isBookmarksRequest()) {
                        remove(getItem(position));
                        notifyDataSetChanged();
                    }
                } else {
                    LinearLayout vwParentRow = (LinearLayout) view.getParent();
                    ((ImageView) vwParentRow.getChildAt(2)).setImageResource(R.drawable.bockmark_star_full);
                    vwParentRow.refreshDrawableState();
                    bookmarks.add(Long.parseLong(id.toString()));
                    presenter.bookmarkAd(id);
                }
            });
        }


        long date = System.currentTimeMillis();
        final long oneWeek = 7 * 24 * 60 * 60 * 1000;

        if ((date - rowItem.getDate()) < oneWeek) {
            holder.new_ad_marker.setVisibility(View.VISIBLE);
            holder.new_ad_marker.setImageResource(R.drawable.ic_fiber_new_red_600_24dp);
        } else {
            holder.new_ad_marker.setVisibility(View.INVISIBLE);
        }

        //remove linearlayout for delete. view count...
        if (!isMyAdsRequest()) {
            holder.mainLl.removeView(holder.myAdsView);
        } else {

            //Delete Button
            holder.deleteButton.setOnClickListener((view) -> {
                //get ad id and send delete request
                Integer adId = rowItem.getId();
                deleteAdRequest(adId, view);
            });
            holder.deleteButton.setTag(position);

            //edit my article
            holder.editButton.setOnClickListener(view -> {
                Intent i = new Intent(activity.getApplicationContext(), NewAdActivity.class);
                i.putExtra(Constants.ARTICLE_ID, rowItem.getId());
                i.putExtra(Constants.TITLE, rowItem.getTitle());
                i.putExtra(Constants.DESCRIPTION, rowItem.getDescription());
                i.putExtra(Constants.PRICE, rowItem.getPrice());
                i.putExtra(Constants.AD_URL, rowItem.getUrl());
                i.putExtra(Constants.LAT, rowItem.getLocation().getCoordinates()[0]);
                i.putExtra(Constants.LNG, rowItem.getLocation().getCoordinates()[1]);
                i.putExtra(Constants.DATE, rowItem.getDate());
                activity.startActivityForResult(i, Constants.REQUEST_ID_FOR_NEW_AD);
            });
        }

        return convertView;
    }

    private String getDistAndLocation(RowItem item) {
        return item.getDistance() + " km";
    }

    private void removeFromBookmark(Integer id) {
        int i = 0;
        for (Long bm : new ArrayList<>(bookmarks)) {
            if (bm == Long.parseLong(id.toString())) bookmarks.remove(i);
            i++;
        }
    }

    private void deleteAdRequest(final Integer adId, final View view) {
        AlertDialog myQuittingDialogBox = new AlertDialog.Builder(activity)
                //set message, title, and icon
                .setTitle(R.string.delete)
                .setMessage(R.string.confirm_delete)
                .setIcon(R.drawable.ic_delete_blue_grey_600_24dp)
                .setPositiveButton("Lösche", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                        service.deleteAdObserv(adId, getUserToken())
                                .subscribeOn(Schedulers.newThread())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Observer<String>() {
                                    @Override
                                    public void onComplete() {
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

                                    @Override
                                    public void onSubscribe(Disposable d) {

                                    }
                                });
                    }
                })

                .setNegativeButton("Nein!", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();
        myQuittingDialogBox.show();
    }


    private Boolean isLocationOn() {
        SharedPreferences settings = context.getSharedPreferences(Constants.USERS_LOCATION, 0);
        Double lat = Double.longBitsToDouble(settings.getLong(Constants.LAT, 0));
        Double lng = Double.longBitsToDouble(settings.getLong(Constants.LNG, 0));
        return !(lat.equals(0.0) && lng.equals(0.0));
    }

    private boolean isMyAdsRequest() {
        return context.getSharedPreferences(SHOW_MY_ADS, 0).getBoolean(IS_MY_ADS, false);
    }

    private boolean isBookmarksRequest() {
        return context.getSharedPreferences(SHOW_BOOKMARKS, 0).getBoolean(IS_BOOKMARKS, false);
    }

    private String getUserToken() {
        return context.getSharedPreferences(SHARED_PREFS_USER_INFO, 0).getString(Constants.USER_TOKEN, "");
    }

    private Boolean isLocationServiceEnabled() {
        return context.getSharedPreferences(USERS_LOCATION, 0).getBoolean(Constants.LOCATION_SERVICE_IS_ENABLED, false);
    }
}