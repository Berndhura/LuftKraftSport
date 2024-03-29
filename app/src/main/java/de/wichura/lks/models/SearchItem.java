package de.wichura.lks.models;

/**
 * Created by bernd wichura on 07.02.2017.
 * Luftkraftsport
 */

public class SearchItem {


    private Long id;

    private String description;

    private Integer distance;

    private Double lat;

    private Double lng;

    private Integer priceFrom;

    private Integer priceTo;

    private Integer resultCount;

    private String locationName;

    public String getLocationName() {return locationName;}

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getDistance() {return distance;}

    public void setDistance(Integer distance) {
        this.distance = distance;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLng() {
        return lng;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }

    public Integer getPriceFrom() {
        return priceFrom;
    }

    public void setPriceFrom(Integer priceFrom) {
        this.priceFrom = priceFrom;
    }

    public Integer getPriceTo() {return priceTo;}

    public void setPriceTo(Integer priceTo) {
        this.priceTo = priceTo;
    }

    public Integer getResultCount() {
        return resultCount;
    }

    public void setResultCount(Integer resultCount) {
        this.resultCount = resultCount;
    }
}
