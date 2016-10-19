package wichura.de.camperapp.models;

import android.content.Intent;

public class MyAdsRowItem {

    public static final String ITEM_SEP = System.getProperty("line.separator");

    //todo constants
    public final static String TITLE = "title";
    public final static String ADID = "ad_id";
    public final static String FILENAME = "";
    public final static String DESC = "description";
    public final static String KEYWORDS = "keywords";
    public final static String LOCATION = "location";
    public final static String PHONE = "phone";
    public final static String PRICE = "price";
    public final static String DATE = "date";

    private String mTitle = new String();
    private String mApId = new String();
    private String mDesc = new String();
    private String mKeywords = new String();
    private String location = new String();
    private String phone = new String();
    private String mFilename = new String();
    private String mPrice = new String();
    private long mDate;

    MyAdsRowItem(final String title, final String apid, final String desc, final String keyw,
                 final String fileName, final String location, final String phone, final String price,
                 final long date) {
        this.mTitle = title;
        this.mApId = apid;
        this.mDesc = desc;
        this.mKeywords = keyw;
        this.mFilename = fileName;
        this.location = location;
        this.phone = phone;
        this.mPrice = price;
        this.mDate = date;

    }

    public MyAdsRowItem(final Intent intent) {

        mTitle = intent.getStringExtra(MyAdsRowItem.TITLE);
        mApId = intent.getStringExtra(MyAdsRowItem.ADID);
        mDesc = intent.getStringExtra(MyAdsRowItem.DESC);
        mFilename = intent.getStringExtra(MyAdsRowItem.FILENAME);
        mKeywords = intent.getStringExtra(MyAdsRowItem.KEYWORDS);
        location = intent.getStringExtra((MyAdsRowItem.LOCATION));
        phone = intent.getStringExtra(MyAdsRowItem.PHONE);
        mPrice = intent.getStringExtra(MyAdsRowItem.PRICE);
        mDate = intent.getLongExtra(MyAdsRowItem.DATE, 0);
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(final String title) {
        mTitle = title;
    }

    public String getDesc() {
        return mDesc;
    }

    public void setDesc(final String desc) {
        mDesc = desc;
    }

    public String getKeywords() {
        return mKeywords;
    }

    public void setKeywords(final String keyw) {
        mKeywords = keyw;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
    // Take a set of String data values and
    // package them for transport in an Intent

    public static void packageIntent(final Intent intent, final String title, final String apid,
                                     final String desc, final String keyw, final String fileURI, final String location,
                                     final String phone, final String price, final long date) {

        intent.putExtra(MyAdsRowItem.TITLE, title);
        intent.putExtra(MyAdsRowItem.ADID, apid);
        intent.putExtra(MyAdsRowItem.DESC, desc);
        intent.putExtra(MyAdsRowItem.KEYWORDS, keyw);
        intent.putExtra(MyAdsRowItem.FILENAME, fileURI);
        intent.putExtra(MyAdsRowItem.LOCATION, location);
        intent.putExtra(MyAdsRowItem.PHONE, phone);
        intent.putExtra(MyAdsRowItem.PRICE, price);
        intent.putExtra(MyAdsRowItem.DATE, date);

    }

    @Override
    public String toString() {
        return mTitle + ITEM_SEP + mDesc + ITEM_SEP + mKeywords;
    }

}
