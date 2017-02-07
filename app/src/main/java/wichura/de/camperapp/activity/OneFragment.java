package wichura.de.camperapp.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.List;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import wichura.de.camperapp.R;
import wichura.de.camperapp.http.Service;
import wichura.de.camperapp.mainactivity.Constants;
import wichura.de.camperapp.models.SearchItem;
import wichura.de.camperapp.presentation.SearchesPresenter;
import wichura.de.camperapp.util.Utility;

import static wichura.de.camperapp.mainactivity.Constants.SHARED_PREFS_USER_INFO;

/**
 * Created by ich on 11.12.2016.
 * Camper App
 */

public class OneFragment extends Fragment {

    private Service service;
    private Utility utils;

    public OneFragment() {
        service = new Service();
        utils = new Utility(getActivity());
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        final Button submitButton = (Button) getView().findViewById(R.id.get_searches);
        submitButton.setOnClickListener((v) -> {
            service.findSearchesObserv(getUserToken())
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Subscriber<List<SearchItem>>() {
                        @Override
                        public void onCompleted() {
                        }

                        @Override
                        public void onError(Throwable e) {
                            Log.d("CONAN", "error loading searches: " + e.getMessage());
                        }

                        @Override
                        public void onNext(List<SearchItem> searchItem) {
                            updateSearches(searchItem);
                        }
                    });
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        return inflater.inflate(R.layout.fragment_one, container, false);
    }


    public void updateSearches(List<SearchItem> searchItem) {
        //...
        Log.d("CONAN", searchItem.get(0).getDescription()+" "+searchItem.get(1).getId());
    }

    public String getUserToken() {
        return getActivity().getSharedPreferences(SHARED_PREFS_USER_INFO, 0).getString(Constants.USER_TOKEN, "");
    }
}
