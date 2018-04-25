package pro.eluzivespikes.easypic

import android.app.Activity
import android.support.v4.app.Fragment
import android.view.View

/**
 * Created by Luca Rossi on 25/04/2018.
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
     */
    fun withSuccessListener(onPickSuccess: (result: PickerResult) -> Unit = { _ -> }): PicPickerBuilder {
        mPicker.onPickSuccess = onPickSuccess
        return this
    }

    /**
     * Set the on failure method to be invoked when the pick process fails.
     *
     * @param onPickFailure the failure method
     */
    fun withFailureListener(onPickFailure: (exception: Exception) -> Unit = { _ -> }): PicPickerBuilder {
        mPicker.onPickFailure = onPickFailure
        return this
    }

    /**
     * Sets the root view used to display the snackbar when rationale permissions have to
     * be asked to the user.
     *
     * @param rootView the given root view
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
     */
    fun withRequestCode(requestCode: Int): PicPickerBuilder {
        mPicker.requestCode = requestCode
        return this
    }

    /**
     * Adds the gallery apps to the options to be shown
     */
    fun showGallery(): PicPickerBuilder {
        mPicker.showGallery = true
        return this
    }

    fun withFileName(fileName: String): PicPickerBuilder {
        mPicker.fileName = fileName
        return this
    }

    /**
     * Sets a list of modes on how the resulting picture can be returned, possible values are
     * [PicPicker.PickerMode.BITMAP]
     * [PicPicker.PickerMode.BYTES]
     * [PicPicker.PickerMode.FILE]
     *
     * @param modes the chosen modes
     */
    fun withModes(vararg modes: Int): PicPickerBuilder {
        mPicker.modes = modes
        return this
    }

    /**
     * Sets how the resulting image should be scaled to the requested size, possible values are:
     * [PicPicker.ScaleType.KEEP_RATIO]
     * [PicPicker.ScaleType.CROP]
     * [PicPicker.ScaleType.SCALE_XY]
     *
     * @param scaleType
     */
    fun withScaleType(@PicPicker.ScaleType scaleType: Int): PicPickerBuilder {
        mPicker.scaleType = scaleType
        return this
    }

    fun build(): PicPicker {
        return mPicker
    }
}