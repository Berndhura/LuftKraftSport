package wichura.de.camperapp.models;

/**
 * Created by ich on 18.11.2016.
 * CamperApp
 */

public class ArticleDetails {

    private String numberOfBookmarks;
    private RowItem article;

    public String getNumberOfBookmarks() {
        return numberOfBookmarks;
    }

    public void setNumberOfBookmarks(String numberOfBookmarks) {
        this.numberOfBookmarks = numberOfBookmarks;
    }

    public RowItem getArticle() {
        return article;
    }

    public void setAd(RowItem article) {
        this.article = article;
    }


}
