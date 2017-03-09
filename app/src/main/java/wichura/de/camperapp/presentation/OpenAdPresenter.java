package wichura.de.camperapp.presentation;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import wichura.de.camperapp.activity.OpenAdActivity;
import wichura.de.camperapp.http.Service;
import wichura.de.camperapp.mainactivity.Constants;
import wichura.de.camperapp.models.ArticleDetails;
import wichura.de.camperapp.models.User;

import static wichura.de.camperapp.mainactivity.Constants.SHARED_PREFS_USER_INFO;

/**
 * Created by ich on 13.11.2016.
 * CamperApp
 */

public class OpenAdPresenter {

    private Service service;
    private Context context;
    private OpenAdActivity view;
    private Subscription subscription;

    public OpenAdPresenter(OpenAdActivity myAdsActivity, Service service, Context applicationContext) {
        this.service = service;
        this.context = applicationContext;
        this.view = myAdsActivity;
    }

    public void loadBookmarksForUser() {
        service.getBookmarksForUserObserv(getUserToken())
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Long[]>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d("CONAN", "error loading bookmarks: " + e.getMessage());
                    }

                    @Override
                    public void onNext(Long[] bookmarks) {
                        view.updateBookmarkButton(bookmarks);
                    }
                });
    }

    public void increaseViewCount(Integer adId) {
        service.increaseViewCount(adId)
                .subscribeOn(Schedulers.newThread())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<String>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d("CONAN", "error in increase view count: " + e.getMessage());
                    }

                    @Override
                    public void onNext(String result) {
                        Log.d("CONAN", "increase view count: " + result);
                    }
                });
    }

    public void bookmarkAd(Integer adId, String userToken) {
        service.bookmarkAdObserv(adId, userToken)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<String>() {
                    @Override
                    public void onCompleted() {
                        Toast.makeText(view.getApplicationContext(), "Artikel ist gemerkt", Toast.LENGTH_SHORT).show();
                        view.mBookmarkButton.setText("Vergessen");
                        view.isBookmarked = true;
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d("CONAN", "error in bookmark ad: " + e.getMessage());
                    }

                    @Override
                    public void onNext(String result) {
                        Log.d("CONAN", "bookmark ad: " + result);
                    }
                });
    }

    public void deleteBookmark(Integer adId, String userToken) {
        service.delBookmarkAdObserv(adId, userToken)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<String>() {
                    @Override
                    public void onCompleted() {
                        Toast.makeText(view.getApplicationContext(), "von der Merkliste gelöscht!", Toast.LENGTH_SHORT).show();
                        view.mBookmarkButton.setText("Merken");
                        view.isBookmarked = false;
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d("CONAN", "error in remove bookmark: " + e.getMessage());
                    }

                    @Override
                    public void onNext(String result) {
                        Log.d("CONAN", "bookmark deleted: " + result);
                    }
                });
    }

    public void sendNewMessage(String message, Integer adId, String idTo, String userToken) {
        service.sendNewMessageObserv(message, adId, idTo, userToken)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<String>() {
                    @Override
                    public void onCompleted() {
                        Toast.makeText(view.getContext(), "Nachricht gesendet...", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d("CONAN", "error in sending message: " + e.getMessage());
                    }

                    @Override
                    public void onNext(String result) {
                        Log.d("CONAN", "send message to user: " + result);
                    }
                });
    }

    public void deleteAd(Integer adId) {
        service.deleteAdObserv(adId, getUserToken())
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<String>() {
                    @Override
                    public void onCompleted() {
                        Toast.makeText(view.getApplicationContext(), "Artikel gelöscht!", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d("CONAN", "error in deleting ad: " + e.getMessage());
                    }

                    @Override
                    public void onNext(String result) {
                        Log.d("CONAN", "delete ad: " + result);
                    }
                });
    }

    public void getAd(Integer articleId) {
        service.getAdDetailsObserv(articleId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<ArticleDetails>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d("CONAN", "error in getting article details: " + e.getMessage());
                        Log.e("CONAN", "presenter ERROR" + e.getMessage());
                    }

                    @Override
                    public void onNext(ArticleDetails articleDetails) {
                        view.prepareDataFromArticle(articleDetails);
                    }
                });
    }

    public void getSellerInformation(String userId) {
       /* service.getSellerInformationObserv(userId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<User>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d("CONAN", "error in getting seller details: " + e.getMessage());
                        User user = new User();
                        user.setName("Conan Superstar");
                        view.updateSellerInformation(user);
                    }

                    @Override
                    public void onNext(User user) {
                        view.updateSellerInformation(user);
                    }
                });*/
        User user = new User();
        user.setName("Conan Superstar");
        view.updateSellerInformation(user);
    }

    private String getUserToken() {
        return context.getSharedPreferences(SHARED_PREFS_USER_INFO, 0).getString(Constants.USER_TOKEN, "");
    }
}
