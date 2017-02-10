package wichura.de.camperapp.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ListView;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.List;

import wichura.de.camperapp.R;
import wichura.de.camperapp.adapter.SearchesListAdapter;
import wichura.de.camperapp.http.Service;
import wichura.de.camperapp.models.SearchItem;
import wichura.de.camperapp.presentation.SearchesPresenter;

/**
 * Created by bwichura on 07.02.2017.
 * desurf
 */

public class SearchesActivity extends AppCompatActivity {

    private SearchesPresenter presenter;
    private ListView listView;
    private SearchesListAdapter adapter;
    private ProgressBar progressBar;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.searches_overview_layout);

        progressBar = (ProgressBar) findViewById(R.id.searches_overview_ProgressBar);

        Toolbar toolbar = (Toolbar) findViewById(R.id.searches_overview_toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            toolbar.setNavigationOnClickListener((view) -> finish());
        }

        presenter = new SearchesPresenter(this, new Service(), getApplicationContext());

        listView = (ListView) findViewById(R.id.searches_overview_list);

        presenter.loadSearchesForUser();
    }


    public void updateSearches(List<SearchItem> searchItem) {
        List<SearchItem> rowItems = new ArrayList<>();
        rowItems.addAll(searchItem);

        getSupportActionBar().setTitle("Searches: " + rowItems.size());

        adapter = new SearchesListAdapter(
                getApplicationContext(), R.layout.searches_overview_item, rowItems);
        listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        disableProgressbar();
    }

    private void disableProgressbar() {
        progressBar.setVisibility(ProgressBar.GONE);
    }
}
