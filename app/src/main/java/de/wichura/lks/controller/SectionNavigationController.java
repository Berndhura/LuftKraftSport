package de.wichura.lks.controller;

import android.content.Intent;
import android.content.SharedPreferences;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

import de.wichura.lks.R;
import de.wichura.lks.activity.EbayActivity;
import de.wichura.lks.activity.MessagesOverviewActivity;
import de.wichura.lks.activity.NewAdActivity;
import de.wichura.lks.activity.SearchActivity;
import de.wichura.lks.activity.SettingsActivity;
import de.wichura.lks.mainactivity.Constants;
import de.wichura.lks.util.SessionStore;

import static de.wichura.lks.mainactivity.Constants.SHOW_BOOKMARKS;
import static de.wichura.lks.mainactivity.Constants.SHOW_MY_ADS;

/**
 * Handles the drawer-menu navigation and the "which section am I in" state
 * (all-ads, my-ads, bookmarks, search). Owns the search-again button that
 * re-launches a search with the last-used parameters, plus the back-press
 * routing that pops out of sub-sections without leaving the app.
 */
public class SectionNavigationController implements NavigationView.OnNavigationItemSelectedListener {

    private final AppCompatActivity activity;
    private final DrawerLayout drawer;
    private final Button searchAgainButton;
    private final SessionStore session;
    private final AdListController adList;
    private final MessageBadgeController messageBadge;
    private final Runnable startLoginAction;
    private final ActivityResultLauncher<Intent> newAdLauncher;
    private final ActivityResultLauncher<Intent> searchLauncher;
    private final ActivityResultLauncher<Intent> settingsLauncher;
    private final ActivityResultLauncher<Intent> messagesLauncher;

    private boolean isMyAds;
    private boolean isBookmarks;
    private boolean isSearch;

    private String searchKeyword;
    private String searchPriceFrom;
    private String searchPriceTo;

    public SectionNavigationController(AppCompatActivity activity,
                                       DrawerLayout drawer,
                                       Button searchAgainButton,
                                       SessionStore session,
                                       AdListController adList,
                                       MessageBadgeController messageBadge,
                                       Runnable startLoginAction,
                                       ActivityResultLauncher<Intent> newAdLauncher,
                                       ActivityResultLauncher<Intent> searchLauncher,
                                       ActivityResultLauncher<Intent> settingsLauncher,
                                       ActivityResultLauncher<Intent> messagesLauncher) {
        this.activity = activity;
        this.drawer = drawer;
        this.searchAgainButton = searchAgainButton;
        this.session = session;
        this.adList = adList;
        this.messageBadge = messageBadge;
        this.startLoginAction = startLoginAction;
        this.newAdLauncher = newAdLauncher;
        this.searchLauncher = searchLauncher;
        this.settingsLauncher = settingsLauncher;
        this.messagesLauncher = messagesLauncher;

        searchAgainButton.setOnClickListener(v -> relaunchLastSearch());
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        final String userId = session.getUserId();
        searchAgainButton.setVisibility(View.GONE);

        int id = item.getItemId();
        if (id == R.id.myads) {
            if (userId.isEmpty()) { startLoginAction.run(); return true; }
            adList.hideEmptyView();
            setMyAdsFlag(true);
            setBookmarksFlag(false);
            isMyAds = true; isBookmarks = false; isSearch = false;
            adList.loadType(Constants.TYPE_USER);
            closeDrawer();
            return true;
        }
        if (id == R.id.new_ad) {
            if (userId.isEmpty()) { startLoginAction.run(); return true; }
            setBookmarksFlag(false);
            Intent i = new Intent(activity, NewAdActivity.class);
            i.putExtra(Constants.USER_ID, userId);
            newAdLauncher.launch(i);
            return true;
        }
        if (id == R.id.search) {
            setBookmarksFlag(false);
            isMyAds = false; isBookmarks = false; isSearch = true;
            searchLauncher.launch(new Intent(activity, SearchActivity.class));
            return true;
        }
        if (id == R.id.settings) {
            setBookmarksFlag(false);
            settingsLauncher.launch(new Intent(activity, SettingsActivity.class));
            return true;
        }
        if (id == R.id.bookmarks) {
            if (userId.isEmpty()) { startLoginAction.run(); return true; }
            adList.hideEmptyView();
            setMyAdsFlag(false);
            setBookmarksFlag(true);
            isBookmarks = true; isMyAds = false; isSearch = false;
            adList.loadType(Constants.TYPE_BOOKMARK);
            closeDrawer();
            return true;
        }
        if (id == R.id.messages_from_user) {
            if (userId.isEmpty()) { startLoginAction.run(); return true; }
            setBookmarksFlag(false);
            messageBadge.hideButton();
            Intent msg = new Intent(activity.getApplicationContext(), MessagesOverviewActivity.class);
            msg.putExtra(Constants.USER_ID, userId);
            messagesLauncher.launch(msg);
            return true;
        }
        if (id == R.id.ebay) {
            // Ebay screen historically reused the settings result contract.
            settingsLauncher.launch(new Intent(activity, EbayActivity.class));
            return true;
        }
        closeDrawer();
        return false;
    }

    /** Returns true if the back press was consumed. */
    public boolean handleBackPressed() {
        searchAgainButton.setVisibility(View.GONE);
        if (drawer != null && drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
            return true;
        }
        if (isBookmarks || isMyAds || isSearch) {
            resetFlags();
            adList.loadType(Constants.TYPE_ALL);
            return true;
        }
        return false;
    }

    /** Callback for the pull-to-refresh gesture in AdListController. */
    public void resetFlags() {
        setMyAdsFlag(false);
        setBookmarksFlag(false);
        isMyAds = false;
        isBookmarks = false;
        isSearch = false;
    }

    /** Store the last search params so the search-again button can re-run them. */
    public void rememberSearch(String keyword, String priceFrom, String priceTo) {
        this.searchKeyword = keyword;
        this.searchPriceFrom = priceFrom;
        this.searchPriceTo = priceTo;
    }

    public void showSearchAgainButton() {
        searchAgainButton.setVisibility(View.VISIBLE);
    }

    public void hideSearchAgainButton() {
        searchAgainButton.setVisibility(View.GONE);
    }

    public void closeDrawer() {
        if (drawer != null) drawer.closeDrawer(GravityCompat.START);
    }

    public void setMyAdsFlag(boolean isMyAds) {
        SharedPreferences prefs = activity.getSharedPreferences(SHOW_MY_ADS, 0);
        prefs.edit().putBoolean(Constants.IS_MY_ADS, isMyAds).apply();
    }

    public void setBookmarksFlag(boolean isBookmarks) {
        SharedPreferences prefs = activity.getSharedPreferences(SHOW_BOOKMARKS, 0);
        prefs.edit().putBoolean(Constants.IS_BOOKMARKS, isBookmarks).apply();
    }

    private void relaunchLastSearch() {
        searchAgainButton.setVisibility(View.GONE);
        Intent i = new Intent(activity, SearchActivity.class);
        i.putExtra(Constants.TITLE, searchKeyword);
        i.putExtra(Constants.PRICE_FROM, searchPriceFrom);
        i.putExtra(Constants.PRICE_TO, searchPriceTo);
        searchLauncher.launch(i);
    }
}
