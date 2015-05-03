package com.example.guitest;

import android.content.Intent;

public class AdItem {

	public static final String ITEM_SEP = System.getProperty("line.separator");

	public final static String TITLE = "title";
	public final static String FILENAME = "";
	public final static String DESC = "description";

	private String mTitle = new String();
	private String mDesc = new String();
	private String mFilename = new String();

	AdItem(final String title, final String desc, final String fileName) {
		this.mTitle = title;
		this.mDesc = desc;
		this.mFilename = fileName;

	}

	// Create a new ToDoItem from data packaged in an Intent

	AdItem(final Intent intent) {

		mTitle = intent.getStringExtra(AdItem.TITLE);
		mDesc = intent.getStringExtra(AdItem.DESC);
		mFilename = intent.getStringExtra(AdItem.FILENAME);

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

	// Take a set of String data values and
	// package them for transport in an Intent

	public static void packageIntent(final Intent intent, final String title,
			final String desc, final String fileURI) {

		intent.putExtra(AdItem.TITLE, title);
		intent.putExtra(AdItem.DESC, desc);
		intent.putExtra(AdItem.FILENAME, fileURI);

	}

	@Override
	public String toString() {
		return mTitle + ITEM_SEP + mDesc;
	}

	public String toLog() {
		return "Title:" + mTitle + "Description:" + mDesc;
	}
}
