package de.wichura.lks.controller;

import android.app.Activity;
import android.net.Uri;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.bumptech.glide.Glide;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.ProfileTracker;

import android.content.Intent;

import de.wichura.lks.R;
import de.wichura.lks.mainactivity.Constants;
import de.wichura.lks.presentation.MainPresenter;
import de.wichura.lks.util.SessionStore;

/**
 * Owns everything related to showing "who is logged in" in the main screen:
 * the login button, the Facebook AccessToken/Profile trackers that mirror
 * Facebook login state into our SessionStore, and the drawer's profile
 * name + avatar.
 */
public class LoginUiController {

    private final Activity activity;
    private final ImageView loginButton;
    private final SessionStore session;
    private final MainPresenter presenter;
    private final Runnable startLoginAction;

    private final AccessTokenTracker accessTokenTracker;
    private final ProfileTracker profileTracker;

    private final Runnable onFacebookLoggedIn;

    public LoginUiController(Activity activity,
                             ImageView loginButton,
                             SessionStore session,
                             MainPresenter presenter,
                             Runnable startLoginAction,
                             Runnable onFacebookLoggedIn) {
        this.activity = activity;
        this.loginButton = loginButton;
        this.session = session;
        this.presenter = presenter;
        this.startLoginAction = startLoginAction;
        this.onFacebookLoggedIn = onFacebookLoggedIn;

        FacebookSdk.sdkInitialize(activity.getApplicationContext());

        this.accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken newAccessToken) {
                if (newAccessToken != null) {
                    session.setUserType(Constants.FACEBOOK_USER);
                    session.updateUser(null, null, newAccessToken.getToken());
                } else {
                    session.updateUser("", "", "");
                    setProfileName("");
                    setProfilePicture(null);
                    refreshLoginButton();
                }
            }
        };

        this.profileTracker = new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(Profile oldProfile, Profile newProfile) {
                if (newProfile == null) return;
                session.setUserType(Constants.FACEBOOK_USER);
                session.updateUser(newProfile.getName(), newProfile.getId(), null);

                if (oldProfile == null && session.isLoggedIn()) {
                    onFacebookLoggedIn.run();
                    refreshLoginButton();
                }
                LocalBroadcastManager.getInstance(activity.getApplicationContext())
                        .sendBroadcast(new Intent(Constants.LOGIN_COMPLETE));
            }
        };

        accessTokenTracker.startTracking();
        profileTracker.startTracking();
    }

    /** Sync the login button with the current session state. */
    public void refreshLoginButton() {
        if (session.getUserId().isEmpty()) {
            loginButton.setEnabled(true);
            loginButton.setVisibility(View.VISIBLE);
            loginButton.setOnClickListener(v -> startLoginAction.run());
        } else {
            loginButton.setEnabled(false);
            loginButton.setVisibility(View.GONE);
        }
        // Legacy behaviour: refresh Facebook user info every time we touch the button.
        presenter.getFacebookUserInfo();
    }

    /** Refresh drawer profile name + avatar from current session. */
    public void refreshProfileHeader() {
        setProfileName(session.isLoggedIn() ? session.getUserName() : "Bitte anmelden...");
        String pic = session.getUserProfilePicture();
        setProfilePicture(pic.isEmpty() ? null : Uri.parse(pic));
    }

    public void setProfileName(String name) {
        TextView view = activity.findViewById(R.id.username);
        if (view != null) view.setText(name);
    }

    public void setProfilePicture(Uri uri) {
        ImageView proPic = activity.findViewById(R.id.profile_image);
        if (proPic == null) return;
        if (uri != null && !uri.toString().isEmpty()) {
            Glide.with(activity.getApplicationContext()).load(uri.toString()).into(proPic);
        } else {
            proPic.setImageResource(R.drawable.lks_app_logo);
        }
    }

    public void destroy() {
        accessTokenTracker.stopTracking();
        profileTracker.stopTracking();
    }
}
