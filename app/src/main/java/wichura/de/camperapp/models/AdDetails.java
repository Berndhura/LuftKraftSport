package wichura.de.camperapp.models;

/**
 * Created by ich on 18.11.2016.
 * CamperApp
 */

public class AdDetails {

    private String numberOfBookmarks;
    private RowItem ad;

    public String getNumberOfBookmarks() {
        return numberOfBookmarks;
    }

    public void setNumberOfBookmarks(String numberOfBookmarks) {
        this.numberOfBookmarks = numberOfBookmarks;
    }

    public RowItem getAd() {
        return ad;
    }

    public void setAd(RowItem ad) {
        this.ad = ad;
    }


}
