package de.wichura.lks.controller;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.util.ArrayList;
import java.util.List;

import de.wichura.lks.R;
import de.wichura.lks.activity.OpenAdActivity;
import de.wichura.lks.adapter.MainListViewAdapter;
import de.wichura.lks.mainactivity.Constants;
import de.wichura.lks.mainactivity.EndlessScrollListener;
import de.wichura.lks.models.AdsAndBookmarks;
import de.wichura.lks.models.RowItem;
import de.wichura.lks.presentation.MainPresenter;
import de.wichura.lks.util.SessionStore;

/**
 * Owns the main ad list: its ArrayAdapter, endless-scroll pagination state,
 * pull-to-refresh, the empty-state view, action-bar title/subtitle, and the
 * per-item click that opens the ad detail screen.
 */
public class AdListController {

    private static final String LIST_STATE = "listState";
    private static final int DEFAULT_PAGE_SIZE = 10;

    private final AppCompatActivity activity;
    private final MainPresenter presenter;
    private final SessionStore session;

    private final AbsListView listView;
    private final SwipeRefreshLayout swipeContainer;
    private final View noResultsView;
    private final CircularProgressIndicator progressBar;
    private final Runnable onPullToRefresh;
    private final ActivityResultLauncher<Intent> openAdLauncher;

    private int page;
    private int size = DEFAULT_PAGE_SIZE;
    private int pages;
    private int total;

    private MainListViewAdapter adapter;
    private List<RowItem> rowItems;
    private Parcelable listState;

    public AdListController(AppCompatActivity activity,
                            MainPresenter presenter,
                            SessionStore session,
                            AbsListView listView,
                            SwipeRefreshLayout swipeContainer,
                            View noResultsView,
                            CircularProgressIndicator progressBar,
                            Runnable onPullToRefresh,
                            ActivityResultLauncher<Intent> openAdLauncher) {
        this.activity = activity;
        this.presenter = presenter;
        this.session = session;
        this.listView = listView;
        this.swipeContainer = swipeContainer;
        this.noResultsView = noResultsView;
        this.progressBar = progressBar;
        this.onPullToRefresh = onPullToRefresh;
        this.openAdLauncher = openAdLauncher;

        initPullToRefresh();
    }

