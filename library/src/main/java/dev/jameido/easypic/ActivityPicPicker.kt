package dev.jameido.easypic

import android.annotation.TargetApi
import android.app.Activity
import android.content.Intent
import android.os.Build

/**
 * @author Jameido
 * @since 3
 *
 * Extends [PicPickerImpl] using an [Activity]
 */
class ActivityPicPicker(private var mActivity: Activity?) : PicPickerImpl() {

    override val activity: Activity?
        get() = mActivity

    /**
     * Request the necessary permissions to the user via [Activity.requestPermissions]
     * @param permissions list of permissions to ask
     * @param requestCode code used to request the permissions
     */
    @TargetApi(Build.VERSION_CODES.M)
    override fun requestPermissions(permissions: Array<String>, requestCode: Int) {
        activity!!.requestPermissions(permissions, requestCode)
    }

    /**
     * Show the application selector to the user
     * @param intent the selector intent
     * @param requestCode code used to invoke the activity result
     */
    override fun startIntentChooser(intent: Intent, requestCode: Int) {
        activity!!.startActivityForResult(intent, requestCode)
    }

    /**
     * Removes the activity reference when it gets destroyed
     * Must be called in [Activity.onDestroy] to avoid memory leaks.
     */
    override fun onDestroy() {
        super.onDestroy()
        mActivity = null
    }
}