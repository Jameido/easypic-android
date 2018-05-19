package pro.eluzivespikes.easypic

import android.content.Intent
import android.support.annotation.IntDef

/**
 * @author Jameido
 * @since 3
 *
 * Interface exposing methods to use PicPicker
 */
interface PicPicker {

    /**
     * Invoked once the image has been taken and processed successfully
     */
    var onPickSuccess: (result: PickerResult) -> Unit

    /**
     * Invoked when an exception is thrown while taking or processing
     * the image
     */
    var onPickFailure: (exception: Exception) -> Unit

    /**
     * Shows the app selector to the user
     */
    fun showSelector()

    /**
     * Must be called in [android.app.Activity.onDestroy] to avoid memory leaks.
     * TODO: switch to Lifecycle observer from Arch Components
     */
    fun onDestroy()

    /**
     * Called from [android.app.Activity.onRequestPermissionsResult]
     * and if the necessary permissions have been given opens the picker.
     *
     * @param requestCode  the permissions request code
     * @param permissions  the permissions asked
     * @param grantResults the grant permissions results
     */
    fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray)

    /**
     * Called from [android.app.Activity.onActivityResult]
     * and if the request code matches with the one used processes the result.
     *
     * @param requestCode the result request code
     * @param resultCode  the result result code
     * @param data        the data from the result
     */
    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)

    /**
     * Used to indicate how the image will be returned
     */
    @Target(
            AnnotationTarget.FIELD,
            AnnotationTarget.LOCAL_VARIABLE,
            AnnotationTarget.TYPE_PARAMETER,
            AnnotationTarget.VALUE_PARAMETER
    )
    @IntDef(BITMAP, BYTES, FILE)
    @Retention(AnnotationRetention.SOURCE)
    annotation class PickerMode

    /**
     * Used to indicate throw the resulting image will be scaled down to the
     * desired size
     * TODO: fix implementation of SCALE_XY
     */
    @Target(
            AnnotationTarget.FIELD,
            AnnotationTarget.LOCAL_VARIABLE,
            AnnotationTarget.TYPE_PARAMETER,
            AnnotationTarget.VALUE_PARAMETER
    )
    @IntDef(KEEP_RATIO, CROP)
    @Retention(AnnotationRetention.SOURCE)
    annotation class ScaleType

    companion object {
        /**
         * A bitmap representing the image will be returned
         */
        const val BITMAP = 0
        /**
         * A byte array representing the image will be returned
         */
        const val BYTES = 1
        /**
         * A file containing the image will be returned
         */
        const val FILE = 2

        /**
         * Bigger size will be scaled down to required size to keep aspect ratio
         */
        const val KEEP_RATIO = 0
        /**
         * Smaller size will be scaled down to required size then cropped
         */
        const val CROP = 1
        /**
         * Both sizes will be scaled down to required size
         */
        const val SCALE_XY = 2
    }
}