package wichura.de.camperapp.mainactivity;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.facebook.login.LoginManager;

import wichura.de.camperapp.R;

/**
 * Created by ich on 03.06.2016.
 * CamperApp
 */
public class SettingsActivity extends Activity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.settings_layout);

        Button logoutButton = initLogoutButton();
    }

    private Button initLogoutButton() {
        Button logoutBtn = (Button) findViewById(R.id.logout_button);
        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //logout from Facebook
                LoginManager.getInstance().logOut();
                //in case email login just delete sharedPrefs
                updateUserInfo();
                finish();
            }
        });
        return  logoutBtn;
    }

    private void updateUserInfo() {
        SharedPreferences settings = getSharedPreferences("UserInfo", 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(Constants.USER_NAME, "");
        editor.putString(Constants.USER_ID, "");
        editor.putString(Constants.USER_TYPE, "");
        editor.apply();
    }
}