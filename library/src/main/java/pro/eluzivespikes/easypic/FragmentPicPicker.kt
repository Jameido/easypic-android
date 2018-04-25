package pro.eluzivespikes.easypic

import android.app.Activity
import android.content.Intent
import android.support.v4.app.Fragment

/**
 * Created by Luca Rossi on 25/04/2018.
 */
/**
 *
 * Extends {@link PicPicker} and uses a fragment instead of the activity
 */
class FragmentPicPicker(private var mFragment: Fragment?) : PicPickerImpl() {

    override val activity: Activity?
        get() = mFragment?.activity

    override fun requestPermissions(permissions: Array<String>, requestCode: Int) {
        mFragment!!.requestPermissions(permissions, requestCode)
    }

    override fun showSelector(intent: Intent, requestCode: Int) {
        mFragment!!.startActivityForResult(intent, requestCode)
    }

    override fun onDestroy() {
        super.onDestroy()
        mFragment = null
    }
}