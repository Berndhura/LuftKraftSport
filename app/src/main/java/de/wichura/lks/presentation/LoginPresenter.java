package de.wichura.lks.presentation;

import android.content.Context;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import de.wichura.lks.activity.LoginActivity;
import de.wichura.lks.dialogs.ShowNetworkProblemDialog;
import de.wichura.lks.http.Service;
import de.wichura.lks.mainactivity.Constants;
import de.wichura.lks.models.User;
import de.wichura.lks.util.Utility;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Bernd Wichura on 05.12.2016.
 * Luftkraftsport
 */

public class LoginPresenter {

    private Service service;
    private Context context;
    private LoginActivity loginActivity;
    private Utility utils;

    public LoginPresenter(LoginActivity activity, Service service, Context applicationContext) {
        this.service = service;
        this.context = applicationContext;
        this.loginActivity = activity;
        this.utils = new Utility(activity);
    }

    public void sendLoginReq(String email, String password) {

        String hashedPassword = utils.getHashBase64(password);

        loginActivity.showProgressDialog();
        service.loginUserObserv(email, hashedPassword)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<User>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d("CONAN", "error sending login email user ");
                        loginActivity.hideProgressDialog();
                        //"The user does not exist, or wrong password"
                        if ("HTTP 401 Unauthorized".equals(e.getMessage())) {
                            loginActivity.showInfo();
                        } else {
                            loginActivity.hideProgressDialog();
                            new ShowNetworkProblemDialog().show(loginActivity.getSupportFragmentManager(), null);
                        }
                    }

                    @Override
                    public void onNext(User user) {
                        loginActivity.hideProgressDialog();
                        Toast.makeText(context, "Benutzer " + user.getName() + " angemeldet!", Toast.LENGTH_SHORT).show();
                        Log.d("CONAN", "login email user " + user.getId());
                        //String name, String userId, Uri userPic, String userType, String userToken
                        loginActivity.setUserPreferences(user.getName(), user.getId(), null, Constants.EMAIL_USER, user.getToken());
                        loginActivity.finish();
                    }
                });
    }
}
