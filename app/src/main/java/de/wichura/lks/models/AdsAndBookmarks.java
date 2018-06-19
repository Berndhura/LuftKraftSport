package de.wichura.lks.models;

import java.util.ArrayList;

/**
 * Created by ich on 19.10.2016.
 * Luftkraftsport
 */

public class AdsAndBookmarks {

    private ArrayList<Long> bookmarks;
    private AdsAsPage ads;

    public ArrayList<Long> getBookmarks() {
        return bookmarks;
    }

    public void setBookmarks(ArrayList<Long> bookmarks) {
        this.bookmarks = bookmarks;
    }

    public AdsAsPage getAdsPage() {return ads;}

    public void setAds(AdsAsPage ads) {
        this.ads = ads;
    }
}
