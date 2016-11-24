package wichura.de.camperapp.models;

/**
 * Created by ich on 19.10.2016.
 * CamperAdd
 */

public class AdsAndBookmarks {

    private String bookmarks;
    private AdsAsPage ads;

    public String getBookmarks() {
        return bookmarks;
    }

    public void setBookmarks(String bookmarks) {
        this.bookmarks = bookmarks;
    }

    public AdsAsPage getAds() {return ads;}

    public void setAds(AdsAsPage ads) {
        this.ads = ads;
    }
}
