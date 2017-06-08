package de.wichura.lks.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.wang.avi.AVLoadingIndicatorView;

import java.util.ArrayList;
import java.util.List;

import de.wichura.lks.R;
import de.wichura.lks.adapter.SearchesListAdapter;
import de.wichura.lks.http.Service;
import de.wichura.lks.mainactivity.Constants;
import de.wichura.lks.mainactivity.MainApp;
import de.wichura.lks.models.SearchItem;
import de.wichura.lks.presentation.SearchesPresenter;

import static android.app.Activity.RESULT_OK;

/**
 * Created by Bernd Wichura on 07.02.2017.
 * LuftKraftSport
 */

public class SearchesActivity extends Fragment implements
        GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks {

    private ListView listView;
    private AVLoadingIndicatorView progressBar;
    private View mainView;
    private SearchesPresenter presenter;
    private GoogleApiClient mGoogleApiClient;
    private LinearLayout emptyView;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //Google Api client
        //initGoogleApiClient();
        if(MainApp.getGoogleApiHelper().isConnected())
        {
            //Get google api client
            mGoogleApiClient = MainApp.getGoogleApiHelper().getGoogleApiClient();
            Log.d("CONAN", "google client connected in Searches Activity!");
        }

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.searches_overview_layout, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        emptyView = (LinearLayout) view.findViewById(R.id.searches_overview_empty_page);

        progressBar = (AVLoadingIndicatorView) view.findViewById(R.id.searches_overview_ProgressBar);
        mainView = view;

        Toolbar toolbar = (Toolbar) view.findViewById(R.id.searches_overview_toolbar);
        if (toolbar != null) {
            ((SearchActivity) getActivity()).setSupportActionBar(toolbar);
            if (((SearchActivity) getActivity()).getSupportActionBar() != null) {
                ((SearchActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                ((SearchActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);
            }
            toolbar.setNavigationOnClickListener((v) -> getActivity().finish());
        }

        presenter = new SearchesPresenter(this, new Service(), getActivity().getApplicationContext());

        listView = (ListView) view.findViewById(R.id.searches_overview_list);

        listView.setOnItemClickListener((adapterView, v, position, l) -> {
            final SearchItem rowItem = (SearchItem) listView.getItemAtPosition(position);

            //open main activity for search
            //final Intent intent = new Intent(getView().getContext(), MainActivity.class);
            Intent intent = new Intent();
            intent.putExtra(Constants.KEYWORDS, rowItem.getDescription());
            intent.putExtra(Constants.LAT, rowItem.getLat());
            intent.putExtra(Constants.LNG, rowItem.getLng());
            intent.putExtra(Constants.DISTANCE, rowItem.getDistance());
            intent.putExtra(Constants.PRICE_FROM, rowItem.getPriceFrom().toString());  //is a String in main activity
            intent.putExtra(Constants.PRICE_TO, rowItem.getPriceTo().toString());
            getActivity().setResult(RESULT_OK, intent);
            getActivity().finish();
            //startActivityForResult(intent, Constants.REQUEST_ID_FOR_FOLLOW_SEARCH);
        });

        presenter.loadSearchesForUser();
    }

    public void emptyPage() {
        emptyView.setVisibility(View.VISIBLE);
    }

    public void updateSearches(List<SearchItem> searchItem) {

        emptyView.setVisibility(View.GONE);

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

    public void showProblem() {
        View empty = mainView.findViewById(R.id.empty_follow_searches_view);
        listView.setEmptyView(empty);
        empty.setVisibility(View.VISIBLE);


        ImageView reload = (ImageView) mainView.findViewById(R.id.reload_follow_searches_button);
        reload.setOnClickListener(v -> {
            empty.setVisibility(View.GONE);
            presenter.loadSearchesForUser();
        });
    }

    public void enableProgressBar() {
        progressBar.setVisibility(ProgressBar.VISIBLE);
    }

    public void disableProgressbar() {
        progressBar.setVisibility(ProgressBar.GONE);
    }

    public GoogleApiClient getGoogleApiClient() {
        return mGoogleApiClient;
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d("CONAN", "onConnectionFailed");
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d("CONAN", "onConnected");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d("CONAN", "onConnectionSuspended");

    }
}
