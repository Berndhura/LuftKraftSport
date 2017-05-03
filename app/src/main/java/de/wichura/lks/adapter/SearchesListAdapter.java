package de.wichura.lks.adapter;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import de.wichura.lks.R;
import de.wichura.lks.http.Service;
import de.wichura.lks.mainactivity.Constants;
import de.wichura.lks.models.SearchItem;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static de.wichura.lks.mainactivity.Constants.SHARED_PREFS_USER_INFO;

/**
 * Created by Bernd Wichura on 07.02.2017.
 * Luftkrafsport
 */

public class SearchesListAdapter extends ArrayAdapter<SearchItem> {

    private Context context;
    private Service service;

    public SearchesListAdapter(final Context context, final int resourceId, final List<SearchItem> items) {
        super(context, resourceId, items);
        this.context = context;
        service = new Service();
    }

    private class ViewHolder {
        TextView title;
        TextView priceRange;
        TextView distance;
        TextView location;
        ImageView deleteSearch;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final LayoutInflater mInflater = (LayoutInflater) context
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

        SearchesListAdapter.ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.searches_overview_item, parent, false);

            holder = new SearchesListAdapter.ViewHolder();
            holder.title = (TextView) convertView.findViewById(R.id.search_title);
            holder.priceRange = (TextView) convertView.findViewById(R.id.search_price_range);
            holder.distance = (TextView) convertView.findViewById(R.id.search_distance);
            holder.deleteSearch = (ImageView) convertView.findViewById(R.id.delete_search);
            holder.location = (TextView) convertView.findViewById(R.id.location_name);
            convertView.setTag(holder);
        } else
            holder = (SearchesListAdapter.ViewHolder) convertView.getTag();

        final SearchItem searchItem = getItem(position);

        holder.title.setText(showTitle(searchItem));
        holder.priceRange.setText(showPriceRange(searchItem));
        holder.distance.setText(showDistance(searchItem));
        holder.location.setText(showLocation(searchItem));

        holder.deleteSearch.setOnClickListener((view) -> deleteSearch(searchItem.getId(), view));
        holder.deleteSearch.setTag(position);

        return convertView;
    }

    private String showTitle(SearchItem item) {
        return "Was: " + item.getDescription();
    }

    private String showPriceRange(SearchItem item) {
        if (Constants.MAX_PRICE.equals(item.getPriceTo())) {
            return "Preis: Beliebig";
        } else {
            return "Von: " + item.getPriceFrom() + "€ Bis: " + item.getPriceTo() + "€";
        }
    }


    private String showLocation(SearchItem item) {
        return "Wo: " + item.getLocationName();
    }

    private String showDistance(SearchItem item) {
        if (item.getDistance() != null) {
            if (Constants.DISTANCE_INFINITY.equals(item.getDistance())) {
                return "Entfernung egal";
            } else {
                Integer distanceInKm = item.getDistance() / 1000;
                return "Im Umkreis von: " + distanceInKm + " km";
            }
        } else {
            return "Entfernung egal";
        }
    }

    private void deleteSearch(Long id, View view) {
        service.deleteSearchesObserv(id, getUserToken())
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<String>() {
                    @Override
                    public void onCompleted() {
                        //view.disableProgress();
                        String pos = view.getTag().toString();
                        int position = Integer.parseInt(pos);
                        remove(getItem(position));
                        notifyDataSetChanged();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d("CONAN", "error deleting searches: " + "id: " + id + e.getMessage());
                    }

                    @Override
                    public void onNext(String result) {
                        //disableProgress();
                        //view.dataChanged();
                        Log.d("CONAN", "deleting searches: " + "id: " + id);

                    }
                });
    }

    private String getUserToken() {
        return context.getSharedPreferences(SHARED_PREFS_USER_INFO, 0).getString(Constants.USER_TOKEN, "");
    }
}