    private void initPullToRefresh() {
        swipeContainer.setOnRefreshListener(() -> {
            hideEmptyView();
            onPullToRefresh.run();
            loadType(Constants.TYPE_ALL);
        });
        swipeContainer.setColorSchemeResources(
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
    }

    /** Reload the list for the given ad type ({@code TYPE_ALL}, {@code TYPE_USER}, {@code TYPE_BOOKMARK}). */
    public void loadType(String type) {
        page = 0;
        if (adapter != null) adapter.clear();
        presenter.loadAdDataPage(page, size, type);
    }

    public void updateAds(AdsAndBookmarks elements, String type,
                          Integer priceFrom, Integer priceTo, Integer distance,
                          String description, String userId) {
        rowItems = new ArrayList<>(elements.getAdsPage().getAds());

        page = elements.getAdsPage().getPage();
        size = elements.getAdsPage().getSize();
        pages = elements.getAdsPage().getPages();
        total = elements.getAdsPage().getTotal();

        showNumberOfAds(total);
        adapter = new MainListViewAdapter(
                activity,
                activity.getApplicationContext(),
                R.layout.list_item_new, rowItems,
                elements.getBookmarks());

        listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        listView.setOnItemClickListener((parent, view, position, id) -> {
            final RowItem rowItem = (RowItem) listView.getItemAtPosition(position);
            final Intent intent = new Intent(activity.getApplicationContext(), OpenAdActivity.class);
            intent.putExtra(Constants.URI_AS_LIST, rowItem.getUrl());
            intent.putExtra(Constants.LOCATION_NAME, rowItem.getLocationName());
            intent.putExtra(Constants.ID, rowItem.getId());
            intent.putExtra(Constants.TITLE, rowItem.getTitle());
            intent.putExtra(Constants.DESCRIPTION, rowItem.getDescription());
            intent.putExtra(Constants.LAT, rowItem.getLocation().getCoordinates()[0]);
            intent.putExtra(Constants.LNG, rowItem.getLocation().getCoordinates()[1]);
            intent.putExtra(Constants.PHONE, rowItem.getPhone());
            intent.putExtra(Constants.PRICE, rowItem.getPrice());
            intent.putExtra(Constants.DATE, rowItem.getDate());
            intent.putExtra(Constants.VIEWS, rowItem.getViews());
            intent.putExtra(Constants.USER_ID_FROM_AD, rowItem.getUserId());
            intent.putExtra(Constants.USER_ID, session.getUserId());
            intent.putExtra(Constants.POSITION_IN_LIST, position);
            intent.putExtra(Constants.AD_URL, rowItem.getUrl());
            openAdLauncher.launch(intent);
        });

        swipeContainer.setRefreshing(false);

        listView.setOnScrollListener(new EndlessScrollListener() {
            @Override
            public boolean onLoadMore(int loadedPages, int totalItemsCount) {
                loadNextDataFromApi(loadedPages, type, priceFrom, priceTo, distance, description, userId);
                return true;
            }
        });
    }

    public void addMoreAdsToList(AdsAndBookmarks elements) {
        rowItems.addAll(elements.getAdsPage().getAds());
        adapter.notifyDataSetChanged();
    }

    public void showProblem(String type) {
        listView.setEmptyView(noResultsView);
        noResultsView.setVisibility(View.VISIBLE);
        ImageView reload = activity.findViewById(R.id.reload_list);
        reload.setOnClickListener(v -> {
            noResultsView.setVisibility(View.GONE);
            if (!Constants.TYPE_SEARCH.equals(type)) {
                presenter.loadAdDataPage(page, size, type);
            }
            // TODO: TYPE_SEARCH reload needs stored search params
        });
    }

    public void hideEmptyView() {
        listView.setEmptyView(null);
        noResultsView.setVisibility(View.GONE);
    }

    public void showNumberOfAds(int numberOfAds) {
        ActionBar bar = activity.getSupportActionBar();
        if (bar != null) bar.setSubtitle("Anzeigen: " + numberOfAds);
    }

    public void setMainTitle(String title) {
        ActionBar bar = activity.getSupportActionBar();
        if (bar != null) bar.setTitle(title);
    }

    public void showProgress() {
        progressBar.setVisibility(ProgressBar.VISIBLE);
    }

    public void hideProgress() {
        progressBar.setVisibility(ProgressBar.GONE);
    }

    public void setListVisibility(int visibility) {
        listView.setVisibility(visibility);
    }

    public void setRefreshing(boolean refreshing) {
        swipeContainer.setRefreshing(refreshing);
    }

    /** Remove the item at {@code position} after the detail screen deletes an ad. */
    public void removeItemAt(int position) {
        if (adapter == null) return;
        adapter.remove(adapter.getItem(position));
        adapter.notifyDataSetChanged();
    }

    public int pageSize() {
        return size;
    }

    public void onSaveInstanceState(Bundle state) {
        listState = listView.onSaveInstanceState();
        state.putParcelable(LIST_STATE, listState);
    }

    public void onRestoreInstanceState(Bundle state) {
        listState = state.getParcelable(LIST_STATE);
    }

    public void onResume() {
        if (listState != null) listView.onRestoreInstanceState(listState);
    }

    private void loadNextDataFromApi(int offset, String type, Integer priceFrom, Integer priceTo,
                                     Integer distance, String description, String userId) {
        Log.d("CONAN", "OFFSET: " + offset);
        if (offset <= pages) {
            page = page + 1;
            if (type == null) {
                presenter.searchForArticles(page, size, priceFrom, priceTo, distance, description, userId);
            } else {
                presenter.loadAdDataPage(page, size, type);
            }
        }
    }
}
