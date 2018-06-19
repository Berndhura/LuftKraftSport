package de.wichura.lks.gcm;

/**
 * Created by Bernd Wichura on 14.05.2016.
 * Luftkraftsport
 */

import android.content.SharedPreferences;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import de.wichura.lks.http.Service;
import de.wichura.lks.mainactivity.Constants;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static de.wichura.lks.mainactivity.Constants.SHARED_PREFS_USER_INFO;

public class MyInstanceIDListenerService extends FirebaseInstanceIdService {

    private static final String TAG = "CONAN";

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is also called
     * when the InstanceID token is initially generated, so this is where
     * you retrieve the token.
     */
    // [START refresh_token]
    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed token: " + refreshedToken);
        // TODO: Implement this method to send any registration to your app's servers.
        sendRegistrationToServer(refreshedToken);
    }

    /**
     * Persist registration to third-party servers.
     * <p/>
     * Modify this method to associate the user's GCM registration token with any server-side account
     * maintained by your application.
     *
     * @param deviceToken The new token.
     */
    public void sendRegistrationToServer(String deviceToken) {
        Service service = new Service();

        SharedPreferences settings = getSharedPreferences(SHARED_PREFS_USER_INFO, 0);
        String userId = settings.getString(Constants.USER_ID, "");
        String userToken = settings.getString(Constants.USER_TOKEN, "");

        if (!userId.equals("")) {
            service.sendDeviceTokenObserv(userToken, deviceToken)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<String>() {

                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onNext(String result) {
                            Log.d("CONAN", "send device token to server: " + result);
                        }

                        @Override
                        public void onError(Throwable e) {
                            Log.d("CONAN", "error in sending device token: " + e.getMessage());
                        }

                        @Override
                        public void onComplete() {

                            Log.d("CONAN", "send device token to server");
                        }
                    });
        }
    }
}
