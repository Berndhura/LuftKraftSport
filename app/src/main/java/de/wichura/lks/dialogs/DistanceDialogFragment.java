package de.wichura.lks.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
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


        /*
         <LinearLayout
        android:id="@+id/location_distance_view"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_alignParentTop="true"
        android:layout_margin="8dp"
        android:background="#FFFFFF"
        android:orientation="vertical">

        <TextView
            android:id="@+id/distance_header"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:padding="8dp"
            android:text="Umkreis:" />

        <SeekBar
            android:id="@+id/distance_seek_for_dialog"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:padding="8dp" />
         */

        LinearLayout view = new LinearLayout(getActivity());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
        view.setLayoutParams(params);
        view.setOrientation(LinearLayout.VERTICAL);


        final SeekBar seek = new SeekBar(getActivity());
        seek.setMax(500);

        TextView showDist = new TextView(getActivity());

        seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progress = 0;

            public void onProgressChanged(SeekBar seekBar, int progressV, boolean fromUser) {
                progress = progressV;
                String v = seekBar.getProgress() + " km";
                showDist.setText(v);
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                String v = seekBar.getProgress() + " km";
                showDist.setText(v);

            }

            public void onStopTrackingTouch(SeekBar seekBar) {
                String v = seekBar.getProgress() + " km";
                showDist.setText(v);
                mListener.onDistanceComplete(seek.getProgress());
            }
        });

        view.addView(showDist);
        view.addView(seek);


        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.distence_request)
                .setView(view)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mListener.onDistanceComplete(seek.getProgress());
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getDialog().cancel();
                    }
                });

        return builder.create();
    }
}
