package wichura.de.camperapp.http;

/**
 * Created by bwichura on 13.11.2015.
 *
 */
public class Urls {

    public static final String MAIN_SERVER_URL = "http://ec2-52-32-84-19.us-west-2.compute.amazonaws.com:8080/2ndHandOz/";
    //public static String MAIN_SERVER_URL = "http://10.0.2.2:8080/2ndHandOz/";
    public static final String UPLOAD_NEW_AD_URL = "/saveNewAd/";
    public static final String DELETE_AD_WITH_APID = "/deleteAdWithId/";
    public static final String GET_ALL_ADS_URL = "getAllAds";
    public static final String GET_ADS_FOR_KEYWORD_URL = "/getAdsWithTag?description=";
    public static final String GET_AD = "getAd";
    public static final String GET_ALL_ADS_FROM_USER  = "/getAdsFromUser?userid=";
    public static final String BOOKMARK_AD = "/bookmark";
    public static final String BOOKMARK_DELETE = "/deleteBookmark";
    public static final String GET_BOOKMARKED_ADS_URL = "getMyBookmarkedAds?userId=";
    public static final String LOGIN_USER = "/loginUser";
    public static final String COUNT_VIEW = "viewCount";
    public static final String CREATE_USER = "/createUser";
    public static final String SEND_MESSAGE = "saveMessage";
    public static final String SEND_TOKEN_FOR_GCM = "/sendToken";
    public static final String GET_ALL_MESSAGES_FOR_USER = "/getAllMessagesForUser";
    public static final String GET_ALL_MESSAGES_FOR_AD ="getAllMessagesForAd";
    public static final String GET_BOOKMARKS_FOR_USER ="getMyBookmarks";
}

