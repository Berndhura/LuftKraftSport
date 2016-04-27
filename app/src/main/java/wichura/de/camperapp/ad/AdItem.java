package wichura.de.camperapp.ad;

import android.content.Intent;

public class AdItem {

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

    AdItem(final String title, final String apid, final String desc, final String keyw,
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

    public AdItem(final Intent intent) {

        mTitle = intent.getStringExtra(AdItem.TITLE);
        mApId = intent.getStringExtra(AdItem.ADID);
        mDesc = intent.getStringExtra(AdItem.DESC);
        mFilename = intent.getStringExtra(AdItem.FILENAME);
        mKeywords = intent.getStringExtra(AdItem.KEYWORDS);
        location = intent.getStringExtra((AdItem.LOCATION));
        phone = intent.getStringExtra(AdItem.PHONE);
        mPrice = intent.getStringExtra(AdItem.PRICE);
        mDate = intent.getLongExtra(AdItem.DATE, 0);
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

        intent.putExtra(AdItem.TITLE, title);
        intent.putExtra(AdItem.ADID, apid);
        intent.putExtra(AdItem.DESC, desc);
        intent.putExtra(AdItem.KEYWORDS, keyw);
        intent.putExtra(AdItem.FILENAME, fileURI);
        intent.putExtra(AdItem.LOCATION, location);
        intent.putExtra(AdItem.PHONE, phone);
        intent.putExtra(AdItem.PRICE, price);
        intent.putExtra(AdItem.DATE, date);

    }

    @Override
    public String toString() {
        return mTitle + ITEM_SEP + mDesc + ITEM_SEP + mKeywords;
    }

}
