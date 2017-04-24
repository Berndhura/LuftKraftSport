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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.List;

import de.wichura.lks.http.GoogleService;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import de.wichura.lks.R;
import de.wichura.lks.http.Service;
import de.wichura.lks.mainactivity.Constants;
import de.wichura.lks.models.SearchItem;

import static de.wichura.lks.mainactivity.Constants.SHARED_PREFS_USER_INFO;

/**
 * Created by ich on 07.02.2017.
 * Luftkrafsport
 */

public class SearchesListAdapter extends ArrayAdapter<SearchItem> {

    private Context context;
    private Service service;
    private GoogleService locationService;

    public SearchesListAdapter(final Context context, final int resourceId, final List<SearchItem> items) {
        super(context, resourceId, items);
        this.context = context;
        service = new Service();
        locationService = new GoogleService();
    }

    private class ViewHolder {
        TextView title;
        TextView priceFrom;
        TextView priceTo;
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
            holder.priceFrom = (TextView) convertView.findViewById(R.id.search_price_from);
            holder.priceTo = (TextView) convertView.findViewById(R.id.search_price_to);
            holder.distance = (TextView) convertView.findViewById(R.id.search_distance);
            holder.deleteSearch = (ImageView) convertView.findViewById(R.id.delete_search);
            holder.location = (TextView) convertView.findViewById(R.id.location_name);
            convertView.setTag(holder);
        } else
            holder = (SearchesListAdapter.ViewHolder) convertView.getTag();

        // getting ad data for the row
        final SearchItem searchItem = getItem(position);

        holder.title.setText(searchItem.getDescription());
        holder.priceFrom.setText(searchItem.getPriceFrom().toString());
        holder.priceTo.setText(searchItem.getPriceTo().toString());
        holder.distance.setText(searchItem.getDistance().toString());

        //TODO beim scrollen werden staendig requests gestartet?! nicht gut
        getLocationName(searchItem, position);

        // holder.date.setText(DateFormat.getDateInstance().format(searchItem.getDate()));

        holder.deleteSearch.setOnClickListener((view) -> {
            deleteSearch(searchItem.getId(), view);
        });
        holder.deleteSearch.setTag(position);

        return convertView;
    }

    private void getLocationName(SearchItem item, int position) {

        Observable<JsonObject> getCityNameFromLatLng = locationService.getCityNameFrimLatLngObserv(item.getLat(), item.getLng(), false);

        getCityNameFromLatLng
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<JsonObject>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d("CONAN", "new Article Presenter: error in getting city name from google maps api: " + e.toString());
                    }

                    @Override
                    public void onNext(JsonObject location) {
                        JsonElement city = location.get("results").getAsJsonArray()
                                .get(0).getAsJsonObject().get("address_components").getAsJsonArray()
                                .get(2).getAsJsonObject().get("long_name");

                        Log.d("CONAN", "city name from google maps api: " + city);

                        //holder.location.setText(city.getAsString());
                    }
                });
    }


    private void deleteSearch(Long id, View view) {
        //view.enableProgress();
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
