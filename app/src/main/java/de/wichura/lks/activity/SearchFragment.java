package de.wichura.lks.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.wang.avi.AVLoadingIndicatorView;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.wichura.lks.R;
import de.wichura.lks.dialogs.ConfirmFollowSearchDialog;
import de.wichura.lks.dialogs.SetPriceDialog;
import de.wichura.lks.dialogs.ShowNetworkProblemDialog;
import de.wichura.lks.http.Service;
import de.wichura.lks.mainactivity.Constants;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;
import static de.wichura.lks.mainactivity.Constants.DISTANCE_INFINITY;
import static de.wichura.lks.mainactivity.Constants.LAST_SEARCH;
import static de.wichura.lks.mainactivity.Constants.SHARED_PREFS_USER_INFO;
import static de.wichura.lks.mainactivity.Constants.UNREAD_MESSAGES;
import static de.wichura.lks.mainactivity.Constants.USER_PRICE_RANGE;

/**
 * Created by Bernd Wichura on 12.03.2017.
 * Luftkraftsport
 */

public class SearchFragment extends Fragment {

    @BindView(R.id.keywords)
    TextView keywords;

    @BindView(R.id.price_from)
    TextView price;

    @BindView(R.id.search_location_zip_and_location)
    TextView location;

    @BindView(R.id.start_search)
    Button startSearch;

    @BindView(R.id.follow_search)
    Button followSearch;

    @BindView(R.id.follow_search_ProgressBar)
    AVLoadingIndicatorView progressBar;

    String priceTo;

