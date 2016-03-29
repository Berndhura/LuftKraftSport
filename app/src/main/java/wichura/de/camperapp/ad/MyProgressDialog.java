package wichura.de.camperapp.ad;

import android.app.ProgressDialog;
import android.content.Context;

import wichura.de.camperapp.R;

/**
 * Created by Bernd Wichura on 29.03.2016.
 */
public class MyProgressDialog extends ProgressDialog {
    public MyProgressDialog(Context cxt) {
        super(cxt);
    }

    @Override
    public void show() {

        super.show();
        setContentView(R.layout.customprog);
    }
}
