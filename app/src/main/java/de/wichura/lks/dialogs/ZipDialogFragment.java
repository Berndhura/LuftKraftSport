package de.wichura.lks.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.widget.EditText;

import de.wichura.lks.R;

/**
 * Created by ich on 31.03.2017.
 * Luftkraftsport
 */

public class ZipDialogFragment extends DialogFragment {

    public interface OnCompleteListener {
        void onComplete(String zipCode);
    }

    private OnCompleteListener mListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            this.mListener = (OnCompleteListener)activity;
        }
        catch (final ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnCompleteListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.zip_request)
                .setView(R.layout.zip_dialog_layout)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String zipCode = ((EditText)getDialog().findViewById(R.id.new_article_zip_code)).getText().toString();
                        mListener.onComplete(zipCode);
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getDialog().cancel();
                    }
                })
                .create();
    }
}
