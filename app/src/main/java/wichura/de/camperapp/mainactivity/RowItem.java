package wichura.de.camperapp.mainactivity;

import com.google.gson.annotations.Expose;

public class RowItem {
    private int imageId;
    @Expose
    private String title;
    @Expose
    private String keywords;
    @Expose
    private String urls;
    @Expose
    private String description;
    @Expose
    private String phone;
    @Expose
    private String date;
    @Expose
    private String price;
    @Expose
    private String id;
    @Expose
    private String location;
    @Expose
    private String userid;

    public RowItem(final int imageId,
                   final String title,
                   final String id,
                   final String keywords,
                   final String url,
                   final String des,
                   final String phone,
                   final String date,
                   final String price,
                   final String location,
                   final String userid) {
        this.imageId = imageId;
        this.title = title;
        this.id = id;
        this.keywords = keywords;
        this.urls = url;
        this.description = des;
        this.phone = phone;
        this.date = date;
        this.price = price;
        this.location = location;
        this.userid = userid;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getAdId() {return id; }

    public void setId(String id) {this.id = id;}

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(final String keyw) {
        this.keywords = keyw;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public String getUrl() {
        return urls;
    }

    public void setUrl(final String url) {
        this.urls = url;
    }
}