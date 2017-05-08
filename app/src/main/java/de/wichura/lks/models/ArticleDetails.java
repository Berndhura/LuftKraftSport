package de.wichura.lks.models;

/**
 * Created by Bernd Wichura on 18.11.2016.
 * Luftkraftsport
 */

public class ArticleDetails {

    private Integer id;
    private String title;
    private String description;
    private String urls;
    private String userId;
    private Float price;
    private Long date;
    private Integer views;
    private Location location;
    private Integer bookmarks;
    private String locationName;


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUrls() {
        return urls;
    }

    public void setUrls(String urls) {
        this.urls = urls;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Float getPrice() {return price;}

    public void setPrice(Float price) {
        this.price = price;
    }

    public Long getDate() {
        return date;
    }

    public void setDate(Long date) {
        this.date = date;
    }

    public Integer getViews() {
        return views;
    }

    public void setViews(Integer views) {
        this.views = views;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Integer getBookmarks() {
        return bookmarks;
    }

    public void setBookmarks(Integer bookmarks) {
        this.bookmarks = bookmarks;
    }

    public String getLocationName() {return locationName;}

    public void setLocationName(String locationName) {this.locationName = locationName;}
}