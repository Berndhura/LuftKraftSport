package wichura.de.camperapp.bitmap;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

//import com.squareup.picasso.Picasso;

import java.io.IOException;

/**
 * Created by ich on 01.09.2015.
 */
public class BitmapHelper {

    private final Context context;
    private int maxWidth = 350;
    private int maxHeight = 350;

    public BitmapHelper(Context context) {
        this.context = context;
    }

    public Bitmap getThump(String uri) {
        Bitmap bitmap = null;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(
                    context.getContentResolver(), Uri.parse(uri));
        } catch (final IOException e) {
            e.printStackTrace();
        }
        //calculate scale
        float scale = Math.round((float) bitmap.getHeight() / maxWidth);
        return resize(uri);//todo add scale, check if 1 or bigger...
    }

    public Bitmap resize(String uri) {

        /*Picasso.with(this.context)
                .load(uri)
                .resize(100,100)
                .into();
*/

        Bitmap bitmap = null;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(
                    context.getContentResolver(), Uri.parse(uri));
        } catch (final IOException e) {
            Log.i("MyActivity", "MyClass.getView() URLS " + "BITMAP FEHLER");
            e.printStackTrace();
        }

        float orignalHeight = bitmap.getHeight();
        int orignalWidth = bitmap.getWidth();
        int factor = Math.round(500 / orignalHeight);
        Log.i("Conan", "Factor: " + factor);

        Bitmap thump = Bitmap.createScaledBitmap(bitmap, (int) (orignalWidth * factor), (int) (orignalHeight * factor), true);


        return thump;
    }
}
