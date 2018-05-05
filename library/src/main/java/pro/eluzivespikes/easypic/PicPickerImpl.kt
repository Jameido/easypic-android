package pro.eluzivespikes.easypic

import android.annotation.TargetApi
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.support.v4.content.FileProvider
import android.util.Log
import android.view.View
import android.widget.TextView
import java.io.IOException

/**
 * Created by Luca Rossi on 19/03/2018.
 */
abstract class PicPickerImpl : PicPicker {

    val TAG = "PicPicker"

    private val PERMISSION_CAMERA_STORAGE = 345
    private val DEFAULT_FILE_NAME = "easy_pic_picture"
    private val REQUEST_RESULT_CAMERA_GALLERY_DEFAULT = 300
    private val DEFAULT_PICTURE_SIZE = 0

    override var onPickSuccess: (result: PickerResult) -> Unit = { _ -> }
    override var onPickFailure: (exception: Exception) -> Unit = { _ -> }

    /**
     * The request code used when invoking
     * {@link Activity#startActivityForResult(Intent, int)} or
     * {@link android.support.v4.app.Fragment#startActivityForResult(Intent, int)}.
     */
    internal var requestCode = REQUEST_RESULT_CAMERA_GALLERY_DEFAULT

    /**
     * The request code used when asking camera and storage permissions
     */
    internal var permissionCode = PERMISSION_CAMERA_STORAGE

    /**
     * The name of the resulting photo file
     * If it's empty the default one will be used instead
     * Also removes the possible jpg extension.
     *
     * TODO: improve this function to accept also PNG
     */
    internal var fileName = DEFAULT_FILE_NAME
        set(value) {
            field = if (value.isBlank()) {
                DEFAULT_FILE_NAME
            } else {
                value.replace(".jpg", "")
            }
        }

    /**
     * Indicates if the gallery applications should be shown in the
     * selector
     */
    internal var showGallery = false


    /**
     * The requested size for the bigger side of the picture, 0 if no compression is required.
     */
    internal var pictureSize = DEFAULT_PICTURE_SIZE

    /**
     *  List of modes on how the resulting picture can be returned, possible values are:
     * {@link PicPicker#BITMAP}
     * {@link PicPicker#BYTES}
     * {@link PicPicker#FILE}
     */
    internal var modes = intArrayOf(PicPicker.BITMAP)

    /**
     * How the resulting image should be scaled to {@link #pictureSize}, possible values are:
     * {@link ScaleType#KEEP_RATIO}
     * {@link ScaleType#CROP}
     * {@link ScaleType#SCALE_XY}
     */
    @PicPicker.ScaleType
    internal var scaleType = PicPicker.KEEP_RATIO

    /**
     * Sets the root view used to display the snackbar when rationale permissions have to
     * be asked to the user.
     */
    internal var rootView: View? = null

    internal var outputFileUri: Uri? = null

    internal abstract val activity: Activity?

    private var tempFileName = "temp_$fileName"

    //TODO: replace async tasks with corutines
    private var processResultTask: ProcessResultTask? = null

    /**
     * Automatically the value is taken from the activity package name, otherwise it can be overridden
     */
    private var authority: String = ""
        get() {
            return if (field.isEmpty() && activity != null) {
                activity!!.application.packageName + ".provider"
            } else {
                field
            }
        }

    /**
     * Returns the uri of the file where the picture is stored.
     *
     * @throws IOException thrown if an error happens while creating the temporary file where store
     *                     the picture
     */
    @Throws(IOException::class)
    private fun initOutputFileUri() {
        activity?.let {
            val photoFile = FileUtils.createPictureTempFile(activity, tempFileName)
            if (photoFile != null) {
                outputFileUri = FileProvider.getUriForFile(
                        it,
                        authority,
                        photoFile)
            }
        }
    }

    /**
     * Removes the activity reference when it gets destroyed
     * Must be called in {@link Activity#onDestroy()} to avoid memory leaks.
     */
    override fun onDestroy() {
        processResultTask?.cancel(true)
    }

