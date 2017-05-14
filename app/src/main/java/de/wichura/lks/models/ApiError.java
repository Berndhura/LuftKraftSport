package de.wichura.lks.models;

/**
 * Created Bernd Wichura ich on 14.05.2017.
 * Luftkraftsport
 */

public class ApiError {

    private int statusCode;
    private String message;

    public ApiError() {
    }

    public int getStatus() {
        return statusCode;
    }

    public String getMessage() {
        return message;
    }
}
