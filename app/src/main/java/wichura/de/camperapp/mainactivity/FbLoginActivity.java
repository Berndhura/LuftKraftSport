package wichura.de.camperapp.mainactivity;


import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.squareup.picasso.Picasso;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import wichura.de.camperapp.R;

/**
 * Created by ich on 28.07.2015.
 */
public class FbLoginActivity extends Activity {

    private TextView mName;
    private String mUserId;
    private String mFacebookPicUrl;
    private ImageView picture;

    private AccessToken token;

    private Button backButton;

    Profile profile;

    private CallbackManager mCallbackMgt;
    private FacebookCallback<LoginResult> mCallback = new FacebookCallback<LoginResult>() {
        @Override
        public void onSuccess(LoginResult loginResult) {

            token = loginResult.getAccessToken();
            Log.d("CONAN", token.getUserId());
            profile = Profile.getCurrentProfile();
            if (profile != null) {
                mName.setText("tark: " + profile.getName());
                mUserId = profile.getId();
                Uri uri = profile.getProfilePictureUri(250, 250);
                Log.d("CONAN", uri.toString());
                //load Facebook profile picture from uri -> show in picture
                Picasso.with(getApplicationContext()).load(uri.toString()).into(picture);
            }

        }

        @Override
        public void onCancel() {

        }

        @Override
        public void onError(FacebookException error) {

        }
    };


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        // Initialize the SDK before executing any other operations,
        // especially, if you're using Facebook UI elements.
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

        LoginButton loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.setReadPermissions("user_friends");

        loginButton.registerCallback(mCallbackMgt, mCallback);

        mName = (EditText) findViewById(R.id.name);
        picture = (ImageView) findViewById(R.id.profilePic);

        backButton();


        AccessTokenTracker tracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken newAccessToken) {

            }
        };

        ProfileTracker profileTracker = new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(Profile oldProfile, Profile newProfile) {
                if (newProfile != null) {
                    mName.setText("FB Name: " + newProfile.getName());
                    mUserId = newProfile.getId();
                    Uri uri = newProfile.getProfilePictureUri(100, 100);
                    mFacebookPicUrl = uri.toString();
                    Log.d("Facebookbild", uri.toString());
                    //load Facebook profile picture from uri -> show in picture
                    Picasso.with(getApplicationContext()).load(uri.toString()).into(picture);
                }
            }
        };

        tracker.startTracking();
        profileTracker.startTracking();
    }

    private void backButton() {
        backButton = (Button) findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent data = new Intent();
                data.putExtra(Constants.FACEBOOK_ID, mUserId);
                data.putExtra(Constants.FACEBOOK_PROFILE_PIC_URL, mFacebookPicUrl);
                data.putExtra(Constants.FACEBOOK_ACCESS_TOKEN, token);

                setResult(RESULT_OK, data);
                if(profile != null){
                    Intent main = new Intent(FbLoginActivity.this, MainActivity.class);
                    main.putExtra("name", profile.getFirstName());
                    main.putExtra("surname", profile.getLastName());
                    main.putExtra("imageUrl", profile.getProfilePictureUri(200,200).toString());
                    startActivity(main);

                }
                finish();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        data.putExtra(Constants.FACEBOOK_ID, mUserId);
        data.putExtra(Constants.FACEBOOK_PROFILE_PIC_URL, mFacebookPicUrl);

        mCallbackMgt.onActivityResult(requestCode, resultCode, data);
    }
}
