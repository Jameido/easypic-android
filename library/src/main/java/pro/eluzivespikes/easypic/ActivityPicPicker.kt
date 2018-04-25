package pro.eluzivespikes.easypic

import android.annotation.TargetApi
import android.app.Activity
import android.content.Intent
import android.os.Build

/**
 * Created by Luca Rossi on 25/04/2018.
 */
class ActivityPicPicker(private var mActivity : Activity?) : PicPickerImpl() {
    override val activity: Activity?
        get() = mActivity


    @TargetApi(Build.VERSION_CODES.M)
    override fun requestPermissions(permissions: Array<String>, requestCode: Int) {
        activity!!.requestPermissions(permissions, requestCode)
    }

    override fun showSelector(intent: Intent, requestCode: Int) {
        activity!!.startActivityForResult(intent, requestCode)
    }

    override fun onDestroy() {
        super.onDestroy()
        mActivity = null
    }
}