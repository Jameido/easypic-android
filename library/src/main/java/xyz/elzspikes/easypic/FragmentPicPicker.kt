package xyz.elzspikes.easypic

import android.app.Activity
import android.content.Intent
import androidx.fragment.app.Fragment

/**
 * @author Jameido
 * @since 3
 *
 * Extends [PicPickerImpl] using a [Fragment]
 */
class FragmentPicPicker(private var mFragment: Fragment?) : PicPickerImpl() {

    override val activity: Activity?
        get() = mFragment?.activity

    /**
     * Request the necessary permissions to the user via [Activity.requestPermissions]
     * @param permissions list of permissions to ask
     * @param requestCode code used to request the permissions
     */
    override fun requestPermissions(permissions: Array<String>, requestCode: Int) {
        mFragment!!.requestPermissions(permissions, requestCode)
    }

    /**
     * Show the application selector to the user
     * @param intent the selector intent
     * @param requestCode code used to invoke the activity result
     */
    override fun startIntentChooser(intent: Intent, requestCode: Int) {
        mFragment!!.startActivityForResult(intent, requestCode)
    }

    /**
     * Removes the activity reference when it gets destroyed
     * Must be called in [Fragment.onDestroy] to avoid memory leaks.
     */
    override fun onDestroy() {
        super.onDestroy()
        mFragment = null
    }
}