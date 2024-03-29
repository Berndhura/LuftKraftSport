package de.wichura.lks.activity;


import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.Profile;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.SignInButton;
import com.wang.avi.AVLoadingIndicatorView;

import de.wichura.lks.R;
import de.wichura.lks.dialogs.ShowUserNotActivatedDialog;
import de.wichura.lks.http.Service;
import de.wichura.lks.mainactivity.Constants;
import de.wichura.lks.presentation.LoginPresenter;

import static de.wichura.lks.mainactivity.Constants.RC_SIGN_IN;
import static de.wichura.lks.mainactivity.Constants.SHARED_PREFS_USER_INFO;

/**
 * Created by Bernd Wichura on 28.07.2015.
 * Luftkraftsport
 */
public class LoginActivity extends AppCompatActivity implements
        ShowUserNotActivatedDialog.OnCompleteActivationCodeListener, View.OnClickListener {

    private AccessToken token;
    private Profile profile;

    private EditText _emailText;
    private EditText _passwordText;

    private CallbackManager mCallbackMgt;

    private LoginPresenter presenter;
    private AVLoadingIndicatorView progressBar;

    private GoogleSignInClient mGoogleSignInClient;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Service service = new Service();
        presenter = new LoginPresenter(this, service, getApplicationContext());

        setContentView(R.layout.login_activity);

        mCallbackMgt = CallbackManager.Factory.create();

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestId()
                .requestProfile()
                .requestIdToken(Constants.WEB_CLIENT_ID)
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);


        SignInButton signInButton = findViewById(R.id.google_login_button);
        signInButton.setSize(SignInButton.SIZE_STANDARD);
        signInButton.setColorScheme(SignInButton.COLOR_DARK);
        signInButton.setHovered(true);
        // signInButton.setScopes(gso.getScopeArray());
        setGooglePlusButton(signInButton, "Anmelden mit Google");
        //signInButton.setOnClickListener(v -> signIn());
        findViewById(R.id.google_login_button).setOnClickListener(this);


        Toolbar toolbar = findViewById(R.id.login_toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            toolbar.setNavigationOnClickListener((view) -> finish());
        }

        Button email_login_button = findViewById(R.id.email_login_button);
        if (email_login_button != null) {
            email_login_button.setOnClickListener((view) -> {
                if (!validate()) {
                    onLoginFailed();
                    return;
                }

                showProgressDialog();
                String email = _emailText.getText().toString();
                String password = _passwordText.getText().toString();
                presenter.sendLoginReq(email, password);
            });
        }

        _emailText = findViewById(R.id.login_name);
        _passwordText = findViewById(R.id.password);

        Button register = findViewById(R.id.register);
        register.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), RegisterUserActivity.class);
            startActivityForResult(i, Constants.REQUEST_ID_FOR_REGISTER_USER);
        });

        LoginButton fbLoginButton = findViewById(R.id.fb_login_button);
        if (fbLoginButton != null) {
            //fbLoginButton.setPublishPermissions("publish_actions");
            fbLoginButton.setReadPermissions("email");
            fbLoginButton.registerCallback(mCallbackMgt, new FacebookCallback<LoginResult>() {
                @Override
                public void onSuccess(LoginResult loginResult) {
                    token = loginResult.getAccessToken();
                    Log.d("CONAN", "Facebook user token: " + token.getUserId());
                    profile = Profile.getCurrentProfile();
                    if (profile != null) {
                        Uri uri = profile.getProfilePictureUri(250, 250);
                        setUserPreferences(profile.getName(), profile.getId(), uri, Constants.FACEBOOK_USER, token.getToken());
                        Intent loginComplete = new Intent(Constants.LOGIN_COMPLETE);
                        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(loginComplete);
                    }
                }

                @Override
                public void onCancel() {

                }

                @Override
                public void onError(FacebookException error) {
                    Log.d("CONAN", "Facebook error: " + error.getLocalizedMessage());
                }
            });
        }

        adaptFacebookButton(fbLoginButton);
        setFbButton(fbLoginButton, "Anmelden mit Facebook");

        progressBar = findViewById(R.id.login_ProgressBar);
    }

    private void adaptFacebookButton(LoginButton loginButton) {

        float fbIconScale = 2F;
        Drawable drawable = getApplication().getResources().getDrawable(
                com.facebook.R.drawable.com_facebook_button_icon);
        drawable.setBounds(0, 0, (int) (drawable.getIntrinsicWidth() * fbIconScale),
                (int) (drawable.getIntrinsicHeight() * fbIconScale));
        loginButton.setCompoundDrawables(drawable, null, null, null);
        loginButton.setCompoundDrawablePadding(getApplication().getResources().
                getDimensionPixelSize(R.dimen.fb_margin_override_textpadding));
        loginButton.setPadding(
                getApplication().getResources().getDimensionPixelSize(
                        R.dimen.fb_margin_override_lr),
                getApplication().getResources().getDimensionPixelSize(
                        R.dimen.fb_margin_override_top),
                getApplication().getResources().getDimensionPixelSize(
                        R.dimen.fb_margin_override_lr),
                getApplication().getResources().getDimensionPixelSize(
                        R.dimen.fb_margin_override_bottom));
    }

    private void setGooglePlusButton(SignInButton signInButton, String buttonText) {

        for (int i = 0; i < signInButton.getChildCount(); i++) {
            View v = signInButton.getChildAt(i);
            Log.d("GOOGLE_PLUS_TAG", "Type Of Child : " + v.getClass().getName());
            if (v instanceof TextView) {
                TextView tv = (TextView) v;
                tv.setText(buttonText);
                int drawablePadding = 0;
                tv.setCompoundDrawablePadding(drawablePadding);
                tv.setTextColor(getResources().getColor(R.color.white_smoke));
                tv.setTextSize(15);
                tv.setBackgroundColor(getResources().getColor(R.color.googlePlusLoginBtn));
                return;
            }
        }
    }

    private void setFbButton(LoginButton signInButton, String buttonText) {
        signInButton.setBackgroundColor(getResources().getColor(R.color.facebookLoginBtn));
        signInButton.setText(buttonText);
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d("CONAN", "handleSignInResult:" + data);

        if (requestCode == Constants.RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result, data);
        }

        if (requestCode == Constants.REQUEST_ID_FOR_REGISTER_USER) {
            //use request code from email to verify email
            // Intent i = new Intent(getApplicationContext(), ActivateUserActivity.class);
            // startActivityForResult(i, Constants.REQUEST_ID_FOR_ACTIVATE_USER);
            // TODO: back from activate... einloggen...
        }

        mCallbackMgt.onActivityResult(requestCode, resultCode, data);
        finish();
    }

    private void handleSignInResult(GoogleSignInResult result, Intent data) {
        Log.d("CONAN", "handleSignInResult from Google:" + result.isSuccess());
        Log.d("CONAN", "handleSignInResult status from google:" + result.getStatus());

        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();
            if (acct != null) {
                String name = acct.getDisplayName();
                String userId = acct.getId();
                String token = acct.getIdToken();
                Log.d("CONAN", "Google Token: " + token);

                Uri userPicture = acct.getPhotoUrl();
                Log.d("CONAN", userPicture.toString());
                setUserPreferences(name, userId, userPicture, Constants.GOOGLE_USER, token);
            }
            setResult(RESULT_OK, data);
            finish();
        } else {
            Log.d("CONAN", result.toString());
        }
    }

    public void setUserPreferences(String name, String userId, Uri userPic, String userType, String userToken) {
        SharedPreferences settings = getSharedPreferences(SHARED_PREFS_USER_INFO, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(Constants.USER_NAME, name);
        editor.putString(Constants.USER_ID, userId);

        if (userPic != null) {
            editor.putString(Constants.USER_PICTURE, userPic.toString());
        } else {
            editor.putString(Constants.USER_PICTURE, "");
        }
        editor.putString(Constants.USER_TYPE, userType);

        if (userToken != null) {
            editor.putString(Constants.USER_TOKEN, userToken);
        } else {
            editor.putString(Constants.USER_TOKEN, "");
        }
        editor.apply();
    }

    public void onLoginFailed() {
        Toast.makeText(getBaseContext(), "Login fehl geschlagen", Toast.LENGTH_LONG).show();
    }

    public boolean validate() {
        boolean valid = true;

        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _emailText.setError("gib eine richtige email an!");
            valid = false;
        } else {
            _emailText.setError(null);
        }

        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
            _passwordText.setError("zwischen 4 und 10 alphanumerische Zeichen!");
            valid = false;
        } else {
            _passwordText.setError(null);
        }

        return valid;
    }

    @Override
    public void onActivationCodeComplete(String email, String password, String code) {
        Log.d("CONAN", "ActivationCode from dialog: " + code);
        presenter.sendActivationCode(email, password, code);
    }

    public void showProgressDialog() {
        progressBar.setVisibility(View.VISIBLE);
    }

    public void hideProgressDialog() {
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.google_login_button:
                signIn();
                break;
            // ...
        }
    }
}