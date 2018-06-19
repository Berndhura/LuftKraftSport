package de.wichura.lks.models;

import java.util.List;

/**
 * Created by ich on 22.11.2016.
 * Luftkraftsport
 */

public class AdsAsPage {

    public List<RowItem> getAds() {
        return ads;
    }

    public void setAds(List<RowItem> ads) {
        this.ads = ads;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getPages() {
        return pages;
    }

    public void setPages(int pages) {
        this.pages = pages;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    private List<RowItem> ads;
    private int page;
    private int size;
    private int pages;
    private int total;

}
