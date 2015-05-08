package de.wichura.camperapp.mainactivity;

public class RowItem {
	private int imageId;
	private String title;
	private String desc;
	private byte[] image;

	public RowItem(final int imageId, final String title, final String desc,
			final byte[] image) {
		this.imageId = imageId;
		this.title = title;
		this.desc = desc;
		this.image = image;
	}

	public byte[] getImage() {
		return image;
	}

	public void setImage(final byte[] image) {
		this.image = image;
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
