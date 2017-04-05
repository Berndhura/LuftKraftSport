package de.wichura.lks.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

/**
 * Created by ich on 05.04.2017.
 * Luftkraftsport
 */

public class FileNameParcelable implements Parcelable {

    private String mFileName;

    @Override
    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(mFileName);
    }

    public FileNameParcelable(String fileName) {
        mFileName = fileName;
    }

    private FileNameParcelable(Parcel in) {
        Log.d("CONAN", "parcel in");
        mFileName = in.readString();
    }

    public String getFileName() {
        return mFileName;
    }

    public static final Parcelable.Creator<FileNameParcelable> CREATOR
            = new Parcelable.Creator<FileNameParcelable>() {
        public FileNameParcelable createFromParcel(Parcel in) {
            Log.d("CONAN", "createFromParcel()");
            return new FileNameParcelable(in);
        }

        public FileNameParcelable[] newArray(int size) {
            Log.d("CONAN", "createFromParcel() newArray ");
            return new FileNameParcelable[size];
        }
    };
}
