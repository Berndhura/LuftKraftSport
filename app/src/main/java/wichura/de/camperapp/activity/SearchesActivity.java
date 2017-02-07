package wichura.de.camperapp.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;

import java.util.List;

import wichura.de.camperapp.R;
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

        setContentView(R.layout.fragment_one);

        presenter = new SearchesPresenter(this, new Service(), getApplicationContext());

        presenter.loadSearchesForUser();

        final Button submitButton = (Button) findViewById(R.id.get_searches);
        submitButton.setOnClickListener((v) -> {
            presenter.loadSearchesForUser();
        });
    }


    public void updateSearches(List<SearchItem> searchItem) {
        //...
        Log.d("CONAN", searchItem+"");
    }
}