    /**
     * Called from {@link Activity#onRequestPermissionsResult(int, String[], int[])}
     * and if the request code matches with {@link #mPermissionCode} checks if the permissions have
     * been given and calls the appropriate {@link PicPickerImpl} method.
     *
     * @param requestCode  the permissions request code
     * @param permissions  the permissions asked
     * @param grantResults the grant permissions results
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode != permissionCode) {
            return
        }

        val deniedPermissions = emptyArray<String>()
        for (index in permissions.indices) {
            if (grantResults[index] != PackageManager.PERMISSION_GRANTED) {
                deniedPermissions.plus(permissions[index])
            }
        }

        if (deniedPermissions.isEmpty()) {
            startIntentChooser()
        }
    }

    /**
     * Called from {@link Activity#onActivityResult(int, int, Intent)} and if the request code
     * matches with {@link #mRequestCode} gets the image, compresses it if needed and copies it
     * in the internal storage.
     *
     * @param requestCode the result request code
     * @param resultCode  the result result code
     * @param data        the data from the result
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == this.requestCode) {
            if (resultCode == Activity.RESULT_OK) {
                var isCamera = true
                if (data?.data != null) {
                    val action = data.action
                    isCamera = MediaStore.ACTION_IMAGE_CAPTURE == action
                }
                val uriFileSrc = if (isCamera) outputFileUri else data?.data
                activity?.let {
                    processResultTask = ProcessResultTask(it, this)
                    processResultTask?.execute(uriFileSrc)
                }
            } else {
                FileUtils.deleteFileFromUri(activity, outputFileUri)
            }
        }
    }

    override fun openPicker() {

        val missingPermissions = CameraUtils.getMissingPermissions(activity)

        if (missingPermissions.missingPermissions.isEmpty()) {
            startIntentChooser()
        } else {
            requestPermissions(missingPermissions)
        }
    }

    internal abstract fun requestPermissions(permissions: Array<String>, requestCode: Int)

    internal abstract fun showSelector(intent: Intent, requestCode: Int)


    /**
     * Shows the application chooser to the user
     */
    private fun startIntentChooser() {
        try {
            initOutputFileUri()
            showSelector(CameraUtils.getIntentChooser(activity, outputFileUri, showGallery), requestCode)
        } catch (ex: IOException) {
            Log.e(TAG, "startIntentChooser: ", ex)
            onPickFailure.invoke(ex)
        }

    }

    /**
     * Asks the user the missing permissions, with the standard system alert if they haven't been
     * already negated once otherwise are asked in a "rationale" way (custom alert or snackbar).
     *
     * @param missingPermissions list of missing permissions with the message for the rationale
     * request
     */
    @TargetApi(Build.VERSION_CODES.M)
    private fun requestPermissions(missingPermissions: CameraUtils.MissingPermissions) {
        if (missingPermissions.isRationale) {
            requestPermissionsRationale(missingPermissions.missingPermissions.toTypedArray())
        } else {
            requestPermissions(missingPermissions.missingPermissions.toTypedArray(), permissionCode)
        }
    }

    /**
     * Asks the user to give permissions showing a [Snackbar] if possible or an
     * [AlertDialog] as fallback
     *
     * @param missingPermissions list of missing permissions with the message to show
     */
    private fun requestPermissionsRationale(missingPermissions: Array<String>) {
        activity?.let {
            if (rootView != null) {
                val snackBar = Snackbar.make(rootView!!, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                val tv = snackBar.view.findViewById<TextView>(R.id.snackbar_text)
                tv.setTextColor(Color.WHITE)
                snackBar.setAction(android.R.string.ok) { requestPermissions(missingPermissions, permissionCode) }
                snackBar.setActionTextColor(ContextCompat.getColor(it, android.R.color.white))
                snackBar.show()
            } else {
                AlertDialog.Builder(activity)
                        .setMessage(R.string.permission_rationale)
                        .setPositiveButton(android.R.string.ok) { _, _ -> requestPermissions(missingPermissions, permissionCode) }
                        .setNegativeButton(android.R.string.cancel) { _, _ -> }
                        .show()
            }
        }
    }
}