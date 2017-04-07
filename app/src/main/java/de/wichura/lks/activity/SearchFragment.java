package de.wichura.lks.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import de.wichura.lks.R;
import de.wichura.lks.dialogs.ConfirmFollowSearchDialog;
import de.wichura.lks.dialogs.SetPriceDialog;
import de.wichura.lks.http.Service;
import de.wichura.lks.mainactivity.Constants;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;
import static de.wichura.lks.mainactivity.Constants.DISTANCE_INFINITY;
import static de.wichura.lks.mainactivity.Constants.PRICE;
import static de.wichura.lks.mainactivity.Constants.SHARED_PREFS_USER_INFO;
import static de.wichura.lks.mainactivity.Constants.USER_PRICE_RANGE;

/**
 * Created by ich on 12.03.2017.
 * LuftKraftSport
 */

public class SearchFragment extends Fragment {

    private TextView keywords;
    private TextView price;
    private String priceTo;
    private String priceFrom;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.search_activity, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        createGui(view);

        showLocation();
    }

    private void createGui(View view) {

        Toolbar toolbar = (Toolbar) view.findViewById(R.id.search_toolbar);
        if (toolbar != null) {
            ((SearchActivity) getActivity()).setSupportActionBar(toolbar);
            if (((SearchActivity) getActivity()).getSupportActionBar() != null) {
                ((SearchActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                ((SearchActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);
            }
            toolbar.setNavigationOnClickListener((v) -> getActivity().finish());
        }

        ImageView saveSearchButton = (ImageView) view.findViewById(R.id.save_search);
        saveSearchButton.setOnClickListener((v) -> {
            if (!"".equals(getUserToken())) {
                if (!isSaveSearchValid()) {
                    return;
                }
                saveSearch();
            } else {
                Toast.makeText(getActivity(), "Bitte anmelden, um Suche zu folgen!", Toast.LENGTH_LONG).show();
            }
        });

        keywords = (TextView) view.findViewById(R.id.keywords);

        price = (TextView) view.findViewById(R.id.price_from);
        price.setOnClickListener(v -> new SetPriceDialog().show(getActivity().getSupportFragmentManager(), null));

        ImageView changePriceBtn = (ImageView) view.findViewById(R.id.changePrice);

        Button searchButton = (Button) view.findViewById(R.id.search_button);

        searchButton.setOnClickListener(v -> {

            priceFrom = getPrice(Constants.PRICE_FROM);
            priceTo = getPrice(Constants.PRICE_TO);

            final Intent data = new Intent();
            data.putExtra(Constants.KEYWORDS, keywords.getText().toString());

            if (getString(R.string.price_does_not_matter).equals(price.getText().toString())) {
                data.putExtra(Constants.PRICE_FROM, "");
                data.putExtra(Constants.PRICE_TO, "");
            } else {

                if (getString(R.string.price_does_not_matter).equals(priceFrom)) {
                    data.putExtra(Constants.PRICE_FROM, "");
                } else {
                    data.putExtra(Constants.PRICE_FROM, priceFrom);
                }

                if (getString(R.string.price_does_not_matter).equals(priceTo)) {
                    data.putExtra(Constants.PRICE_TO, "");
                } else {
                    data.putExtra(Constants.PRICE_TO, priceTo);
                }
            }

            data.putExtra(Constants.DISTANCE, getDistance());

            getActivity().setResult(RESULT_OK, data);
            getActivity().finish();
        });

        changePriceBtn.setOnClickListener(v -> new SetPriceDialog().show(getActivity().getSupportFragmentManager(), null));
    }

    public void adaptLayoutForPrice(String from, String to) {
        if (getString(R.string.price_does_not_matter).equals(priceFrom)) {
            price.setText(R.string.price_does_not_matter);
        } else {
            price.setText(from + " bis " + to);
        }
    }

    private void saveSearch() {
        String description = keywords.getText().toString();
        Service service = new Service();

        service.saveSearchObserv(description, getMinPrice(), getMaxPrice(), getLat(), getLng(), getDistance(), getUserToken())
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<String>() {
                    @Override
                    public void onCompleted() {
                        new ConfirmFollowSearchDialog().show(getActivity().getSupportFragmentManager(), null);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d("CONAN", "error saving searches: " + e.getMessage());
                    }

                    @Override
                    public void onNext(String result) {}
                });
    }

    private int getMinPrice() {
        String minPrice = getActivity().getSharedPreferences(Constants.USER_PRICE_RANGE, MODE_PRIVATE).getString(Constants.PRICE_FROM, "");
        return (getString(R.string.price_does_not_matter).equals(minPrice))? 0 : Integer.parseInt(minPrice);
    }

    private int getMaxPrice() {
        String maxPrice = getActivity().getSharedPreferences(Constants.USER_PRICE_RANGE, MODE_PRIVATE).getString(Constants.PRICE_TO, "");
        return (getString(R.string.price_does_not_matter).equals(maxPrice))? Constants.MAX_PRICE : Integer.parseInt(maxPrice);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constants.REQUEST_ID_FOR_LOCATION) {
            showLocation();
        }
    }

    public boolean isSaveSearchValid() {
        boolean valid = true;

        String text = keywords.getText().toString();

        if (text.isEmpty()) {
            keywords.setError("Für Sucheaufträge darf der Titel nicht leer sein!");
            valid = false;
        } else {
            keywords.setError(null);
        }

        return valid;
    }


    private void showLocation() {
        SharedPreferences location = getActivity().getSharedPreferences(Constants.USERS_LOCATION, 0);
        int distance = location.getInt(Constants.DISTANCE, DISTANCE_INFINITY);
        if (((SearchActivity) getActivity()).getSupportActionBar() != null)
            ((SearchActivity) getActivity()).getSupportActionBar()
                    .setSubtitle("in " + location.getString(Constants.LOCATION, "") + ((distance == DISTANCE_INFINITY) ? (" Unbegrenzt") : (" (+" + location.getInt(Constants.DISTANCE, 0) / 1000 + " km)")));
    }

    public String getUserToken() {
        return getActivity().getSharedPreferences(SHARED_PREFS_USER_INFO, 0).getString(Constants.USER_TOKEN, "");
    }

    public String getPrice(String price) {
        return getActivity().getSharedPreferences(USER_PRICE_RANGE, 0).getString(price, "");
    }

    public Double getLng() {
        SharedPreferences settings = getActivity().getApplicationContext().getSharedPreferences(Constants.USERS_LOCATION, 0);
        return Double.longBitsToDouble(settings.getLong(Constants.LNG, 0));
    }

    public int getDistance() {
        SharedPreferences settings = getActivity().getApplicationContext().getSharedPreferences(Constants.USERS_LOCATION, 0);
        return settings.getInt(Constants.DISTANCE, Constants.DISTANCE_INFINITY);
    }


    public Double getLat() {
        SharedPreferences settings = getActivity().getApplicationContext().getSharedPreferences(Constants.USERS_LOCATION, 0);
        return Double.longBitsToDouble(settings.getLong(Constants.LAT, 0));
    }
}
