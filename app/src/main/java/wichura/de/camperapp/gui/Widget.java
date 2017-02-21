package wichura.de.camperapp.gui;

import android.app.Activity;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ich on 17.02.2017.
 * desurf
 */

public class Widget {

    private Widget() {}

    public static void addItemsOnSpinner(Activity activity, Spinner spinner) {

        List<String> list = new ArrayList<>();
        list.add("unbegrenzt");
        list.add("100 km");
        list.add("200 km");
        list.add("300 km");
        list.add("400 km");
        list.add("500 km");
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(activity.getBaseContext(), android.R.layout.simple_spinner_dropdown_item, list);
        //dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);
    }

    public static Long getDistanceFromSpinner(Spinner spinner) {
        if ("unbegrenzt".contains(String.valueOf(spinner.getSelectedItem()))) {
            return 10000000L;
        } else {
            String value = String.valueOf(spinner.getSelectedItem());
            return Long.parseLong(value.substring(0, value.indexOf(" ")));
        }
    }
}
