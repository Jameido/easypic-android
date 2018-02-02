package pro.eluzivespikes.easyphotopicker;

import android.graphics.Bitmap;

import java.io.File;

/**
 * Created by Luca Rossi on 02/02/2018.
 */

public class PickerResult {

    public File mFile;
    public Bitmap mBitmap;
    public byte[] mBytes;

    public void setFile(File aFile) {
        mFile = aFile;
    }

    public void setBitmap(Bitmap aBitmap) {
        mBitmap = aBitmap;
    }

    public void setBytes(byte[] aBytes) {
        mBytes = aBytes;
    }

    public File getFile() {
        return mFile;
    }

    public Bitmap getBitmap() {
        return mBitmap;
    }

    public byte[] getBytes() {
        return mBytes;
    }
}
