package de.wichura.lks.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;

import de.wichura.lks.R;

/**
 * Created by Bernd Wichura on 05.05.2017.
 * Luftkraftsport
 */

public class DistanceDialogFragment extends DialogFragment {

    public interface OnCompleteDistanceListener {
        void onDistanceComplete(Integer distance);
    }

    private OnCompleteDistanceListener mListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            this.mListener = (OnCompleteDistanceListener) activity;
        } catch (final ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnCompleteDistanceListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.distence_request)
                .setView(R.layout.distance_dialog_layout)
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
        theButton.setOnClickListener(new DistanceDialogFragment.CustomListener(alertDialog));

        return alertDialog;
    }

    private class CustomListener implements View.OnClickListener {
        private final Dialog dialog;

        private CustomListener(Dialog dialog) {
            this.dialog = dialog;
        }

        @Override
        public void onClick(View v) {

            SeekBar seekBar = ((SeekBar) getDialog().findViewById(R.id.distance_seek_for_dialog));
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    mListener.onDistanceComplete(seekBar.getProgress() * 5);
                    Log.d("COANAN" , "onStopTrackingTouch"+ seekBar);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    Log.d("COANAN" , "onStartTrackingTouch"+ seekBar);
                    mListener.onDistanceComplete(seekBar.getProgress());
                }

                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                    Log.d("COANAN" , "onprochanges"+ seekBar);
                    Integer distance = progress * 5;
                    mListener.onDistanceComplete(progress);
                }
            });
            mListener.onDistanceComplete(seekBar.getProgress());
            dismiss();
        }
    }
}
