package de.wichura.lks.models;

import com.google.gson.annotations.Expose;

/**
 * Created by ich on 28.01.2017.
 *
 */

public class Location {
    @Expose
    private String type;
    @Expose
    private double[] coordinates;

    public double[] getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(double[] coordinates) {
        this.coordinates = coordinates;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
