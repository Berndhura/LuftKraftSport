package wichura.de.camperapp.ad;

import android.content.Intent;

public class AdItem {

	public static final String ITEM_SEP = System.getProperty("line.separator");

	public final static String TITLE = "title";
	public final static String FILENAME = "";
	public final static String DESC = "description";
	public final static String KEYWORDS = "keywords";
	public final static String LOCATION = "location";
    public final static String PHONE = "phone";

	private String mTitle = new String();
	private String mDesc = new String();
	private String mKeywords = new String();
	private String location = new String();
    private String phone = new String();
	private String mFilename = new String();

	AdItem(final String title, final String desc, final String keyw,
			final String fileName, final String location, final String phone) {
		this.mTitle = title;
		this.mDesc = desc;
		this.mKeywords = keyw;
		this.mFilename = fileName;
		this.location = location;
        this.phone = phone;

	}

	public AdItem(final Intent intent) {

		mTitle = intent.getStringExtra(AdItem.TITLE);
		mDesc = intent.getStringExtra(AdItem.DESC);
		mFilename = intent.getStringExtra(AdItem.FILENAME);
		mKeywords = intent.getStringExtra(AdItem.KEYWORDS);
		location = intent.getStringExtra((AdItem.LOCATION));
        phone = intent.getStringExtra(AdItem.PHONE);
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

	public String getLocation() {return location;}

	public void setLocation(String location) {this.location = location;	}
	// Take a set of String data values and
	// package them for transport in an Intent

	public static void packageIntent(final Intent intent, final String title,
			final String desc, final String keyw, final String fileURI, final String location,
                                     final String phone) {

		intent.putExtra(AdItem.TITLE, title);
		intent.putExtra(AdItem.DESC, desc);
		intent.putExtra(AdItem.KEYWORDS, keyw);
		intent.putExtra(AdItem.FILENAME, fileURI);
		intent.putExtra(AdItem.LOCATION, location);
        intent.putExtra(AdItem.PHONE, phone);

	}

	@Override
	public String toString() {
		return mTitle + ITEM_SEP + mDesc + ITEM_SEP + mKeywords;
	}

	public String toLog() {
		return "Title:" + mTitle + "Description:" + mDesc;
	}
}
