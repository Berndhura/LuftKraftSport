package de.wichura.lks.mainactivity;

import android.app.Application;
import android.util.Log;

import org.acra.ACRA;
import org.acra.ReportField;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;
import org.acra.sender.HttpSender;

import de.wichura.lks.R;
import de.wichura.lks.http.Urls;
import de.wichura.lks.util.GoogleApiHelper;

/**
 * Created by Bernd Wichura on 25.02.2017.
 * Luftkraftsport
 */

@ReportsCrashes(
        formUri = Urls.UPLOAD_ERROR_URL,
        reportType = HttpSender.Type.JSON,
        httpMethod = HttpSender.Method.POST,
        customReportContent = {
                ReportField.APP_VERSION_CODE,
                ReportField.APP_VERSION_NAME,
                ReportField.ANDROID_VERSION,
                ReportField.PACKAGE_NAME,
                ReportField.REPORT_ID,
                ReportField.BUILD,
                ReportField.STACK_TRACE
        },
        mode = ReportingInteractionMode.NOTIFICATION,
        resToastText = R.string.app_name,
        resNotifTickerText = R.string.crash_text,
        resNotifTitle = R.string.crash_titel,
        resNotifText = R.string.crash_notification_text,
        resDialogText = R.string.crash_dialog_text
)
public class MainApp extends Application {

    private GoogleApiHelper googleApiHelper;
    private static MainApp mInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("CONAN", "onCreate in MainApp");

        mInstance = this;
        googleApiHelper = new GoogleApiHelper(mInstance);

        //ACRA.init(this);
       /* if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        LeakCanary.install(this);*/
    }

    public static synchronized MainApp getInstance() {
        return mInstance;
    }

    public GoogleApiHelper getGoogleApiHelperInstance() {
        return this.googleApiHelper;
    }
    public static GoogleApiHelper getGoogleApiHelper() {
        return getInstance().getGoogleApiHelperInstance();
    }
}
