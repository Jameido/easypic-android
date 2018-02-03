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

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.CallSuper;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by Luca Rossi on 03/02/2018.
 */

public interface EasyPhotoPicker {


    /**
     * If the user has given all the necessary permissions opens the picker
     * If not asks the permissions
     */
    void openPicker();

    /**s
     * Must be called in {@link Activity#onDestroy()} to avoid memory leaks.
     */
    void onDestroy();

    /**
     * Called from {@link Activity#onRequestPermissionsResult(int, String[], int[])}
     * and if the necessary permissions have been given opens the picker.
     *
     * @param requestCode  the permissions request code
     * @param permissions  the permissions asked
     * @param grantResults the grant permissions results
     */
    void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults);

    /**
     * Called from {@link Activity#onActivityResult(int, int, Intent)} and if the request code
     * matches with the one used processes the result.
     *
     * @param requestCode the result request code
     * @param resultCode  the result result code
     * @param data        the data from the result
     */
    void onActivityResult(int requestCode, int resultCode, Intent data);

    /**
     * Used to indicate the type of
     */
    @Target({ElementType.FIELD,
            ElementType.LOCAL_VARIABLE,
            ElementType.PARAMETER,
            ElementType.ANNOTATION_TYPE})
    @IntDef({
            EasyPhotoPickerImpl.PickerMode.BITMAP,
            EasyPhotoPickerImpl.PickerMode.BYTES,
            EasyPhotoPickerImpl.PickerMode.FILE
    })
    @Retention(RetentionPolicy.SOURCE)
    @interface PickerMode {
        /**
         * A bitmap representing the image will be returned
         */
        int BITMAP = 0;
        /**
         * A byte array representing the image will be returned
         */
        int BYTES = 1;
        /**
         * A file containing the image will be returned
         */
        int FILE = 2;
    }

    interface OnResultListener {
        void onPickPhotoSuccess(@NonNull PickerResult pickerResult);

        void onPickPhotoFailure(@NonNull Exception exception);
    }
}
