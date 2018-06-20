package de.wichura.lks.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import de.wichura.lks.R;
import de.wichura.lks.mainactivity.Constants;

/**
 * Created by Bernd Wichura on 02.04.2017.
 * Luftkraftsport
 */

public class SetPriceDialog extends DialogFragment {

    private TextView priceFromTv;
    private TextView priceToTv;
    private CheckBox priceDnM;

    public interface OnCompleteListener {
        void onPriceRangeComplete(String priceFrom, String priceTo);
    }

    private SetPriceDialog.OnCompleteListener mListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            this.mListener = (SetPriceDialog.OnCompleteListener) activity;
        } catch (final ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnCompleteListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.price_range)
                .setView(R.layout.set_price_activity)
                .setCancelable(false)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //nothing here
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getDialog().cancel();
                    }
                });


        AlertDialog alertDialog = builder.create();
        alertDialog.show();
        Button theButton = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
        theButton.setOnClickListener(new CustomListener(alertDialog));


        return alertDialog;
    }

    private class CustomListener implements View.OnClickListener {
        private final Dialog dialog;

        private CustomListener(Dialog dialog) {
            this.dialog = dialog;
        }

        @Override
        public void onClick(View v) {

            priceFromTv = getDialog().findViewById(R.id.priceFrom);
            priceToTv = getDialog().findViewById(R.id.priceTo);
            priceDnM = getDialog().findViewById(R.id.price_does_not_matter);

            if (validatePriceRange()) {
                getPrices();
                dismiss();
            } else {
                return;
            }
        }
    }

    private boolean validatePriceRange() {
        boolean valid = true;

        if (priceDnM.isChecked()) {
            return true;
        }

        String priceMin = priceFromTv.getText().toString();
        if (priceMin.isEmpty()) {
            priceFromTv.setError("Richtigen Wert angeben!");
            valid = false;
        } else if (Integer.parseInt(priceMin) >= Integer.MAX_VALUE) {
            priceFromTv.setError("Komm schon, etwas teuer oder?");
            valid = false;
        } else {
            priceFromTv.setError(null);
        }

        String priceMax = priceToTv.getText().toString();
        if (priceMax.isEmpty()) {
            priceToTv.setError("Richtigen Wert angeben!");
            valid = false;
        } else if (Integer.parseInt(priceMin) >= Integer.MAX_VALUE) {
            priceToTv.setError("Komm schon, etwas teuer oder?");
            valid = false;
        } else if (Integer.parseInt(priceMax) > Integer.parseInt(priceMax)) {
            priceToTv.setError("Maximum ist kleiner als das Minimum -> Quatsch!");
            valid = false;
        } else {
            priceToTv.setError(null);
        }

        return valid;
    }

    private void getPrices() {

        if (priceDnM.isChecked()) {
            mListener.onPriceRangeComplete(getString(R.string.price_does_not_matter), getString(R.string.price_does_not_matter));
        } else {

            String priceFrom = priceFromTv.getText().toString();

            if ("Beliebig".equals(priceFrom)) {
                priceFrom = "0";
            } else {
                priceFrom = priceFromTv.getText().toString();
            }

            String priceTo = priceToTv.getText().toString();
            if ("Beliebig".equals(priceTo)) {
                priceTo = Constants.MAX_PRICE.toString();
            } else {
                priceTo = priceToTv.getText().toString();
            }
            mListener.onPriceRangeComplete(priceFrom, priceTo);
        }
    }
}

