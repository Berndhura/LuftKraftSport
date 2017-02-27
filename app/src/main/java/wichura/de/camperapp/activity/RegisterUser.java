package wichura.de.camperapp.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import wichura.de.camperapp.R;
import wichura.de.camperapp.http.Service;

/**
 * Created by bwichura on 24.02.2017.
 * blue ground
 */


public class RegisterUser extends AppCompatActivity  {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.register_user_activity);

        findViewById(R.id.register_user_button).setOnClickListener(v -> {
            registerUser();
            Log.d("CONAN", "sending...");
        });
    }

    private void registerUser() {
        ((TextView)findViewById(R.id.register_user_info_box)).setText("Registriere Konto...");
        Service service = new Service();

        String name = ((TextView)findViewById(R.id.register_user_name)).getText().toString();
        String email = ((TextView)findViewById(R.id.register_user_email)).getText().toString();
        String password = ((TextView)findViewById(R.id.register_user_password)).getText().toString();

        service.registerUserObserv(name, email, password)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<String>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d("CONAN", "error registering email user " + e.getMessage());
                        ((TextView)findViewById(R.id.register_user_info_box)).setText("Es gab ein Problem" +
                                " das Konto anzulegen. Versuche es nochmal. ");
                    }

                    @Override
                    public void onNext(String info) {
                        ((TextView)findViewById(R.id.register_user_info_box)).setText("Neues Konto wurde angelegt!" +
                                " Ein Aktivierungscode wurde dir an deine Email gesendet. Bitte gib diesen ein, um deine" +
                                " Kontoaktivierung abzuschliessen!");
                        Log.d("CONAN", "registering email user " + info);
                        // setResult(RESULT_OK, null);
                        //finish();

                        adaptViewForActivation();
                    }
                });
    }

    private void adaptViewForActivation() {

        findViewById(R.id.register_user_password).setVisibility(View.GONE);
        ((TextView)findViewById(R.id.register_user_name)).setHint("Aktivierungscode");
        ((Button)findViewById(R.id.register_user_button)).setText("Sende Aktiverungscode");
        ((TextView)findViewById(R.id.register_user_name)).setText("");

        findViewById(R.id.register_user_button).setOnClickListener(v -> {
            activateUser();
            Log.d("CONAN", "activating...");
        });
    }

    private void activateUser() {
        ((TextView)findViewById(R.id.register_user_info_box)).setText("Aktiviere Konto...");

        Service service = new Service();

        String email = ((TextView)findViewById(R.id.register_user_email)).getText().toString();
        String code = ((TextView)findViewById(R.id.register_user_name)).getText().toString();

        service.activateUserObserv(code, email)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<String>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d("CONAN", "error activating email user " + e.getMessage());
                        ((TextView)findViewById(R.id.register_user_info_box)).setText("Es gab ein Problem" +
                                " das Konto zu aktivieren. Versuche es nochmal. ");
                    }

                    @Override
                    public void onNext(String info) {
                        ((TextView)findViewById(R.id.register_user_info_box)).setText("Neues Konto wurde aktiviert!");
                        Log.d("CONAN", "activating email user " + info);
                        // setResult(RESULT_OK, null);
                        //finish();
                        //adaptViewForActivation();
                    }
                });
    }
}
