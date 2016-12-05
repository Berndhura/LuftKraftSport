package wichura.de.camperapp.activity;


import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Base64;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import wichura.de.camperapp.R;
import wichura.de.camperapp.http.Service;
import wichura.de.camperapp.http.Urls;
import wichura.de.camperapp.mainactivity.Constants;
import wichura.de.camperapp.presentation.LoginPresenter;

import static wichura.de.camperapp.mainactivity.Constants.RC_SIGN_IN;
import static wichura.de.camperapp.mainactivity.Constants.SHARED_PREFS_USER_INFO;

/**
 * Created by Bernd Wichura on 28.07.2015.
 * Camper App
 */
public class LoginActivity extends AppCompatActivity {

    private AccessToken token;
    private Profile profile;

    private EditText _emailText;
    private EditText _passwordText;

    private CallbackManager mCallbackMgt;

    private GoogleApiClient mGoogleApiClient;

    private LoginPresenter presenter;
    private Service service;
    private ProgressDialog progressDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        service = new Service();
        presenter = new LoginPresenter(this, service, getApplicationContext());

        FacebookSdk.sdkInitialize(getApplicationContext());
        FacebookCallback<LoginResult> mCallback = initFacebookCallback();

        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    "wichura.de.camperapp",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.d("CONAN", "Package for app on facebook not found: " + e);

        } catch (NoSuchAlgorithmException e) {
            Log.d("CONAN", "Error while Facebook login " + e);
        }

        setContentView(R.layout.fb_login_activity);

        mCallbackMgt = CallbackManager.Factory.create();

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestId()
                .requestProfile()
                .requestIdToken(Constants.WEB_CLIENT_ID)
                .build();

        // Build a GoogleApiClient with access to the Google Sign-In API and the
        // options specified by gso.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        Log.d("CONAN", "google: " + connectionResult.getErrorMessage());
                    }
                } /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        SignInButton signInButton = (SignInButton) findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_STANDARD);
        signInButton.setColorScheme(SignInButton.COLOR_DARK);
        signInButton.setHovered(true);
        signInButton.setScopes(gso.getScopeArray());

        findViewById(R.id.sign_in_button).setOnClickListener(v -> {
            switch (v.getId()) {
                case R.id.sign_in_button:
                    signIn();
                    break;
                // ...
            }
        });


        Toolbar toolbar = (Toolbar) findViewById(R.id.login_toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            toolbar.setNavigationOnClickListener((view) -> finish());
        }

        ImageView okButton = (ImageView) findViewById(R.id.ok_button);
        if (okButton != null) {
            okButton.setOnClickListener((view) -> {
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

        _emailText = (EditText) findViewById(R.id.login_name);
        _passwordText = (EditText) findViewById(R.id.password);

        TextView tv = (TextView) findViewById(R.id.register);
        tv.setText(Html.fromHtml("<a href=\"http://raent.de:9876/\">Register</a>"));
        tv.setClickable(true);
        tv.setMovementMethod(LinkMovementMethod.getInstance());

        LoginButton loginButton = (LoginButton) findViewById(R.id.login_button);
        if (loginButton != null) {
            loginButton.setReadPermissions("user_friends");
            loginButton.registerCallback(mCallbackMgt, mCallback);
        }
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private FacebookCallback<LoginResult> initFacebookCallback() {
        return new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                token = loginResult.getAccessToken();
                Log.d("CONAN", token.getUserId());
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
            }
        };
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constants.RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }

        mCallbackMgt.onActivityResult(requestCode, resultCode, data);
        finish();
    }

    private void handleSignInResult(GoogleSignInResult result) {
        Log.d("CONAN", "handleSignInResult:" + result.isSuccess());
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
            finish();
        } else {
            // Signed out, show unauthenticated UI.
            //updateUI(false);
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
        Toast.makeText(getBaseContext(), "Login failed", Toast.LENGTH_LONG).show();
    }

    public boolean validate() {
        boolean valid = true;

        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _emailText.setError("enter a valid email address");
            valid = false;
        } else {
            _emailText.setError(null);
        }

        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
            _passwordText.setError("between 4 and 10 alphanumeric characters");
            valid = false;
        } else {
            _passwordText.setError(null);
        }

        return valid;
    }

    public void showProgressDialog() {
        progressDialog = new ProgressDialog(LoginActivity.this,
                R.style.AppTheme);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Authenticating...");
        progressDialog.show();
    }

    public void hideProgressdialog() {
        progressDialog.hide();
    }
}