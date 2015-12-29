package wichura.de.camperapp.mainactivity;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;


import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
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

import java.io.IOException;
import java.net.URI;
import java.net.URL;

import wichura.de.camperapp.R;
import wichura.de.camperapp.app.AppController;

/**
 * Created by ich on 28.07.2015.
 */
public class FbLoginActivity extends Activity {

    private TextView mName;
    private ImageView picture;
    ImageLoader imageLoader = AppController.getInstance().getImageLoader();

    private CallbackManager mCallbackMgt;

    private FacebookCallback<LoginResult> mCallback = new FacebookCallback<LoginResult>() {
        @Override
        public void onSuccess(LoginResult loginResult) {

            AccessToken token = loginResult.getAccessToken();
            Profile profile =  Profile.getCurrentProfile();
           if (profile!=null) {
               mName.setText("tark: " + profile.getName());
              Uri uri =  profile.getProfilePictureUri(100, 100);
               Log.d("Facebookbild", uri.toString());
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

        mCallbackMgt=CallbackManager.Factory.create();

        setContentView(R.layout.fb_login_activity);

        LoginButton loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.setReadPermissions("user_friends");

        loginButton.registerCallback(mCallbackMgt, mCallback);

        mName= (EditText) findViewById(R.id.name);
        picture = (ImageView) findViewById(R.id.profilePic);


        AccessTokenTracker tracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken newAccessToken) {

            }
        };

        ProfileTracker profileTracker = new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(Profile oldProfile, Profile newProfile) {
                if (newProfile!=null) {
                    mName.setText("tark: " + newProfile.getName());
                    Uri uri =  newProfile.getProfilePictureUri(100, 100);
                    Log.d("Facebookbild", uri.toString());

                    Picasso.with(getApplicationContext()).load(uri.toString()).into(picture);
                }
            }
        };

        tracker.startTracking();
        profileTracker.startTracking();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        mCallbackMgt.onActivityResult(requestCode,resultCode, data);
    }
}
