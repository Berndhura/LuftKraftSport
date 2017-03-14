package de.wichura.lks.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import de.wichura.lks.R;
import de.wichura.lks.http.Service;

/**
 * Created by ich on 24.02.2017.
 * blue ground
 */

public class ActivateUserActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activate_user_activity);
        findViewById(R.id.activation_user_button).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (R.id.activation_user_button) {
            case R.id.activation_user_button: {
                sendActivationCode();
                break;
            }
        }
    }

    private void sendActivationCode() {
        Service service = new Service();
        service.activateUserObserv("43116", "wichura@gmx.de")
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<String>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d("CONAN", "error activate email user " + e.getMessage());
                        //view.finish();
                    }

                    @Override
                    public void onNext(String info) {
                        //view.hideProgressDialog();
                        //view.finish();
                        Log.d("CONAN", "activate email user " + info);
                        setResult(RESULT_OK, null);
                        finish();
                    }
                });
    }
}
