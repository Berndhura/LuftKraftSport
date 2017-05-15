package de.wichura.lks.presentation;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;

import de.wichura.lks.activity.LoginActivity;
import de.wichura.lks.dialogs.ConfirmWrongLoginDialog;
import de.wichura.lks.dialogs.ShowNetworkProblemDialog;
import de.wichura.lks.dialogs.ShowUserNotActivatedDialog;
import de.wichura.lks.http.Service;
import de.wichura.lks.mainactivity.Constants;
import de.wichura.lks.models.ApiError;
import de.wichura.lks.models.User;
import de.wichura.lks.util.Utility;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Response;
import retrofit2.adapter.rxjava.HttpException;
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

        String hashedPassword = utils.computeSHAHash(password);

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
                        String errorMessage = "";

                        if (e instanceof HttpException) {
                            HttpException exception = (HttpException) e;
                            Response response = exception.response();

                            Converter<ResponseBody, ApiError> errorConverter = service.getErrorConverter();
                            // Convert the error body into our Error type.
                            try {
                                ApiError error = errorConverter.convert(response.errorBody());
                                if ("This user is not activated".equals(error.getMessage())) {
                                    errorMessage = error.getMessage();
                                    Bundle credentials = new Bundle();
                                    credentials.putString("email", email);
                                    //use not hashed password
                                    //after activation password gets hashed again here in login
                                    credentials.putString("password", password);
                                    ShowUserNotActivatedDialog dialog = new ShowUserNotActivatedDialog();
                                    dialog.setArguments(credentials);
                                    dialog.show(loginActivity.getSupportFragmentManager(), null);
                                }
                                //"The user does not exist, or wrong password"
                                else if ("The user does not exist, or wrong password".equals(error.getMessage())) {
                                    errorMessage = error.getMessage();
                                    new ConfirmWrongLoginDialog().show(loginActivity.getSupportFragmentManager(), null);
                                } else {
                                    loginActivity.hideProgressDialog();
                                    new ShowNetworkProblemDialog().show(loginActivity.getSupportFragmentManager(), null);
                                }

                            } catch (IOException e1) {
                                e1.printStackTrace();
                            }
                        }
                        Log.d("CONAN", "error sending login email user: " + errorMessage);
                        loginActivity.hideProgressDialog();
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

    public void sendActivationCode(String email, String password, String code) {

        loginActivity.showProgressDialog();

        service.activateUserObserv(code, email)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<String>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d("CONAN", "error activating email user " + e.getMessage());
                        loginActivity.hideProgressDialog();
                    }

                    @Override
                    public void onNext(String info) {
                        loginActivity.hideProgressDialog();
                        Log.d("CONAN", "activating email user " + info);
                        //TODO falls activating email user invalid ->"info == invalid"
                        //activated, now login with credentials
                        sendLoginReq(email, password);
                    }
                });
    }
}
