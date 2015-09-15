package wichura.de.camperapp.bitmap;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import java.io.IOException;

/**
 * Created by ich on 01.09.2015.
 */
public class BitmapHelper {

    private final Context context;
    private int maxWidth = 350;
    private int maxHeight = 350;

    public BitmapHelper(Context context)
    {
        this.context=context;
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
        float scale = Math.round((float)bitmap.getHeight()/maxWidth);
        return resize(uri);//todo add scale, check if 1 or bigger...
    }

    public Bitmap resize(String uri){
        Bitmap bitmap = null;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(
                    context.getContentResolver(), Uri.parse(uri));
        } catch (final IOException e) {
            Log.i("MyActivity", "MyClass.getView() URLS " + "BITMAP FEHLER");
            e.printStackTrace();
        }
//get the original size
        int orignalHeight = bitmap.getHeight();
        int orignalWidth = bitmap.getWidth();

        Bitmap thump = Bitmap.createScaledBitmap(bitmap,(int)(orignalWidth*0.1), (int)(orignalHeight*0.1), true);

//initialization of the scale
       // int resizeScale = 1;
//get the good scale
        //if ( orignalWidth > maxWidth || orignalHeight > maxHeight ) {
        //    final int heightRatio = Math.round((float) orignalHeight / (float) maxHeight);
        //    final int widthRatio = Math.round((float) orignalWidth / (float) maxWidth);
        //    resizeScale = heightRatio < widthRatio ? heightRatio : widthRatio;
       // }
//put the scale instruction (1 -> scale to (1/1); 8-> scale to 1/8)
        //opts.inSampleSize = resizeScale;
        //opts.inJustDecodeBounds = false;
//get the futur size of the bitmap
        //int bmSize = (orignalWidth / resizeScale) * (orignalHeight / resizeScale) * 4;
//check if it's possible to store into the vm java the picture
        //if ( Runtime.getRuntime().freeMemory() > bmSize ) {
//decode the file
          //  bp = BitmapFactory.decodeFile(path, opts);
        //} else
         //   return null;
        return thump;
    }
}
