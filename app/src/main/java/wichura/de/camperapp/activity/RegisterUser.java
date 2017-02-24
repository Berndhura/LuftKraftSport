package wichura.de.camperapp.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import wichura.de.camperapp.R;
import wichura.de.camperapp.http.Service;

/**
 * Created by bwichura on 24.02.2017.
 * blue ground
 */

public class RegisterUser extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.register_user_activity);

        findViewById(R.id.register_user_button).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.register_user_button:
                registerUser();
                Log.d("CONAN", "sending...");
                break;
        }
    }

    private void registerUser() {
        Service service = new Service();
        service.registerUserObserv("farthole", "wichura@gmx.de", "john joop")
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<String>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d("CONAN", "error registering email user " + e.getMessage());
                        //view.finish();
                    }

                    @Override
                    public void onNext(String info) {
                        //view.hideProgressDialog();
                        //view.finish();
                        Log.d("CONAN", "registering email user " + info);
                    }
                });
    }
}
