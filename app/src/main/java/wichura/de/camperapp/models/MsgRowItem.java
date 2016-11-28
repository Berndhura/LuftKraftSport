package wichura.de.camperapp.models;

import com.google.gson.annotations.Expose;

/**
 * Created by ich on 29.05.2016.
 * Camper App
 */
public class MsgRowItem {

    public MsgRowItem(String msg) {
        this.message = msg;
        this.date = 123123123;
    }

    @Expose
    private String message;
    @Expose
    private long date;
    @Expose
    private String idFrom;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSender() {
        return idFrom;
    }

    public void setSender(String idFrom) {
        this.idFrom = idFrom;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

}