    String priceFrom;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.search_activity, container, false);

        ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        createGui(view, savedInstanceState);

        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(keywords.getWindowToken(), 0);

        //showLocation();
    }

    private void createGui(View view, Bundle savedInstanceState) {

        Toolbar toolbar = (Toolbar) view.findViewById(R.id.search_toolbar);
        if (toolbar != null) {
            ((SearchActivity) getActivity()).setSupportActionBar(toolbar);
            if (((SearchActivity) getActivity()).getSupportActionBar() != null) {
                ((SearchActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                ((SearchActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);
            }
            toolbar.setNavigationOnClickListener((v) -> getActivity().finish());
        }

        followSearch.setOnClickListener(v -> {
            if (!"".equals(getUserToken())) {
                if (!isSaveSearchValid()) {
                    return;
                }
                saveSearch();
            } else {
                Toast.makeText(getActivity(), "Bitte anmelden, um einer Suche zu folgen!", Toast.LENGTH_LONG).show();
            }
        });

        keywords.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int DRAWABLE_RIGHT = 2;
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (event.getRawX() >= (keywords.getRight() - keywords.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        performSearch();
                        return true;
                    }
                }
                return false;
            }
        });

        keywords.setOnEditorActionListener((tv, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                keywords.clearFocus();
                InputMethodManager in = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                in.hideSoftInputFromWindow(keywords.getWindowToken(), 0);
                performSearch();
                return true;
            }
            return false;
        });

        //TODO dadurch bleibt keywords auch nicht belegt nach wechsel zur location
        if (savedInstanceState != null) {
            Log.d("CONAN", "restore keywords");
            keywords.setText(savedInstanceState.getString("keywords", ""));
        }

        location.setOnClickListener(v -> {
            closeKeyboard(getActivity(), keywords.getWindowToken());

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                checkLocationPermission();
            } else {
                openLocationFragment();
            }
        });

        location.setText(getLocationString());

        price.setOnClickListener(v -> new SetPriceDialog().show(getActivity().getSupportFragmentManager(), null));

        startSearch.setOnClickListener(v -> performSearch());

        //in case we come back from main activity for search again -> use old search parameter
        if (getActivity().getIntent().getStringExtra(Constants.TITLE) != null) {
            keywords.setText(getActivity().getIntent().getStringExtra(Constants.TITLE));
            adaptLayoutForPrice(getActivity().getIntent().getStringExtra(Constants.PRICE_FROM), getActivity().getIntent().getStringExtra(Constants.PRICE_TO));
        }
    }


    //TODO dadurch bleibt keywords auch nicht belegt nach wechsel zur location
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onCreate(outState);
        Log.d("CONAN", "outState");
        outState.putString("keyword", keywords.getText().toString());
    }

    private void openLocationFragment() {
        android.support.v4.app.FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.layout, new LocationFragment());
        fragmentTransaction.commit();
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void checkLocationPermission() {

        if (getActivity().checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            //permission granted, just go on
            openLocationFragment();
        } else {
            if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                Toast.makeText(getActivity(), "Zum Anzeigen einer Karte wird diese Berechtigung benötigt!", Toast.LENGTH_LONG).show();
                //Intent i = new Intent();
                //i.putExtra(Constants.PERMISSION_DENIED, "permission");
                //setResult(RESULT_OK, i);
                //finish();
            } else {
                //nothing to do here -> user wants to use upload first time
            }
            //ask for permission
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, Constants.REQUEST_ID_FOR_LOCATION_PERMISSION);
        }
    }

    public static void closeKeyboard(Context c, IBinder windowToken) {
        InputMethodManager mgr = (InputMethodManager) c.getSystemService(Context.INPUT_METHOD_SERVICE);
        mgr.hideSoftInputFromWindow(windowToken, 0);
    }

    //TODO soft pad schließt nicht!!!!!
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        closeKeyboard(getActivity(), keywords.getWindowToken());

    }

    private void performSearch() {
        //store last search in sharedPrefs for Ebay search
        storeLastSearch(keywords.getText().toString());

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
        data.putExtra(Constants.TITLE, keywords.getText());
        getActivity().setResult(RESULT_OK, data);
        getActivity().finish();
    }

    public void hideSoftKeyboard() {
        if (getActivity().getCurrentFocus() != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
        }
    }

    public void showSoftKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        view.requestFocus();
        inputMethodManager.showSoftInput(view, 0);
    }

    public void adaptLayoutForPrice(String from, String to) {
        Log.d("CONAN", "priceTo: " + to);
        if (getString(R.string.price_does_not_matter).equals(from)) {
            price.setText("");
            price.setHint(R.string.hint_for_price);
        } else if ("".equals(from) && "".equals(to)) {
            price.setHint(R.string.hint_for_price);
        } else if (to.equals(Constants.MAX_PRICE.toString())) {
            price.setText("");
            price.setHint(R.string.hint_for_price);
        } else {
            price.setText(from + "€" + " bis " + to + "€");
        }
    }

    private void saveSearch() {
        followSearch.setClickable(false);
        showProgress();

        String description = keywords.getText().toString();
        Service service = new Service();

        service.saveSearchObserv(description, getMinPrice(), getMaxPrice(), getLat(), getLng(), getDistance(), getUserToken())
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<String>() {
                    @Override
                    public void onCompleted() {
                        followSearch.setClickable(true);
                        disableProgress();
                        new ConfirmFollowSearchDialog().show(getActivity().getSupportFragmentManager(), null);
                    }

                    @Override
                    public void onError(Throwable e) {
                        followSearch.setClickable(true);
                        disableProgress();
                        new ShowNetworkProblemDialog().show(getActivity().getSupportFragmentManager(), null);
                        Log.d("CONAN", "error saving searches: " + e.getMessage());
                    }

                    @Override
                    public void onNext(String result) {
                    }
                });
    }

    private void showProgress() {
        progressBar.setVisibility(View.VISIBLE);
    }

    private void disableProgress() {
        progressBar.setVisibility(View.GONE);
    }

    private int getMinPrice() {
        String minPrice = getActivity().getSharedPreferences(Constants.USER_PRICE_RANGE, MODE_PRIVATE).getString(Constants.PRICE_FROM, "");
        if ("".equals(minPrice)) {
            return 0;
        } else {
            return (getString(R.string.price_does_not_matter).equals(minPrice)) ? 0 : Integer.parseInt(minPrice);
        }
    }

    private int getMaxPrice() {
        String maxPrice = getActivity().getSharedPreferences(Constants.USER_PRICE_RANGE, MODE_PRIVATE).getString(Constants.PRICE_TO, "");
        if ("".equals(maxPrice)) {
            return Constants.MAX_PRICE;
        } else {
            return (getString(R.string.price_does_not_matter).equals(maxPrice)) ? Constants.MAX_PRICE : Integer.parseInt(maxPrice);
        }
    }

    public String getPrice(String price) {
        //TODO hier stimmt noch was nicht, wenn der wert für MIn und MAX price initial noch nicht gesetzt ist
        if (getActivity().getSharedPreferences(USER_PRICE_RANGE, 0).getString(price, "") != null) {
            return getActivity().getSharedPreferences(USER_PRICE_RANGE, 0).getString(price, "");
        } else {
            return getString(R.string.price_does_not_matter);
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
        if (((SearchActivity) getActivity()).getSupportActionBar() != null) {
            ((SearchActivity) getActivity()).getSupportActionBar()
                    .setSubtitle("in " + location.getString(Constants.LOCATION, "") + ((DISTANCE_INFINITY.equals(distance)) ? (" Unbegrenzt") : (" (+" + location.getInt(Constants.DISTANCE, 0) / 1000 + " km)")));
        }
    }

    private String getLocationString() {
        SharedPreferences location = getActivity().getSharedPreferences(Constants.USERS_LOCATION, 0);
        int distance = location.getInt(Constants.DISTANCE, DISTANCE_INFINITY);
        return location.getString(Constants.LOCATION, "") + ((DISTANCE_INFINITY.equals(distance)) ? (" Unbegrenzt") : (" (+" + location.getInt(Constants.DISTANCE, 0) / 1000 + " km)"));
    }

    public String getUserToken() {
        return getActivity().getSharedPreferences(SHARED_PREFS_USER_INFO, 0).getString(Constants.USER_TOKEN, "");
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


    private void storeLastSearch(String keyword) {
        SharedPreferences settings = getActivity().getSharedPreferences(LAST_SEARCH, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(Constants.KEYWORDS, keyword);
        editor.apply();
    }
}
