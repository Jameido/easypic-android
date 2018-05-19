package pro.eluzivespikes.easypic

import android.Manifest
import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Parcelable
import android.provider.MediaStore
import java.io.IOException
import java.util.ArrayList

/**
 * @author Jameido
 * @since 3
 *
 * Collection of utils functions for the camera
 */
object CameraUtils {

    /**
     * Puts together the results of [getGalleryIntents] and
     * [getCameraIntents] to form and intent used to show the
     * chooser to the user
     *
     * @param context       the given context
     * @param outputFileUri the uri where store the image
     * @param showGallery   if gallery apps should be included
     * @return intent to display the chooser
     * @throws IOException thrown if an error happens while creating the temp file
     */
    @Throws(IOException::class)
    fun getIntentChooser(context: Context, outputFileUri: Uri, showGallery: Boolean): Intent {

        val selectorIntents = ArrayList<Intent>()
        selectorIntents.addAll(getCameraIntents(context, outputFileUri))
        if (showGallery) {
            selectorIntents.addAll(getGalleryIntents(context))
        }

        val targetIntent: Intent
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            targetIntent = Intent()
        else {
            targetIntent = selectorIntents[selectorIntents.size - 1]
            selectorIntents.removeAt(selectorIntents.size - 1)
        }

        val chooserIntent = Intent.createChooser(targetIntent, context.getString(R.string.select_picture_source))
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, selectorIntents.toTypedArray<Parcelable>())

        return chooserIntent
    }

    /**
     * Builds a list of intents for all the camera apps installed on the device
     *
     * @param context       the given context
     * @param outputFileUri the uri where the file will be saved in
     * @return the intents used to launch a camera app
     */
    private fun getCameraIntents(context: Context, outputFileUri: Uri): List<Intent> {
        val cameraIntents = ArrayList<Intent>()
        val captureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val listCam = context.packageManager.queryIntentActivities(captureIntent, 0)

        for (res in listCam) {
            val intent = Intent(captureIntent)
            intent.component = ComponentName(res.activityInfo.packageName, res.activityInfo.name)
            context.grantUriPermission(res.activityInfo.packageName, outputFileUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
            intent.`package` = res.activityInfo.packageName
            intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri)
            cameraIntents.add(intent)
        }

        return cameraIntents
    }

    /**
     * Builds a list of intents for all the gallery apps installed on the device
     * Documents app are removed from the list
     *
     * @param context the given context
     * @return the intents used to launch a gallery app
     */
    private fun getGalleryIntents(context: Context): List<Intent> {
        // Add all gallery apps as options
        val intents = ArrayList<Intent>()
        val galleryIntent = Intent(Intent.ACTION_GET_CONTENT)
        galleryIntent.type = "image/*"
        val listGallery = context.packageManager.queryIntentActivities(galleryIntent, 0)

        for (res in listGallery) {
            if ("com.android.documentsui.DocumentsActivity" == res.activityInfo.name) {
                continue
            }

            val intent = Intent(galleryIntent)
            intent.component = ComponentName(res.activityInfo.packageName, res.activityInfo.name)
            intent.`package` = res.activityInfo.packageName
            intents.add(intent)
        }

        return intents
    }


    /**
     * Checks if all the necessary permissions have been given and returns the ones that
     * still need to be granted by the user
     *
     * @param activity the activity from which the picker is called
     * @return the list of missing permissions
     */
    fun getMissingPermissions(activity: Activity): MissingPermissions {
        val vMissingPermissions = MissingPermissions(ArrayList(), false)
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
            if (activity.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                vMissingPermissions.missingPermissions.add(Manifest.permission.CAMERA)

                vMissingPermissions.askRationale = activity.shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)
            }

            if (activity.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                vMissingPermissions.missingPermissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
                vMissingPermissions.askRationale = vMissingPermissions.askRationale || activity.shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }

            if (activity.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                vMissingPermissions.missingPermissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                vMissingPermissions.askRationale = vMissingPermissions.askRationale || activity.shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }
        return vMissingPermissions
    }
}