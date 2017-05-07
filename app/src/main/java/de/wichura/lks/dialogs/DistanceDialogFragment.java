package de.wichura.lks.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import de.wichura.lks.R;

import static android.widget.ListPopupWindow.WRAP_CONTENT;

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

        LinearLayout view = new LinearLayout(getActivity());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
        view.setLayoutParams(params);
        view.setOrientation(LinearLayout.VERTICAL);

        final SeekBar seek = new SeekBar(getActivity());
        seek.setMax(100);
        seek.setPadding(50, 20, 50, 20);

        TextView showDist = new TextView(getActivity());
        showDist.setPadding(50, 50, 50, 20);

        seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            public void onProgressChanged(SeekBar seekBar, int progressV, boolean fromUser) {
                showDist.setText(getDistanceString(seekBar));
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                showDist.setText(getDistanceString(seekBar));
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
                showDist.setText(getDistanceString(seekBar));
                mListener.onDistanceComplete(seek.getProgress());
            }
        });

        view.addView(showDist);
        view.addView(seek);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.distence_request)
                .setView(view)
                .setPositiveButton(android.R.string.yes, (dialog, which) ->
                        mListener.onDistanceComplete(seek.getProgress()))
                .setNegativeButton(android.R.string.no, (dialog, which) ->
                        getDialog().cancel());
        return builder.create();
    }

    private String getDistanceString(SeekBar seekBar) {
        return (seekBar.getProgress() == 100) ? "Überall" : seekBar.getProgress() * 5 + " km";
    }
}
