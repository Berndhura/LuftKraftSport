package wichura.de.camperapp.models;

import com.google.gson.annotations.Expose;

/**
 * Created by ich on 29.05.2016.
 * Camper App
 */
public class MsgRowItem {

    public MsgRowItem(String msg) {
        this.message = msg;
        this.date = System.currentTimeMillis();
    }

    @Expose
    private String message;
    @Expose
    private long date;
    @Expose
    private String idFrom;
    @Expose
    private String chatPartner;

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

    public String getChatPartner() {return chatPartner;}

    public void setChatPartner(String chatPartner) {this.chatPartner = chatPartner;}
}
