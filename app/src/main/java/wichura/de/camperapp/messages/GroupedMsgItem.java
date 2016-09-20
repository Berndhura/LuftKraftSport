package wichura.de.camperapp.messages;

import com.google.gson.annotations.Expose;

/**
 * Created by ich on 20.06.2016.
 * CamperApp
 */
public class GroupedMsgItem {

    @Expose
    private String message;
    @Expose
    private String name;
    @Expose
    private String url;

    public String getUrl() { return url; }

    public String getMessage() {
        return message;
    }

    public String getName() { return name;}

    public void setName(String name) {
        this.name = name;
    }
}
