package com.example.guitest;

public class RowItem {
	private int imageId;
	private String title;
	private String desc;

	public RowItem(final int imageId, final String title, final String desc) {
		this.imageId = imageId;
		this.title = title;
		this.desc = desc;
	}

	public int getImageId() {
		return imageId;
	}

	public void setImageId(final int imageId) {
		this.imageId = imageId;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(final String desc) {
		this.desc = desc;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(final String title) {
		this.title = title;
	}

	@Override
	public String toString() {
		return title + "\n" + desc;
	}
}
