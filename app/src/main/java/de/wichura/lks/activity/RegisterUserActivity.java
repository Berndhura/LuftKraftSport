package de.wichura.lks.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import de.wichura.lks.R;
import de.wichura.lks.dialogs.ShowUserNotActivatedDialog;
import de.wichura.lks.http.Service;
import de.wichura.lks.mainactivity.Constants;
import de.wichura.lks.util.Utility;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static de.wichura.lks.mainactivity.Constants.ACTIVATE_USER_STATUS;

/**
 * Created by bwichura on 24.02.2017.
 * Luftkraftsport
 */


public class RegisterUserActivity extends AppCompatActivity implements
        ShowUserNotActivatedDialog.OnCompleteActivationCodeListener {

    private EditText email;
    private EditText name;
    private EditText password;
    private Utility utils;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        utils = new Utility(this);

        setContentView(R.layout.register_user_activity);

        findViewById(R.id.register_user_button).setOnClickListener(v -> {
            registerUser();
            Log.d("CONAN", "Registriere user");
        });
    }

    private void registerUser() {

        Service service = new Service();

        name = (EditText) findViewById(R.id.register_user_name);
        email = (EditText) findViewById(R.id.register_user_email);
        password = (EditText) findViewById(R.id.register_user_password);

        String hashedPw = utils.computeSHAHash(password.getText().toString());

        if (validate()) {
            ((TextView) findViewById(R.id.register_user_info_box)).setText("Registriere Konto...");
            service.registerUserObserv(name.getText().toString(), email.getText().toString(), hashedPw)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<String>() {
                        @Override
                        public void onComplete() {

                        }

                        @Override
                        public void onError(Throwable e) {
                            Log.d("CONAN", "error registering email user " + e.getMessage());
                            ((TextView) findViewById(R.id.register_user_info_box)).setText(R.string.user_is_registered_problem);
                        }

                        @Override
                        public void onNext(String info) {
                            ((TextView) findViewById(R.id.register_user_info_box)).setText(R.string.user_is_registered);
                            Log.d("CONAN", "registering email user " + info);
                            // setResult(RESULT_OK, null);
                            //finish();
                            setRegisterUpdatePreferences(true, false);
                            adaptViewForActivation();
                        }

                        @Override
                        public void onSubscribe(Disposable d) {

                        }
                    });
        } else {
            ((TextView) findViewById(R.id.register_user_info_box)).setText("Es gibt noch ungülige Eingaben!");
        }
    }

    private void setRegisterUpdatePreferences(boolean isUserRegistered, boolean isUserActivated) {
        SharedPreferences settings = getBaseContext().getSharedPreferences(ACTIVATE_USER_STATUS, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(Constants.REGISTER_USER, isUserRegistered);
        editor.putBoolean(Constants.ACTIVATE_USER, isUserActivated);
        editor.apply();
    }

    public boolean isUserRegistered() {
        SharedPreferences settings = getBaseContext().getSharedPreferences(Constants.ACTIVATE_USER_STATUS, 0);
        return settings.getBoolean(Constants.REGISTER_USER, false);
    }

    public boolean isUserActive() {
        SharedPreferences settings = getBaseContext().getSharedPreferences(Constants.ACTIVATE_USER_STATUS, 0);
        return settings.getBoolean(Constants.ACTIVATE_USER, false);
    }


    private void adaptViewForActivation() {

        findViewById(R.id.register_user_password).setVisibility(View.GONE);
        ((TextView) findViewById(R.id.register_user_name)).setHint("Aktivierungscode");
        ((Button) findViewById(R.id.register_user_button)).setText("Sende Aktiverungscode");
        ((TextView) findViewById(R.id.register_user_name)).setText("");

        findViewById(R.id.register_user_button).setOnClickListener(v -> {
            activateUser();
            Log.d("CONAN", "activating...");
        });
    }

    private void adaptViewForExitActivation() {
        ((Button) findViewById(R.id.register_user_button)).setText("Zurück zum Login");
        findViewById(R.id.register_user_button).setOnClickListener(v -> {
            final Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            finish();
            startActivityForResult(intent, Constants.REQUEST_ID_FOR_LOGIN);
        });
    }

    private void activateUser() {

        ((TextView) findViewById(R.id.register_user_info_box)).setText("Aktiviere Konto...");

        Service service = new Service();

        String email = ((TextView) findViewById(R.id.register_user_email)).getText().toString();
        String code = ((TextView) findViewById(R.id.register_user_name)).getText().toString();

        if (validateActivationCode()) {
            service.activateUserObserv(code, email)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<String>() {
                        @Override
                        public void onComplete() {

                        }

                        @Override
                        public void onError(Throwable e) {
                            Log.d("CONAN", "error activating email user " + e.getMessage());
                            ((TextView) findViewById(R.id.register_user_info_box)).setText("Es gab ein Problem das Konto zu aktivieren. Versuche es nochmal. ");
                        }

                        @Override
                        public void onNext(String info) {
                            ((TextView) findViewById(R.id.register_user_info_box)).setText("Neues Konto wurde aktiviert! Du kannst dich mit deiner Email und Passwort anmelden.");
                            Log.d("CONAN", "activating email user " + info);

                            if ("invalid".equals(info)) {
                                openActivationDialog(email, null);
                            } else {
                                adaptViewForExitActivation();
                            }
                        }

                        @Override
                        public void onSubscribe(Disposable d) {

                        }
                    });
        }
    }

    private void openActivationDialog(String email, String password) {

        Bundle credentials = new Bundle();
        credentials.putString("email", email);
        //use not hashed password
        //after activation password gets hashed again here in login
        if (password != null)
            credentials.putString("password", password);

        ShowUserNotActivatedDialog dialog = new ShowUserNotActivatedDialog();
        dialog.setArguments(credentials);
        dialog.show(getSupportFragmentManager(), null);
    }

    public boolean validateActivationCode() {

        boolean valid = true;

        String nameStr = name.getText().toString();

        if (nameStr.isEmpty()) {
            name.setError("Aktivierungscode angeben!");
            valid = false;
        } else {
            name.setError(null);
        }

        return valid;
    }

    public boolean validate() {
        boolean valid = true;

        String emailStr = email.getText().toString();

        if (emailStr.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(emailStr).matches()) {
            email.setError("Richtige email Adresse angeben!");
            valid = false;
        } else {
            email.setError(null);
        }

        String nameStr = name.getText().toString();

        if (nameStr.isEmpty()) {
            name.setError("Namen angeben!");
            valid = false;
        } else {
            name.setError(null);
        }

        String passwordStr = password.getText().toString();

        if (passwordStr.isEmpty()) {
            password.setError("Passwort angeben!");
            valid = false;
        } else {
            password.setError(null);
        }

        return valid;
    }

    @Override
    public void onActivationCodeComplete(String email, String password, String code) {
        Log.d("CONAN", "ActivationCode from dialog: " + code);
        ((TextView) findViewById(R.id.register_user_name)).setText(code);
        activateUser();
    }
}
