package wichura.de.camperapp.presentation;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import wichura.de.camperapp.activity.LoginActivity;
import wichura.de.camperapp.http.Service;
import wichura.de.camperapp.mainactivity.Constants;

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
                    public void onNext(String result) {
                        view.hideProgressdialog();
                        if (!result.equals("wrong")) {
                            Toast.makeText(context, "User in", Toast.LENGTH_SHORT).show();
                            //get userid and back to mainActiv
                            String[] userInfos = result.split(",");
                            //userToken is open -> null
                            view.setUserPreferences(userInfos[1], userInfos[0], null, Constants.EMAIL_USER, null);
                            view.finish();
                        } else {
                            Toast.makeText(context, "Wrong user or password. Try again!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
