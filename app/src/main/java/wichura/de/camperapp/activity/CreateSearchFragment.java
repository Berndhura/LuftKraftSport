package wichura.de.camperapp.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import wichura.de.camperapp.R;

/**
 * Created by ich on 11.12.2016.
 *
 */

public class CreateSearchFragment extends Fragment {

    public CreateSearchFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.create_search_fragment, container, false);
    }
}
