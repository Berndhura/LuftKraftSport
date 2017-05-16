package de.wichura.lks.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.EditText;

import de.wichura.lks.R;

/**
 * Created by Bernd Wichura on 14.05.2017.
 * Luftkraftsport
 */

public class ShowUserNotActivatedDialog extends DialogFragment {

    private String email;
    private String password;

    public interface OnCompleteActivationCodeListener {
        void onActivationCodeComplete(String email, String password, String code);
    }

    private ShowUserNotActivatedDialog.OnCompleteActivationCodeListener mListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            this.mListener = (ShowUserNotActivatedDialog.OnCompleteActivationCodeListener) activity;
        } catch (final ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnCompleteActivationCodeListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Bundle bundle = getArguments();
        if (bundle.getString("password") != null) {
            email = bundle.getString("email");
            password = bundle.getString("password");
        }

        EditText code = new EditText(getActivity());
        code.setVisibility(View.VISIBLE);
        code.setPadding(50, 50, 50, 50);
        code.setHint("code...");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.confirm_user_not_activated_error)
                .setMessage(R.string.confirm_user_not_activated_error_text)
                .setIcon(R.drawable.ic_error_outline_red_600_24dp)
                .setView(code)
                .setNegativeButton("Später", (dialog, which) -> getDialog().dismiss())
                .setPositiveButton("Aktivieren", (dialog, which) -> {
                    if ("".equals(code.getText().toString())) {
                        //nothing here
                    } else {
                        getDialog().dismiss();
                        mListener.onActivationCodeComplete(email, password, code.getText().toString());
                    }
                });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();

        return alertDialog;
    }
}
