package de.wichura.lks.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.List;

import de.wichura.lks.R;
import de.wichura.lks.adapter.SearchesListAdapter;
import de.wichura.lks.http.Service;
import de.wichura.lks.models.SearchItem;
import de.wichura.lks.presentation.SearchesPresenter;

/**
 * Created by bwichura on 07.02.2017.
 * LuftKraftSport
 */

public class SearchesActivity extends Fragment {

    private ListView listView;
    private ProgressBar progressBar;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        return inflater.inflate(R.layout.searches_overview_layout, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        progressBar = (ProgressBar) view.findViewById(R.id.searches_overview_ProgressBar);

        Toolbar toolbar = (Toolbar) view.findViewById(R.id.searches_overview_toolbar);
        if (toolbar != null) {
            ((SearchActivity) getActivity()).setSupportActionBar(toolbar);
            if (((SearchActivity) getActivity()).getSupportActionBar() != null) {
                ((SearchActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                ((SearchActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);
            }
            toolbar.setNavigationOnClickListener((v) -> getActivity().finish());
        }

        SearchesPresenter presenter = new SearchesPresenter(this, new Service(), getActivity().getApplicationContext());

        listView = (ListView) view.findViewById(R.id.searches_overview_list);

        presenter.loadSearchesForUser();
    }


    public void updateSearches(List<SearchItem> searchItem) {
        List<SearchItem> rowItems = new ArrayList<>();
        rowItems.addAll(searchItem);

        if (((SearchActivity) getActivity()).getSupportActionBar() != null)
            ((SearchActivity) getActivity()).getSupportActionBar().setTitle("Gespeicherte Suchen: " + rowItems.size());

        SearchesListAdapter adapter = new SearchesListAdapter(
                getActivity().getApplicationContext(), R.layout.searches_overview_item, rowItems);
        listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        disableProgressbar();
    }

    private void disableProgressbar() {
        progressBar.setVisibility(ProgressBar.GONE);
    }
}
