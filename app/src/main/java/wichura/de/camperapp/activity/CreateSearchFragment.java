package wichura.de.camperapp.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import wichura.de.camperapp.R;
import wichura.de.camperapp.http.Service;
import wichura.de.camperapp.mainactivity.Constants;
import wichura.de.camperapp.models.SearchItem;

import static wichura.de.camperapp.mainactivity.Constants.SHARED_PREFS_USER_INFO;

/**
 * Created by ich on 11.12.2016.
 *
 */

public class CreateSearchFragment extends Fragment {

    private EditText description;
    private EditText priceFrom;
    private EditText priceTo;

    public CreateSearchFragment() {
        service = new Service();
    }
    private Service service;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.create_search_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        Button saveButton = (Button)getView().findViewById(R.id.save_new_search_button);
        description = (EditText) getView().findViewById(R.id.search_description_et);
        priceFrom = (EditText) getView().findViewById(R.id.price_from_et);
        priceTo = (EditText) getView().findViewById(R.id.price_to_et);


        saveButton.setOnClickListener((v)-> {
            saveNewSearch(description.getText().toString(), Integer.parseInt(priceFrom.getText().toString()), Integer.parseInt(priceTo.getText().toString()));
        });
    }

    private void saveNewSearch(String description, Integer priceFrom, Integer priceTo) {
        service.saveSearchObserv(description, priceFrom, priceTo, 0.0f, 0.0f, 5000, getUserToken())
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<String>() {
                    @Override
                    public void onCompleted() {
                        Toast.makeText(getContext(), "Suche abgespeichert!", Toast.LENGTH_SHORT).show();
                        cleanText();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d("CONAN", "error saving searches: " + e.getMessage());
                    }

                    @Override
                    public void onNext(String result) {

                    }
                });

    }

    private void cleanText() {
        description.setText("");
        priceFrom.setText("");
        priceTo.setText("");
    }

    public String getUserToken() {
        return getActivity().getSharedPreferences(SHARED_PREFS_USER_INFO, 0).getString(Constants.USER_TOKEN, "");
    }
}
