package de.wichura.lks.dialogs;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;

import de.wichura.lks.R;
import de.wichura.lks.mainactivity.Constants;

import static de.wichura.lks.mainactivity.Constants.SHARED_PREFS_WELCOME_DIALOG;

/**
 * Created by Bernd Wichura on 17.05.2017.
 * Luftkraftsport
 */

public class WelcomeDialog extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        CheckBox showAgain = new CheckBox(getActivity());
        showAgain.setVisibility(View.VISIBLE);
        showAgain.setPadding(10, 50, 50, 50);
        showAgain.setHint("Verstanden, nicht noch mal zeigen!");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.welcome_title)
                .setIcon(R.drawable.lks_app_logo)
                .setMessage(R.string.welcome_text)
                .setView(showAgain)
                .setPositiveButton("Schließen", (dialog, which) -> {
                    if (showAgain.isChecked()) {
                        showAgain(false);
                    } else {
                        showAgain(true);
                    }
                });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();

        return alertDialog;
    }

    private void showAgain(boolean showAgain) {
        Log.d("CONAN" ,"show again: " +showAgain);
        SharedPreferences settings = getActivity().getSharedPreferences(SHARED_PREFS_WELCOME_DIALOG, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(Constants.SHOW_WELCOME, showAgain);
        editor.apply();
    }
}
