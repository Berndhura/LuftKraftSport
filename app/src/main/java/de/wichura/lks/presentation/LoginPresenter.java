package de.wichura.lks.presentation;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import de.wichura.lks.activity.LoginActivity;
import de.wichura.lks.http.Service;
import de.wichura.lks.mainactivity.Constants;
import de.wichura.lks.models.User;

/**
 * Created by ich on 05.12.2016.
 * Camper App
 */

public class LoginPresenter {

    private Service service;
    private Context context;
    private LoginActivity view;

    public LoginPresenter(LoginActivity acitivity, Service service, Context applicationContext) {
        this.service = service;
        this.context = applicationContext;
        this.view = acitivity;
    }

    public void sendLoginReq(String email, String password) {
        view.showProgressDialog();
        service.loginUserObserv(email, password)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<User>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d("CONAN", "error sending login email user " + e.getMessage());
                        view.finish();
                    }

                    @Override
                    public void onNext(User user) {
                        view.hideProgressDialog();
                        Toast.makeText(context, "Benutzer " + user.getName() + " angemeldet!", Toast.LENGTH_SHORT).show();
                        Log.d("CONAN", "login email user " + user.getId());
                        //String name, String userId, Uri userPic, String userType, String userToken
                        view.setUserPreferences(user.getName(), user.getId().toString(), null, Constants.EMAIL_USER, user.getToken());
                        view.finish();
                    }
                });
    }

    public String registerUser(String email, String password) {
        view.showProgressDialog();
        service.registerUserObserv(email, password, "john joop")
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<String>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d("CONAN", "error sending login email user " + e.getMessage());
                        view.finish();
                    }

                    @Override
                    public void onNext(String info) {
                        view.hideProgressDialog();
                        view.finish();

                    }
                });
        return "ok";
    }
}
