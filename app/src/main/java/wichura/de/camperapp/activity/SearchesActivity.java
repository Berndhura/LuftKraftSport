package wichura.de.camperapp.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import wichura.de.camperapp.http.Service;
import wichura.de.camperapp.models.SearchItem;
import wichura.de.camperapp.presentation.SearchesPresenter;

/**
 * Created by bwichura on 07.02.2017.
 *
 */

public class SearchesActivity extends AppCompatActivity {

    private SearchesPresenter presenter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        presenter = new SearchesPresenter(this, new Service(), getApplicationContext());

        presenter.loadSearchesForUser();
    }


    public void updateSearches(SearchItem searchItem) {
        //...
    }
}
