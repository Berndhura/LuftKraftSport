package wichura.de.camperapp.messages;

import com.google.gson.annotations.Expose;

/**
 * Created by ich on 20.06.2016.
 * CamperApp
 */
public class GroupedMsgItem {

    @Expose
    private String adTitle;
    @Expose
    private String name;



    public String getAdTitle() {
        return adTitle;
    }

    public void setAdTitle(String adTitle) {
        this.adTitle = adTitle;
    }

    public String getName() { return name;}

    public void setName(String name) {
        this.name = name;
    }
}
