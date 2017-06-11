package de.wichura.lks.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.wang.avi.AVLoadingIndicatorView;

import java.util.ArrayList;
import java.util.List;

import de.wichura.lks.R;
import de.wichura.lks.adapter.EbayAdsAdapter;
import de.wichura.lks.http.EbayRestService;
import de.wichura.lks.models.EbayAd;
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

    private AVLoadingIndicatorView mMessagesProgressBar;

    private ListView listView;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ebayRestService = new EbayRestService();

        setContentView(R.layout.ebay_overview_layout);

        Toolbar toolbar = (Toolbar) findViewById(R.id.ebay_overview_toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            toolbar.setNavigationOnClickListener((view) -> finish());
        }

        mMessagesProgressBar = (AVLoadingIndicatorView) findViewById(R.id.ebay_overview_ProgressBar);

        listView = (ListView) findViewById(R.id.ebay_overview_list);

        Observable<JsonObject> ebayService = ebayRestService.findItemsByKeywordObersv("tabou");

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

                        JsonElement elm = result.get("findItemsByKeywordsResponse").getAsJsonArray().get(0).getAsJsonObject().get("searchResult");

                        JsonArray resArray = elm.getAsJsonArray().get(0).getAsJsonObject().get("item").getAsJsonArray();

                        ArrayList<EbayAd> ebayAds = new ArrayList<EbayAd>();

                        for (JsonElement j : resArray) {
                            Log.d("CONAN", j.getAsJsonObject().get("title").toString());
                            Log.d("CONAN", j.getAsJsonObject().get("galleryURL").toString());
                            Log.d("CONAN", j.getAsJsonObject().get("viewItemURL").toString());
                            Log.d("CONAN", j.getAsJsonObject().get("location").toString());
                            //Log.d("CONAN", j.getAsJsonObject().get("currentPrice").getAsJsonArray().get(0).getAsJsonObject().get("__value__").toString());   //currentPrice":[{"@currencyId":"EUR","__value__":"1070.0"}]
                            EbayAd el = new EbayAd();
                            el.setTitle(j.getAsJsonObject().get("title").getAsString());
                            el.setThumbNailUrl(j.getAsJsonObject().get("galleryURL").getAsString());
                            el.setLocation(j.getAsJsonObject().get("location").getAsString());
                            el.setUrl(j.getAsJsonObject().get("viewItemURL").getAsString());

                            ebayAds.add(el);
                        }

                        updateResults(ebayAds);

                        Log.d("CONAN", result.toString());
                    }
                });
    }

    private void updateResults(List<EbayAd> ebayList) {

        hideProgressBar();

        List<EbayAd> rowItems = new ArrayList<>();

        for (EbayAd e : ebayList) {
            rowItems.add(e);
        }

        getSupportActionBar().setTitle("Treffer: " + rowItems.size());

        EbayAdsAdapter adapter = new EbayAdsAdapter(getApplicationContext(), R.layout.ebay_row_item, rowItems);
        listView.setAdapter(adapter);
        listView.setSelection(listView.getCount() - 1);
        adapter.notifyDataSetChanged();
        listView.setSelectionAfterHeaderView();
        listView.setOnItemClickListener((adapterView, view, position, l) -> {
            final EbayAd rowItem = (EbayAd) listView.getItemAtPosition(position);
            //open url in browser
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(rowItem.getUrl()));
            startActivity(browserIntent);
        });
    }

    public void hideProgressBar() {
        mMessagesProgressBar.setVisibility(ProgressBar.GONE);
    }
}
