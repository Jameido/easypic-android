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
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;

/**
 * Created by Luca Rossi on 03/02/2018.
 */
public class EasyPhotoPickerBuilder {

    private EasyPhotoPickerImpl mEasyPhotoPicker;

    public EasyPhotoPickerBuilder(Activity activity) {
        mEasyPhotoPicker = new ActivityEasyPhotoPicker(activity);
    }

    public EasyPhotoPickerBuilder(Fragment fragment) {
        mEasyPhotoPicker = new FragmentEasyPhotoPicker(fragment);
    }

    /**
     * Set the result listener to be invoked when the picture has been successfully processed.
     *
     * @param resultListener the result listener
     */
    public EasyPhotoPickerBuilder withResultListener(EasyPhotoPickerImpl.OnResultListener resultListener) {
        this.mEasyPhotoPicker.setOnResultListener(resultListener);
        return this;
    }

    /**
     * Sets the coordinator layout used to display the snackbar when rationale permissions have to
     * be asked to the user.
     *
     * @param coordinatorLayout the given coordinator
     */
    public EasyPhotoPickerBuilder withCoordinatorLayout(CoordinatorLayout coordinatorLayout) {
        this.mEasyPhotoPicker.setCoordinatorLayout(coordinatorLayout);
        return this;
    }

    /**
     * Sets the requested size for the bigger side of the picture, 0 if no compression is required.
     *
     * @param pictureSize the requested size (0 if no compression is required)
     */
    public EasyPhotoPickerBuilder withPictureSize(int pictureSize) {
        this.mEasyPhotoPicker.setPictureSize(pictureSize);
        return this;
    }

    /**
     * Sets the request code used when invoking
     * {@link Activity#startActivityForResult(Intent, int)} or
     * {@link android.support.v4.app.Fragment#startActivityForResult(Intent, int)}
     *
     * @param requestCode the given request code
     */
    public EasyPhotoPickerBuilder withRequestCode(int requestCode) {
        this.mEasyPhotoPicker.setRequestCode(requestCode);
        return this;
    }

    /**
     * Sets a bool indicating if also the gallery activities should be shown
     *
     * @param showGallery the bool
     */
    public EasyPhotoPickerBuilder withGallery(boolean showGallery) {
        this.mEasyPhotoPicker.setShowGallery(showGallery);
        return this;
    }

    public EasyPhotoPickerBuilder withFileName(String fileName) {
        this.mEasyPhotoPicker.setFilename(fileName);
        return this;
    }

    /**
     * Sets a list of modes on how the resulting picture can be returned, possible values are
     * {@link EasyPhotoPickerImpl.PickerMode#BITMAP}
     * {@link EasyPhotoPickerImpl.PickerMode#BYTES}
     * {@link EasyPhotoPickerImpl.PickerMode#FILE}
     *
     * @param modes the chosen modes
     */
    public EasyPhotoPickerBuilder withModes(int... modes) {
        this.mEasyPhotoPicker.setModes(modes);
        return this;
    }

    public EasyPhotoPicker build() {
        return mEasyPhotoPicker;
    }
}
