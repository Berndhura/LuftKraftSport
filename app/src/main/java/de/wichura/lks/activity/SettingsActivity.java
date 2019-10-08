package de.wichura.lks.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

import de.wichura.lks.R;
import de.wichura.lks.mainactivity.Constants;

import static de.wichura.lks.mainactivity.Constants.SHARED_PREFS_USER_INFO;

/**
 * Created by Bernd Wichura on 03.06.2016.
 * Luftkraftsport
 */
public class SettingsActivity extends AppCompatActivity {

    private TextView loginInfo;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.settings_layout);

        Toolbar toolbar = findViewById(R.id.settings_toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            toolbar.setNavigationOnClickListener((view) -> finish());
        }

        loginInfo = findViewById(R.id.login_info_text);
        loginInfo.setText("Angemeldet als: " + getUserName());

        ImageView shareApp = findViewById(R.id.share_luftkraftsport_app);
        shareApp.setOnClickListener(view -> {
            final Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, "Klicke den Link an: https://play.google.com/store/apps/details?id=de.wichura.lks");
            shareIntent.setType("text/plain");
            startActivity(Intent.createChooser(shareIntent, "Teile Luftkraftsport"));
        });

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestId()
                .requestProfile()
                .requestIdToken(Constants.WEB_CLIENT_ID)
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        initLogoutButton();
    }

    private void initLogoutButton() {
        Button logoutBtn = findViewById(R.id.logout_button);
        if (isUserLoggedIn()) {
            logoutBtn.setText("Abmelden");
        } else {
            logoutBtn.setText("Anmelden");
        }
        logoutBtn.setOnClickListener((view) -> {
            if (isUserLoggedIn()) {
                //logout from Facebook
                LoginManager.getInstance().logOut();
                //in case email login just delete sharedPrefs
                //sign out from Google
                signOutFromGoogle();
                updateUserInfo();
                finish();
            } else {
                final Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivityForResult(intent, Constants.REQUEST_ID_FOR_LOGIN);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.REQUEST_ID_FOR_LOGIN) {
            initLogoutButton();
            loginInfo.setText("Angemeldet als: " + getUserName());
            setResult(RESULT_OK, data);
            finish();
        }
    }

    private void signOutFromGoogle() {
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this, task ->
                        Log.d("CONAN", "Sign Out from Google done!"));
    }

    private void updateUserInfo() {
        SharedPreferences settings = getSharedPreferences(SHARED_PREFS_USER_INFO, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(Constants.USER_NAME, "");
        editor.putString(Constants.USER_ID, "");
        editor.putString(Constants.USER_TYPE, "");
        editor.putString(Constants.USER_TOKEN, "");
        editor.putString(Constants.USER_PICTURE, "");
        editor.apply();
    }

    private String getUserName() {
        SharedPreferences settings = getSharedPreferences(SHARED_PREFS_USER_INFO, 0);
        return settings.getString(Constants.USER_NAME, "");
    }

    private Boolean isUserLoggedIn() {
        SharedPreferences settings = getSharedPreferences(SHARED_PREFS_USER_INFO, 0);
        return (!settings.getString(Constants.USER_ID, "").equals(""));
    }
}
