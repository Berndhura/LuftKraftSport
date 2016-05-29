package wichura.de.camperapp.messages;

import com.google.gson.annotations.Expose;

/**
 * Created by ich on 29.05.2016.
 *
 */
public class MsgRowItem {

    @Expose
    private String message;
    @Expose
    private String date;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }


    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
