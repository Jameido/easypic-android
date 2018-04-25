package pro.eluzivespikes.easypic

import android.graphics.Bitmap
import java.io.File

/**
 * Created by Luca Rossi on 25/04/2018.
 */
/**
 * The result of the pic picker process
 */
class PickerResult {

    var file: File? = null
    var bitmap: Bitmap? = null
    var bytes: ByteArray? = null
}