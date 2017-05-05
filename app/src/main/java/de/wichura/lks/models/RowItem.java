package de.wichura.lks.models;

import com.google.gson.annotations.Expose;

public class RowItem {
    @Expose
    private String title;
    @Expose
    private String urls;
    @Expose
    private String description;
    @Expose
    private String phone;
    @Expose
    private long date;
    @Expose
    private Float price;
    @Expose
    private Integer id;
    @Expose
    private Location location;
    @Expose
    private String userId;
    @Expose
    private String views;
    @Expose
    private String bookmarks;
    @Expose
    private String[] pictureIds;
    @Expose
    private Integer distance;
    @Expose
    private String locationName;


    public RowItem() {
    }

    public RowItem(final String title,
                   final Integer id,
                   final String url,
                   final String des,
                   final String phone,
                   final long date,
                   final Float price,
                   final Location location,
                   final String userid,
                   final String views,
                   final String bookmarks,
                   final String[] pictureIds) {
        this.title = title;
        this.id = id;
        this.urls = url;
        this.description = des;
        this.phone = phone;
        this.date = date;
        this.price = price;
        this.location = location;
        this.userId = userid;
        this.views = views;
        this.bookmarks = bookmarks;
        this.pictureIds = pictureIds;
    }

    public String getBookmarks() {
        return bookmarks;
    }

    public String getViews() {
        return views;
    }

    public String getUserId() {
        return userId;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location loc) {
        this.location = loc;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public String getPrice() {
        return price.toString();
    }

    public void setPrice(Float price) {
        this.price = price;
    }

    public String getPhone() {
        return phone;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
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

    public Integer getDistance() {
        return distance;
    }

    public void setDistance(Integer distance) {
        this.distance = distance;
    }

    public String[] getPictureIds() {
        return pictureIds;
    }

    public void setPictureIds(String[] pictureIds) {
        this.pictureIds = pictureIds;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }
}