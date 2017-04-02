package de.wichura.lks.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import de.wichura.lks.R;
import de.wichura.lks.mainactivity.Constants;

/**
 * Created by ich on 02.04.2017.
 * Luftkraftsport
 */

public class SetPriceDialog extends DialogFragment {

    private TextView priceFromTv;
    private TextView  priceToTv;

    public interface OnCompleteListener {
        void onPriceRangeComplete(String priceFrom, String priceTo);
    }

    private SetPriceDialog.OnCompleteListener mListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            this.mListener = (SetPriceDialog.OnCompleteListener)activity;
        }
        catch (final ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnCompleteListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {


        AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.price_range)
                .setView(R.layout.set_price_activity)
                .setCancelable(false)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        priceFromTv = (TextView) getDialog().findViewById(R.id.priceFrom);
                        priceToTv = (TextView) getDialog().findViewById(R.id.priceTo);

                        priceFromTv.setOnClickListener(view -> priceFromTv.setText(""));
                        priceFromTv.setOnTouchListener((view, event) -> {
                            priceFromTv.setText("");
                            return false;
                        });

                        priceToTv.setOnClickListener(view -> priceToTv.setText(""));
                        priceToTv.setOnTouchListener((view, event) -> {
                            priceToTv.setText("");
                            return false;
                        });

                        getPrices();
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getDialog().cancel();
                    }
                })
                .create();

        //((TextView)dialog.findViewById(R.id.priceFrom)).setText("");

        return dialog;
    }

    private void getPrices() {
        String priceFrom = priceFromTv.getText().toString();
        if ("Beliebig".equals(priceFrom)) {
            priceFrom = "0";
        } else {
            priceFrom = priceFromTv.getText().toString();
        }

        String priceTo = priceToTv.getText().toString();
        if ("Beliebig".equals(priceTo)) {
            //TODO: hoechstgrenze unklar
            priceTo = Constants.MAX_PRICE.toString();
        } else {
            priceTo = priceToTv.getText().toString();
        }
        mListener.onPriceRangeComplete(priceFrom, priceTo);
    }
}

