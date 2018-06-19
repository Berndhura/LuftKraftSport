package de.wichura.lks.presentation;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import de.wichura.lks.activity.OpenAdActivity;
import de.wichura.lks.http.Service;
import de.wichura.lks.mainactivity.Constants;
import de.wichura.lks.models.ArticleDetails;
import de.wichura.lks.models.User;

import static de.wichura.lks.mainactivity.Constants.SHARED_PREFS_USER_INFO;

/**
 * Created by Bernd Wichura on 13.11.2016.
 * Luftkraftsport
 */

public class OpenAdPresenter {

    private Service service;
    private Context context;
    private OpenAdActivity view;

    public OpenAdPresenter(OpenAdActivity myAdsActivity, Service service, Context applicationContext) {
        this.service = service;
        this.context = applicationContext;
        this.view = myAdsActivity;
    }

    public void loadBookmarksForUser() {
        service.getBookmarksForUserObserv(getUserToken())
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Long[]>() {
                    @Override
                    public void onComplete() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d("CONAN", "error loading bookmarks: " + e.getMessage());
                    }

                    @Override
                    public void onNext(Long[] bookmarks) {
                        view.updateBookmarkButton(bookmarks);
                    }

                    @Override
                    public void onSubscribe(Disposable d) {

                    }
                });
    }

    public void increaseViewCount(Integer adId) {
        service.increaseViewCount(adId)
                .subscribeOn(Schedulers.newThread())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onComplete() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d("CONAN", "error in increase view count: " + e.getMessage());
                    }

                    @Override
                    public void onNext(String result) {
                        Log.d("CONAN", "increase view count: " + result);
                    }

                    @Override
                    public void onSubscribe(Disposable d) {

                    }
                });
    }

    public void bookmarkAd(Integer adId, String userToken) {
        service.bookmarkAdObserv(adId, userToken)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onComplete() {
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

                    @Override
                    public void onSubscribe(Disposable d) {

                    }
                });
    }

    public void deleteBookmark(Integer adId, String userToken) {
        service.delBookmarkAdObserv(adId, userToken)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onComplete() {
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

                    @Override
                    public void onSubscribe(Disposable d) {

                    }
                });
    }

    public void sendNewMessage(String message, Integer adId, String idTo, String userToken) {
        service.sendNewMessageObserv(message, adId, idTo, userToken)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onComplete() {
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

                    @Override
                    public void onSubscribe(Disposable d) {

                    }
                });
    }

    public void deleteAd(Integer adId) {
        service.deleteAdObserv(adId, getUserToken())
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onComplete() {
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

                    @Override
                    public void onSubscribe(Disposable d) {

                    }
                });
    }

    public void getAd(Integer articleId) {
        service.getAdDetailsObserv(articleId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ArticleDetails>() {
                    @Override
                    public void onComplete() {}

                    @Override
                    public void onError(Throwable e) {
                        Log.d("CONAN", "error in getting article details: ");
                    }

                    @Override
                    public void onNext(ArticleDetails articleDetails) {
                        view.prepareDataFromArticle(articleDetails);
                    }

                    @Override
                    public void onSubscribe(Disposable d) {

                    }
                });
    }

    public void getSellerInformation(String userId) {
        service.getSellerInformationObserv(userId, getUserToken())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<User>() {
                    @Override
                    public void onComplete() {}

                    @Override
                    public void onError(Throwable e) {
                        Log.d("CONAN", "error in getting seller details: " + e.getMessage());
                        view.updateSellerInformation(null);
                    }

                    @Override
                    public void onNext(User user) {
                        view.updateSellerInformation(user);
                    }

                    @Override
                    public void onSubscribe(Disposable d) {

                    }
                });
    }

    private String getUserToken() {
        return context.getSharedPreferences(SHARED_PREFS_USER_INFO, 0).getString(Constants.USER_TOKEN, "");
    }
}
