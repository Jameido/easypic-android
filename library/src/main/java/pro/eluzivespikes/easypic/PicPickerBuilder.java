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

package pro.eluzivespikes.easypic;

import android.app.Activity;
import android.content.Intent;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;

/**
 * Created by Luca Rossi on 03/02/2018.
 */
public class PicPickerBuilder {

    private PicPickerImpl mPicPicker;

    public PicPickerBuilder(Activity activity) {
        mPicPicker = new ActivityPicPicker(activity);
    }

    public PicPickerBuilder(Fragment fragment) {
        mPicPicker = new FragmentPicPicker(fragment);
    }

    /**
     * Set the result listener to be invoked when the picture has been successfully processed.
     *
     * @param resultListener the result listener
     */
    public PicPickerBuilder withResultListener(PicPickerImpl.OnResultListener resultListener) {
        this.mPicPicker.setOnResultListener(resultListener);
        return this;
    }

    /**
     * Sets the coordinator layout used to display the snackbar when rationale permissions have to
     * be asked to the user.
     *
     * @param coordinatorLayout the given coordinator
     */
    public PicPickerBuilder withCoordinatorLayout(CoordinatorLayout coordinatorLayout) {
        this.mPicPicker.setCoordinatorLayout(coordinatorLayout);
        return this;
    }

    /**
     * Sets the requested size for the bigger side of the picture, 0 if no compression is required.
     *
     * @param pictureSize the requested size (0 if no compression is required)
     */
    public PicPickerBuilder withPictureSize(int pictureSize) {
        this.mPicPicker.setPictureSize(pictureSize);
        return this;
    }

    /**
     * Sets the request code used when invoking
     * {@link Activity#startActivityForResult(Intent, int)} or
     * {@link android.support.v4.app.Fragment#startActivityForResult(Intent, int)}
     *
     * @param requestCode the given request code
     */
    public PicPickerBuilder withRequestCode(int requestCode) {
        this.mPicPicker.setRequestCode(requestCode);
        return this;
    }

    /**
     * Sets a bool indicating if also the gallery activities should be shown
     *
     * @param showGallery the bool
     */
    public PicPickerBuilder withGallery(boolean showGallery) {
        this.mPicPicker.setShowGallery(showGallery);
        return this;
    }

    public PicPickerBuilder withFileName(String fileName) {
        this.mPicPicker.setFilename(fileName);
        return this;
    }

    /**
     * Sets a list of modes on how the resulting picture can be returned, possible values are
     * {@link PicPicker.PickerMode#BITMAP}
     * {@link PicPicker.PickerMode#BYTES}
     * {@link PicPicker.PickerMode#FILE}
     *
     * @param modes the chosen modes
     */
    public PicPickerBuilder withModes(int... modes) {
        this.mPicPicker.setModes(modes);
        return this;
    }

    /**
     * Sets how the resulting image should be scaled to the requested size, possible values are:
     * {@link PicPicker.ScaleType#KEEP_RATIO}
     * {@link PicPicker.ScaleType#CROP}
     * {@link PicPicker.ScaleType#SCALE_XY}
     *
     * @param scaleType
     */
    public PicPickerBuilder withScaleType(@PicPicker.ScaleType int scaleType) {
        this.mPicPicker.setScaleType(scaleType);
        return this;
    }

    public PicPicker build() {
        return mPicPicker;
    }
}
