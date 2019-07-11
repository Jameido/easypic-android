package dev.jameido.easypic

import android.app.Activity
import android.view.View
import androidx.fragment.app.Fragment

/**
 * @author Jameido
 * @since 3
 *
 * Builder to set the wanted parameters of [PicPicker]
 */
class PicPickerBuilder {

    private val mPicker: PicPickerImpl

    constructor(activity: Activity) {
        mPicker = ActivityPicPicker(activity)
    }

    constructor(fragment: Fragment) {
        mPicker = FragmentPicPicker(fragment)
    }

    /**
     * Set the success method to be invoked when the picture has been successfully processed.
     *
     * @param onPickSuccess the success method
     * @return the builder itself
     */
    fun withSuccessListener(onPickSuccess: (result: PickerResult) -> Unit = { _ -> }): PicPickerBuilder {
        mPicker.onPickSuccess = onPickSuccess
        return this
    }

    /**
     * Set the on failure method to be invoked when the pick process fails.
     *
     * @param onPickFailure the failure method
     * @return the builder itself
     */
    fun withFailureListener(onPickFailure: (exception: Exception) -> Unit = { _ -> }): PicPickerBuilder {
        mPicker.onPickFailure = onPickFailure
        return this
    }

    /**
     * Set the listener to be called al the end of picker process.
     * Is used for Java compatibility
     *
     * @param onPickResultListener the result listener
     * @return the builder itself
     */
    fun withResultListener(onPickResultListener: OnPickResultListener): PicPickerBuilder {
        mPicker.onPickResultListener = onPickResultListener
        return this
    }

    /**
     * Sets the root view used to display the [android.support.design.widget.Snackbar] when
     * rationale permissions have to be asked to the user.
     *
     * @param rootView the given root view
     * @return the builder itself
     */
    fun withRootView(rootView: View): PicPickerBuilder {
        mPicker.rootView = rootView
        return this
    }

    /**
     * Sets the requested size for the bigger side of the picture, 0 if no compression is required.
     *
     * @param pictureSize the requested size (0 if no compression is required)
     */
    fun withPictureSize(pictureSize: Int): PicPickerBuilder {
        mPicker.pictureSize = pictureSize
        return this
    }

    /**
     * Sets the request code used when invoking
     * [Activity.startActivityForResult] or
     * [android.support.v4.app.Fragment.startActivityForResult]
     *
     * @param requestCode the given request code
     * @return the builder itself
     */
    fun withRequestCode(requestCode: Int): PicPickerBuilder {
        mPicker.requestCode = requestCode
        return this
    }

    /**
     * Adds the gallery apps to the options to be shown
     * @return the builder itself
     */
    fun showGallery(): PicPickerBuilder {
        mPicker.showGallery = true
        return this
    }

    /**
     * Sets the [PicPickerImpl.fileName]
     *
     * @param fileName the file name
     * @return the builder itself
     */
    fun withFileName(fileName: String): PicPickerBuilder {
        mPicker.fileName = fileName
        return this
    }

    /**
     * Sets a list of modes on how the resulting picture can be returned, possible values are
     * [PicPicker.BITMAP]
     * [PicPicker.BYTES]
     * [PicPicker.FILE]
     *
     * @param modes the chosen modes
     * @return the builder itself
     */
    fun withModes(vararg modes: Int): PicPickerBuilder {
        mPicker.modes = modes
        return this
    }

    /**
     * Sets how the resulting image should be scaled to the requested size, possible values are:
     * [PicPicker.KEEP_RATIO]
     * [PicPicker.CROP]
     * [PicPicker.SCALE_XY]
     *
     * @param scaleType
     * @return the builder itself
     */
    fun withScaleType(@PicPicker.ScaleType scaleType: Int): PicPickerBuilder {
        mPicker.scaleType = scaleType
        return this
    }

    /**
     * @return the composed [PicPicker]
     */
    fun build(): PicPicker {
        return mPicker
    }
}