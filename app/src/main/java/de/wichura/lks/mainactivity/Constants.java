package de.wichura.lks.mainactivity;

/**
 * Created by Bernd Wichura on 22.01.2016.
 * Luftkraftsport
 */
public class Constants {


    private Constants() {}

    public static int MAX_IMAGE_SIZE = 5;

    public static final int REQUEST_ID_FOR_NEW_AD = 1;
    public static final int REQUEST_ID_FOR_LOGIN = 2;
    public static final int REQUEST_ID_FOR_OPEN_AD = 3;
    public static final int REQUEST_ID_FOR_SEARCH = 5;
    public static final int REQUEST_ID_FOR_MESSAGES = 6;
    public static final int REQUEST_ID_FOR_SETTINGS = 7;
    public static final int REQUEST_ID_FOR_SEARCHES = 8;
    public static final int REQUEST_ID_FOR_LOCATION_PERMISSION = 9;
    public static final int REQUEST_ID_FOR_FILE_PERMISSION = 10;
    public static final int RC_SIGN_IN = 11;
    public static final int REQUEST_ID_FOR_REGISTER_USER = 12;
    public static final int REQUEST_ID_FOR_ACTIVATE_USER = 13;
    public static final int REQUEST_ID_FOR_MAINACTIVITY = 14;
    public static final int REQUEST_ID_FOR_FOLLOW_SEARCH = 15;

    public static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    public static final String FACEBOOK_USER = "facebook_user";

    public static final String USER_ID = "user_id";
    public static final String USER_NAME = "user_name";
    public static final String USER_PICTURE = "user_picture";
    public static final String USER_TYPE = "user_type";
    public static final String USER_TOKEN = "user_token";
    public static final String LAST_LOCATION_NAME = "last_location_name";

    public static final String GOOGLE_USER = "google_user";
    public static final String EMAIL_USER = "email_user";

    //Intent
    public static final String URI = "uri";
    public static final String URI_AS_LIST = "uri_as_list";
    public static final String TITLE = "title";
    public static final String LOCATION_NAME = "locationName";
    public static final String IMAGE = "image";
    public static final String DESCRIPTION = "description";
    public static final String LOCATION = "location";
    public static final String LAT = "lat";
    public static final String LNG = "lng";
    public static final String PHONE = "phone";
    public static final String PRICE = "price";
    public static final String DATE = "date";
    public static final String VIEWS = "views";
    public static final String USER_ID_FROM_AD ="user_id_from_ad";
    public static final String KEYWORDS = "keywords";
    public static final String AD_URL = "adURL";
    public static final String PRICE_FROM = "price_from";
    public static final String PRICE_TO = "price_to";
    public static final String DISTANCE = "distance";
    public static final String FILENAME = "filename";
    public static final String ID = "id";
    public static final String POSITION_IN_LIST = "positionInList";
    public static final String IS_EDIT_MODE = "isEditMode";
    public static final String PERMISSION_DENIED = "permissionDenied";

    //Broadcast messages
    public static final String LOGIN_COMPLETE = "loginComplete";
    public static final String IS_MY_ADS ="isMyAds";
    public static final String IS_BOOKMARKS ="isBookmarks";
    public static final String SENDER_ID = "sender";
    public static final String SENDER_NAME = "senderName";
    public static final String ID_FROM = "idFrom";
    public static final String CHAT_PARTNER = "chatPartner";
    public static final String ID_TO = "idTo";
    public static final String ARTICLE_ID = "articleId";
    public static final String MESSAGE = "message";
    public static final String NOTIFICATION_TYPE = "notification_type";

    //Shared Preferences
    public static final String MESSAGE_ACTIVITY = "messageActivity";
    public static final String SHOW_MY_ADS = "showMyAds";
    public static final String SHOW_BOOKMARKS = "showBookmarks";
    public static final String SHARED_PREFS_USER_INFO = "UserInfo";
    public static final String SHARED_PREFS_LOCATION = "lastLoacation";
    public static final String USERS_LOCATION = "usersLocation";
    public static final String LOCATION_SERVICE_IS_ENABLED = "locationServiceIsEnabled";
    public static final String USER_PRICE_RANGE = "usersPriceRange";
    public static final String ACTIVATE_USER_STATUS = "usersActivate";
    public static final String UNREAD_MESSAGES = "unreadMessages";
    public static final String SHARED_PREFS_WELCOME_DIALOG = "welcomeDialog";
    public static final String SHOW_WELCOME = "showWelcome";
    public static final String LAST_SEARCH = "lastSearch";

    public static final String ACTIVATE_USER = "isUserActivated";
    public static final String REGISTER_USER = "isUserRegistered";

    //Google
    public static final String WEB_CLIENT_ID = "225684928245-21lot3bitst9q7te84fq0kcc1bel3pl7.apps.googleusercontent.com";

    //Type of Ads to Get
    public static final String TYPE_BOOKMARK = "getBookmarks";
    public static final String TYPE_ALL = "getAll";
    public static final String TYPE_USER = "getUser";
    public static final String TYPE_SEARCH = "getSearch";
    public static final String TYPE_USER_WITH_ID = "getUserWithId";

    public static final Integer DISTANCE_INFINITY = 10000000;
    public static final Integer MAX_PRICE = 10000000;
}
