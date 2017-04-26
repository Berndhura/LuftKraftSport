package de.wichura.lks.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import de.wichura.lks.R;

/**
 * Created by Bernd Wichura on 07.04.2017.
 * Luftkraftsport
 */

public class ConfirmFollowSearchDialog extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.confirm_save_search)
                .setMessage(R.string.save_search_info_text)
                .setIcon(R.drawable.ic_add_alert_blue_grey_700_24dp)
                .setPositiveButton(android.R.string.yes, (dialog, which) -> getDialog().dismiss())
                .create();
    }
}
