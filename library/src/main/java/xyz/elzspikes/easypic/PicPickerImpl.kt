package xyz.elzspikes.easypic

import android.annotation.TargetApi
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.StrictMode
import android.provider.MediaStore
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import android.util.Log
import android.view.View
import android.widget.TextView
import com.google.android.material.snackbar.Snackbar
import java.io.File
import java.io.IOException

/**
 * @author Jameido
 * @since 3
 *
 * Abstract implementation of [PicPicker]
 */
abstract class PicPickerImpl : PicPicker {

    val TAG = "PicPicker"

    private val PERMISSION_CAMERA_STORAGE = 345
    private val DEFAULT_FILE_NAME = "easy_pic_picture"
    private val REQUEST_RESULT_CAMERA_GALLERY_DEFAULT = 300
    private val DEFAULT_PICTURE_SIZE = 0

    override var onPickSuccess: (result: PickerResult) -> Unit = { _ -> }
    override var onPickFailure: (exception: Exception) -> Unit = { _ -> }
    override var onPickResultListener = object : OnPickResultListener {
        override fun onPicPickSuccess(result: PickerResult) {
        }

        override fun onPicPickFailure(exception: Exception) {
        }
    }
    /**
     * The request code used when invoking
     * [Activity.startActivityForResult] or
     * [android.support.v4.app.Fragment.startActivityForResult]
     */
    internal var requestCode = REQUEST_RESULT_CAMERA_GALLERY_DEFAULT

    /**
     * The request code used when asking camera and storage permissions
     */
    private var permissionCode = PERMISSION_CAMERA_STORAGE

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
     * [PicPicker.BITMAP]
     * [PicPicker.BYTES]
     * [PicPicker.FILE]
     */
    internal var modes = intArrayOf(PicPicker.BITMAP)

    /**
     * How the resulting image should be scaled to [pictureSize], possible values are:
     * [PicPicker.KEEP_RATIO]
     * [PicPicker.CROP]
     * [PicPicker.SCALE_XY]
     */
    @PicPicker.ScaleType
    internal var scaleType = PicPicker.KEEP_RATIO

    /**
     * Root [View] used to display the [Snackbar] when rationale permissions have to
     * be asked to the user.
     */
    internal var rootView: View? = null

    /**
     * [Uri] with the location of the file where the picture is stored.
     */
    private var outputFileUri: Uri? = null

    /**
     * [Activity] used to show selector and as [android.content.Context]
     */
    internal abstract val activity: Activity?

    /**
     * Name of the temporary file used to store the picture
     */
    private var tempFileName = "temp_$fileName"

    /**
     * [android.os.AsyncTask] that elaborates the taken image with the chosen parameters
     * TODO: replace async tasks with corutines
     */
    private var processResultTask: ProcessResultTask? = null

    /**
     * Automatically the value is taken from the activity package name,
     * otherwise it can be overridden
     */
    private var authority: String = ""
        get() {
            return if (field.isEmpty() && activity != null) {
                activity!!.application.packageName + ".easypicprovider"
            } else {
                field
            }
        }

    /**
     * Initializes [outputFileUri] with the location of the file where the picture is stored.
     *
     * @throws IOException thrown if an error happens while creating the temporary file where store
     *                     the picture
     */
    @Throws(IOException::class)
    private fun initOutputFileUri() {
        activity?.let {
            val photoFile = FileUtils.createPictureTempFile(it, tempFileName)
            outputFileUri = FileProvider.getUriForFile(
                    it,
                    authority,
                    photoFile)
        }
    }


    /**
     * WORKAROUND for this crash:
     * https://console.firebase.google.com/u/0/project/pay-x-1ad8c/crashlytics/app/android:it.nexi.yap/issues/5c1a3492f8b88c2963691545
     *
     * Found here:
     * https://stackoverflow.com/questions/46673683/android-fileprovider-failed-to-find-configured-root-that-contains
     * @param context
     * @param file
     * @return
     */
    private fun getFileUri(context: Context, file: File): Uri {
        return try {
            FileProvider.getUriForFile(context, authority, file)
        } catch (e: IllegalArgumentException) {
            val builder = StrictMode.VmPolicy.Builder()
            StrictMode.setVmPolicy(builder.build())
            Uri.fromFile(file)
        }
    }

    /**
     * Removes the activity reference when it gets destroyed
     * Must be called in [Activity.onDestroy] to avoid memory leaks.
     */
    override fun onDestroy() {
        processResultTask?.cancel(true)
    }

    /**
     * Called from [Activity.onRequestPermissionsResult]
     * and if the request code matches with [permissionCode] checks if the permissions have
     * been given and calls the appropriate [PicPickerImpl] method.
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
            showSelector()
        }
    }

    /**
     * Called from [Activity.onActivityResult] and if the request code
     * matches with [requestCode] gets the image, compresses it if needed and copies it
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
            } else if (activity != null && outputFileUri != null) {
                FileUtils.deleteFileFromUri(activity!!, outputFileUri!!)
            }
        }
    }

    /**
     * If the user has given all the necessary permissions shows the app selector
     * with [startIntentChooser]
     * If not asks the permissions via [requestPermissions]
     */
    override fun showSelector() {
        activity?.let {
            val missingPermissions = CameraUtils.getMissingPermissions(it)

            if (missingPermissions.missingPermissions.isEmpty()) {
                startIntentChooser()
            } else {
                requestPermissions(missingPermissions)
            }
        }
    }

    /**
     * Request the necessary permissions to the user
     * @param permissions list of permissions to ask
     * @param requestCode code used to request the permissions
     */
    internal abstract fun requestPermissions(permissions: Array<String>, requestCode: Int)

    /**
     * Show the application selector to the user
     * @param intent the selector intent
     * @param requestCode code used to invoke the activity result
     */
    internal abstract fun startIntentChooser(intent: Intent, requestCode: Int)

    /**
     * Init the output file and show the application selector to the user
     */
    private fun startIntentChooser() {
        activity?.let {
            try {
                initOutputFileUri()
                startIntentChooser(CameraUtils.getIntentChooser(it, outputFileUri!!, showGallery), requestCode)
            } catch (ex: IOException) {
                Log.e(TAG, "startIntentChooser: ", ex)
                onPickFailure.invoke(ex)
            }
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
    private fun requestPermissions(missingPermissions: MissingPermissions) {
        if (missingPermissions.askRationale) {
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
                val snackBar = Snackbar.make(rootView!!, R.string.easypic_permission_rationale, Snackbar.LENGTH_INDEFINITE)
                val tv = snackBar.view.findViewById<TextView>(R.id.snackbar_text)
                tv.setTextColor(Color.WHITE)
                snackBar.setAction(android.R.string.ok) { requestPermissions(missingPermissions, permissionCode) }
                snackBar.setActionTextColor(ContextCompat.getColor(it, android.R.color.white))
                snackBar.show()
            } else {
                AlertDialog.Builder(activity)
                        .setMessage(R.string.easypic_permission_rationale)
                        .setPositiveButton(android.R.string.ok) { _, _ -> requestPermissions(missingPermissions, permissionCode) }
                        .setNegativeButton(android.R.string.cancel) { _, _ -> }
                        .show()
            }
        }
    }
}