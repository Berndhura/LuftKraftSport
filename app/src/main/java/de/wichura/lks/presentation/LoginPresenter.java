package de.wichura.lks.presentation;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import de.wichura.lks.activity.LoginActivity;
import de.wichura.lks.http.Service;
import de.wichura.lks.mainactivity.Constants;
import de.wichura.lks.models.User;
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
    private LoginActivity view;

    public LoginPresenter(LoginActivity activity, Service service, Context applicationContext) {
        this.service = service;
        this.context = applicationContext;
        this.view = activity;
    }

    public void sendLoginReq(String email, String password) {
        view.showProgressDialog();
        service.loginUserObserv(email, password)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<User>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d("CONAN", "error sending login email user ");
                        view.hideProgressDialog();
                        //"The user does not exist, or wrong password"
                        //TODO throwable richtig hier? von holger kommt string denke ich
                        if ("HTTP 401 Unauthorized".equals(e.getMessage().toString())) {

                            String info = "Dieser Nutzer ist nicht vorhanden oder es wurde ein falsches Passwort angegeben.";
                            view.showInfo(info);
                        }
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
}
