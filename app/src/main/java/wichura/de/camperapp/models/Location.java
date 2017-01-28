package wichura.de.camperapp.models;

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
}
