package wichura.de.camperapp.adapter;

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

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import wichura.de.camperapp.R;
import wichura.de.camperapp.http.Service;
import wichura.de.camperapp.mainactivity.Constants;
import wichura.de.camperapp.models.SearchItem;

import static wichura.de.camperapp.mainactivity.Constants.SHARED_PREFS_USER_INFO;

/**
 * Created by ich on 07.02.2017.
 *
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
        TextView name;
        TextView date;
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
            holder.name = (TextView) convertView.findViewById(R.id.search_distance);
            holder.date = (TextView) convertView.findViewById(R.id.search_price);
            holder.deleteSearch = (ImageView) convertView.findViewById(R.id.delete_search);
            convertView.setTag(holder);
        } else
            holder = (SearchesListAdapter.ViewHolder) convertView.getTag();

        // getting ad data for the row
        final SearchItem searchItem = getItem(position);

        holder.title.setText(searchItem.getDescription());
        holder.name.setText(searchItem.getPriceTo().toString());
        // holder.date.setText(DateFormat.getDateInstance().format(searchItem.getDate()));

        //click to bookmark/debookmark an ad
        holder.deleteSearch.setOnClickListener((view) -> {
            deleteSearch(searchItem.getId());
        });

        return convertView;
    }

    private void deleteSearch(Long id) {
        //view.enableProgress();
        service.deleteSearchesObserv(id, getUserToken())
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<String>() {
                    @Override
                    public void onCompleted() {
                        //view.disableProgress();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d("CONAN", "error deleting searches: "+"id: "+ id + e.getMessage());
                    }

                    @Override
                    public void onNext(String result) {
                        //disableProgress();
                        //view.dataChanged();
                        Log.d("CONAN", "deleting searches: "+"id: "+ id);
                    }
                });
    }

    private String getUserToken() {
        return context.getSharedPreferences(SHARED_PREFS_USER_INFO, 0).getString(Constants.USER_TOKEN, "");
    }
}
