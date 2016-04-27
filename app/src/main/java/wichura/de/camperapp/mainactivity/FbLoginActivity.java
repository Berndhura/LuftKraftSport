package wichura.de.camperapp.mainactivity;


import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Base64;
import android.util.Log;
import android.view.View;
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
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import wichura.de.camperapp.R;
import wichura.de.camperapp.http.Urls;

/**
 * Created by Bernd Wichura on 28.07.2015.
 * Camper App
 */
public class FbLoginActivity extends AppCompatActivity {

    private String mUserId;
    private String mEmailUserId;
    private String mFacebookPicUrl;
    private AccessToken token;
    private Profile profile;

    private EditText _emailText;
    private EditText _passwordText;

    private LoginButton loginButton;

    private CallbackManager mCallbackMgt;
    private GoogleApiClient mGoogleApiClient;
    private int RC_SIGN_IN = 9;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

        } catch (NoSuchAlgorithmException e) {

        }

        mCallbackMgt = CallbackManager.Factory.create();

        setContentView(R.layout.fb_login_activity);

        Toolbar toolbar = (Toolbar) findViewById(R.id.login_toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    finish();
                }
            });
        }

        ImageView okButton = (ImageView) findViewById(R.id.ok_button);
        if (okButton != null) {
            okButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //TODO send request

                    if (!validate()) {
                        onLoginFailed();
                        return;
                    }

                    final ProgressDialog progressDialog = new ProgressDialog(FbLoginActivity.this,
                            R.style.AppTheme);
                    progressDialog.setIndeterminate(true);
                    progressDialog.setMessage("Authenticating...");
                    progressDialog.show();

                    String email = _emailText.getText().toString();
                    String password = _passwordText.getText().toString();
                    sendLoginRequest(email, password, progressDialog);
                }
            });
        }

        _emailText = (EditText) findViewById(R.id.login_name);
        _passwordText = (EditText) findViewById(R.id.password);


        TextView tv = (TextView) findViewById(R.id.register);
        tv.setText(Html.fromHtml("<a href=\"http://www.google.com\">Register</a>"));
        tv.setClickable(true);
        tv.setMovementMethod(LinkMovementMethod.getInstance());

        loginButton = (LoginButton) findViewById(R.id.login_button);
        if (loginButton != null) {
            loginButton.setReadPermissions("user_friends");
            loginButton.registerCallback(mCallbackMgt, mCallback);
        }

        AccessTokenTracker tracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken newAccessToken) {

            }
        };

        ProfileTracker profileTracker = new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(Profile oldProfile, Profile newProfile) {
                if (newProfile != null) {
                    mUserId = newProfile.getId();
                    Uri uri = newProfile.getProfilePictureUri(100, 100);
                    mFacebookPicUrl = uri.toString();
                    Log.d("Facebookbild", uri.toString());
                    //Picasso.with(getApplicationContext()).load(uri.toString()).into(picture);
                }
            }
        };

        tracker.startTracking();
        profileTracker.startTracking();


        /*
        GOOGLE+ Login
        -------------------------------------------------------------------------------------------
         */

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestProfile()
                .requestId()
                .requestScopes(new Scope(Scopes.PLUS_ME))
                .requestScopes(new Scope(Scopes.PLUS_LOGIN))
                .build();


        mGoogleApiClient = new GoogleApiClient.Builder(this)
                //TODO: this does not work....
                // .enableAutoManage(this, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        SignInButton googleButton = (SignInButton) findViewById(R.id.sign_in_button);
        setGooglePlusButtonText(googleButton, "Login with GOOGLE");
        if (googleButton != null) {
            googleButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
                    startActivityForResult(signInIntent, RC_SIGN_IN);
                }
            });
        }
    }


    private FacebookCallback<LoginResult> initFacebookCallback() {
        return new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                token = loginResult.getAccessToken();
                Log.d("CONAN", token.getUserId());
                profile = Profile.getCurrentProfile();
                if (profile != null) {
                    mUserId = profile.getId();
                    Uri uri = profile.getProfilePictureUri(250, 250);
                    Log.d("CONAN", uri.toString());
                    //Picasso.with(getApplicationContext()).load(uri.toString()).into(picture);
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
    protected void onStart() {
        super.onStart();
        //add this to connect Google Client
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        //Stop the Google Client when activity is stopped
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        data.putExtra(Constants.FACEBOOK_ID, mUserId);
        data.putExtra(Constants.FACEBOOK_PROFILE_PIC_URL, mFacebookPicUrl);

        mCallbackMgt.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        } else {
            //failed to connect
        }
        finish();
    }

    private void handleSignInResult(GoogleSignInResult result) {
        // Log.d(TAG, "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();
            String name = acct.getDisplayName();
            String email = acct.getEmail();
            String id = acct.getIdToken();
            String userGoogleId = acct.getId();
            Uri userUri = acct.getPhotoUrl();
            String name3 = acct.getDisplayName();
            Log.d("CONAN: ", "googel+ name: " + name);
            Log.d("CONAN: ", "googel+ id: " + userGoogleId);
        } else {
            // Signed out, show unauthenticated UI.
        }
    }


    protected void setGooglePlusButtonText(SignInButton signInButton,
                                           String buttonText) {
        for (int i = 0; i < signInButton.getChildCount(); i++) {
            View v = signInButton.getChildAt(i);

            if (v instanceof TextView) {
                TextView tv = (TextView) v;
                tv.setTextSize(15);
                tv.setTypeface(null, Typeface.NORMAL);
                tv.setText(buttonText);
                return;
            }
        }
    }


    public void onLoginFailed() {
        Toast.makeText(getBaseContext(), "Login failed", Toast.LENGTH_LONG).show();

        // _loginButton.setEnabled(true);
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


    private void sendLoginRequest(String email, String password, final ProgressDialog progressDialog) {

        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        String url = Urls.MAIN_SERVER_URL + Urls.LOGIN_USER + "?email=" + email + "&password=" + password;
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progressDialog.dismiss();
                        if (!response.equals("wrong")) {
                            Toast.makeText(getApplicationContext(), "User in", Toast.LENGTH_SHORT).show();
                            //TODO get userid back to mainActiv
                            String[] userInfos = response.split(",");
                            mEmailUserId = userInfos[0];
                            Intent data = new Intent();
                            data.putExtra(Constants.EMAIL_USR_ID, mEmailUserId);
                            data.putExtra(Constants.USER_NAME, userInfos[1]);
                            data.putExtra(Constants.USER_TYPE, Constants.EMAIL_USER);
                            setResult(RESULT_OK, data);
                            finish();
                        } else {
                            Toast.makeText(getApplicationContext(), "Wrong user or password. Try again!", Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();
                Toast.makeText(FbLoginActivity.this, "Network problems...Try again!", Toast.LENGTH_LONG).show();
            }
        });
        requestQueue.add(stringRequest);
    }
}

//Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
//        new ResultCallback<Status>(){
//      @Override
//      public void onResult(Status status){
//        Log.d("CONAN: ","google+ logout");
//        }
//        });