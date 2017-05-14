package de.wichura.lks.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import de.wichura.lks.R;

/**
 * Created by Bernd Wichura on 14.05.2017.
 * Luftkraftsport
 */

public class ShowUserNotActivatedDialog extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.confirm_user_not_activated_error)
                .setMessage(R.string.confirm_user_not_activated_error_text)
                .setIcon(R.drawable.ic_error_outline_red_600_24dp)
                .setPositiveButton(android.R.string.yes, (dialog, which) -> getDialog().dismiss())
                .setNegativeButton("Aktivieren", (dialog, which) -> {
                    Toast.makeText(getActivity(), "Aktivieren!!!!!!!!", Toast.LENGTH_SHORT).show();
                })
                .create();
    }
}
