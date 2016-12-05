package wichura.de.camperapp.app;

import android.app.Application;
import android.text.TextUtils;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

import wichura.de.camperapp.util.LruBitmapCache;

/**
 * Created by ich on 21.06.2015.
 * CamperApp
 */
public class AppController extends Application {


    @Override
    public void onCreate() {
        super.onCreate();
        }
}
