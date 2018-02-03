/*
 * Copyright 2017.  Luca Rossi
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 *
 */

package pro.eluzivespikes.easyphotopicker;

import android.graphics.Bitmap;
import android.support.annotation.Nullable;

import java.io.File;

/**
 * Created by Luca Rossi on 02/02/2018.
 */

public class PickerResult {

    private @Nullable
    File mFile;
    private @Nullable
    Bitmap mBitmap;
    private @Nullable
    byte[] mBytes;

    void setFile(@Nullable File aFile) {
        mFile = aFile;
    }

    void setBitmap(@Nullable Bitmap aBitmap) {
        mBitmap = aBitmap;
    }

    void setBytes(@Nullable byte[] aBytes) {
        mBytes = aBytes;
    }

    @Nullable
    public final File getFile() {
        return mFile;
    }

    @Nullable
    public final Bitmap getBitmap() {
        return mBitmap;
    }

    @Nullable
    public final byte[] getBytes() {
        return mBytes;
    }
}
