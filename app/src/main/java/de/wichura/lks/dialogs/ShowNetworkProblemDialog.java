package de.wichura.lks.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import de.wichura.lks.R;

/**
 * Created by Bernd Wichura on 25.04.2017.
 * Luftkraftsport
 */

public class ShowNetworkProblemDialog extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.confirm_network_error)
                .setMessage(R.string.confirm_network_error_text)
                .setIcon(R.drawable.ic_error_outline_red_600_24dp)
                .setPositiveButton(android.R.string.yes, (dialog, which) -> getDialog().dismiss())
                .create();
    }
}