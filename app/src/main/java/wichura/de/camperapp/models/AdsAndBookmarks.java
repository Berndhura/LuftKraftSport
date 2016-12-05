package wichura.de.camperapp.models;

import java.util.ArrayList;

/**
 * Created by ich on 19.10.2016.
 * CamperAdd
 */

public class AdsAndBookmarks {

    private ArrayList<String> bookmarks;
    private AdsAsPage ads;

    public ArrayList<String> getBookmarks() {
        return bookmarks;
    }

    public void setBookmarks(ArrayList<String> bookmarks) {
        this.bookmarks = bookmarks;
    }

    public AdsAsPage getAds() {return ads;}

    public void setAds(AdsAsPage ads) {
        this.ads = ads;
    }
}
