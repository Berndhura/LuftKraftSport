package de.wichura.lks.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.google.gson.JsonObject;

import org.json.JSONObject;

import java.util.List;

import de.wichura.lks.http.EbayRestService;
import de.wichura.lks.models.MsgRowItem;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Bernd Wichura on 06.06.2017.
 * Luftkraftsport
 */

public class EbayActivity extends AppCompatActivity {

    private EbayRestService ebayRestService;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ebayRestService = new EbayRestService();

        Observable<JsonObject> ebayService = ebayRestService.findItemsByKeywordObersv("tabou%203S");

        ebayService.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<JsonObject>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d("CONAN", "error in getting ebayzeug: " + e.toString());
                    }

                    @Override
                    public void onNext(JsonObject result) {
                        Log.d("CONAN", result.toString());
                    }
                });
    }
}
