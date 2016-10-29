package wichura.de.camperapp.bitmap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;


/**
 * Created by ich on 01.09.2015.
 * Camper App
 */
public class BitmapHelper {

    private final Context mContext;


    public BitmapHelper(Context context) {
        this.mContext = context;
    }

    public File saveBitmapToFile(File file) {
        try {

            // BitmapFactory options to downsize the image
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            o.inSampleSize = 6;
            // factor of downsizing the image

            FileInputStream inputStream = new FileInputStream(file);
            //Bitmap selectedBitmap = null;
            BitmapFactory.decodeStream(inputStream, null, o);
            inputStream.close();

            // The new size we want to scale to
            final int REQUIRED_SIZE = 75;

            // Find the correct scale value. It should be the power of 2.
            int scale = 1;
            while (o.outWidth / scale / 2 >= REQUIRED_SIZE &&
                    o.outHeight / scale / 2 >= REQUIRED_SIZE) {
                scale *= 2;
            }

            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            inputStream = new FileInputStream(file);

            Bitmap selectedBitmap = BitmapFactory.decodeStream(inputStream, null, o2);
            inputStream.close();

            //TODO save a local copy, upload and onSuccess delete the local copy!!!!
            // here i override the original image file
            //file.createNewFile();
            //new
            File outputDir = mContext.getCacheDir(); // mContext being the Activity pointer
            File outputFile = File.createTempFile("prefix", "extension", outputDir);
            //

            //FileOutputStream outputStream = new FileOutputStream(file);
            FileOutputStream outputStream = new FileOutputStream(outputFile);

            selectedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);

            return outputFile;
        } catch (Exception e) {
            return null;
        }
    }
}



