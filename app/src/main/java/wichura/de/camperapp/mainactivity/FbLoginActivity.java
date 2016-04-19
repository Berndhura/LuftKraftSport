package wichura.de.camperapp.mainactivity;


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
import android.widget.TextView;

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

/**
 * Created by ich on 28.07.2015.
 *
 */
public class FbLoginActivity extends AppCompatActivity {

    private String mUserId;
    private String mFacebookPicUrl;
    private AccessToken token;
    private Profile profile;

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
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);

        TextView tv = (TextView) findViewById(R.id.register);
        tv.setText(Html.fromHtml("<a href=\"http://www.google.com\">Register</a>"));
        tv.setClickable(true);
        tv.setMovementMethod(LinkMovementMethod.getInstance());

        LoginButton loginButton = (LoginButton) findViewById(R.id.login_button);
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
}

//Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
//        new ResultCallback<Status>(){
//      @Override
//      public void onResult(Status status){
//        Log.d("CONAN: ","google+ logout");
//        }
//        });