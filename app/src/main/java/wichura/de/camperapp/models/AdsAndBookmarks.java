package wichura.de.camperapp.models;

import java.util.List;

/**
 * Created by ich on 19.10.2016.
 * CamperAdd
 */

public class AdsAndBookmarks {

    private String bookmarks;
    private List<RowItem> ads;

    public String getBookmarks() {
        return bookmarks;
    }

    public void setBookmarks(String bookmarks) {
        this.bookmarks = bookmarks;
    }

    public List<RowItem> getAds() {
        return ads;
    }

    public void setAds(List<RowItem> ads) {
        this.ads = ads;
    }
}
