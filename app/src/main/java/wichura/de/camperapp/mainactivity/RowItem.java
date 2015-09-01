package wichura.de.camperapp.mainactivity;

import android.graphics.Bitmap;

import com.google.gson.annotations.Expose;

public class RowItem {
	private int imageId;
	@Expose
	private String title;
	@Expose
	private String keywords;
	@Expose
	private String urls;
	@Expose
	private String description;
	@Expose
	private String phone;
	@Expose
	private String date;
	@Expose
	private String price;

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	@Expose
	private  String location;

	private Bitmap image;

	public RowItem(final int imageId,
                   final String title,
			       final String keywords,
                   final String url,
                   final String des,
                   final String phone,
                   final String date,
                   final String price,
				   final String location) {
		this.imageId = imageId;
		this.title = title;
		this.keywords = keywords;
		this.urls = url;
		this.description = des;
        this.phone = phone;
        this.date = date;
        this.price = price;
		this.location = location;
	}

	public String getDate() {return date;}

	public void setDate(String date) {this.date = date;}

	public String getPrice() {return price;}

	public void setPrice(String price) {this.price = price;}

	public String getPhone() {return phone;}

	public void setPhone(String phone) {this.phone = phone;}

	public String getDescription() {
		return description;
	}

	public void setDescription(final String description) {
		this.description = description;
	}

	public Bitmap getImage() {
		return image;
	}

	public int getImageId() {
		return imageId;
	}

	public void setImageId(final int imageId) {
		this.imageId = imageId;
	}

	public String getKeywords() {
		return keywords;
	}

	public void setKeywords(final String keyw) {
		this.keywords = keyw;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(final String title) {
		this.title = title;
	}

	public String getUrl() {
		return urls;
	}

	public void setUrl(final String url) {this.urls = url;}
}