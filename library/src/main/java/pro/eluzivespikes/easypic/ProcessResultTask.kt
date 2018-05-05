package pro.eluzivespikes.easypic

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.AsyncTask
import android.util.Log
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.lang.ref.WeakReference

class ProcessResultTask(context: Context, private val picPickerImpl: PicPickerImpl) : AsyncTask<Uri, Void, PickerResult>() {

    private var exception: Exception? = null
    private var contextRef: WeakReference<Context> = WeakReference(context.applicationContext)

    override fun doInBackground(vararg params: Uri?): PickerResult {
        val pickerResult = PickerResult()
        if (params.isNotEmpty() && params[0] != null) {
            try {
                val bitmap = resultBitmap(params[0]!!)
                picPickerImpl.modes.forEach { mode: Int ->
                    when (mode) {
                        PicPicker.BITMAP -> pickerResult.bitmap = bitmap
                        PicPicker.BYTES -> pickerResult.bytes = resultBytes(bitmap)
                        PicPicker.FILE -> pickerResult.file = resultFile(bitmap)
                    }
                }

            } catch (ex: Exception) {
                Log.e("ProcessResultTask", "Error while porcessing the image result: ", ex)
                exception = ex
            }
        }
        return pickerResult
    }

    override fun onPostExecute(result: PickerResult) {
        if (exception != null) {
            picPickerImpl.onPickFailure.invoke(exception!!)
        } else {
            picPickerImpl.onPickSuccess.invoke(result)
        }
    }

    @Throws(IOException::class)
    private fun resultBitmap(source: Uri): Bitmap {
        val context = contextRef.get()
        context?.let {
            return when (picPickerImpl.scaleType) {
                PicPicker.KEEP_RATIO -> ImageUtils.decodeAndResizeImageUri(it, source, picPickerImpl.pictureSize)
                PicPicker.CROP -> ImageUtils.decodeAndCropImageUri(it, source, picPickerImpl.pictureSize)
            //TODO: complete implementation
                PicPicker.SCALE_XY -> ImageUtils.decodeAndResizeImageUri(context, source, picPickerImpl.pictureSize)
                else -> ImageUtils.decodeAndResizeImageUri(it, source, picPickerImpl.pictureSize)
            }
        }
        throw IOException("Context is null, cannot decode bitmap from taken image")
    }

    @Throws(IOException::class)
    private fun resultFile(bitmap: Bitmap): File {
        val context = contextRef.get()
        context?.let {
            var outStream: FileOutputStream? = null
            val vDestFile = FileUtils.createPictureFile(it, picPickerImpl.fileName)
            try {
                outStream = FileOutputStream(vDestFile)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream)
            } finally {
                outStream?.let { outStream.close() }
            }
            return vDestFile
        }
        throw IOException("Context is null, cannot decode bitmap from taken image")
    }

    private fun resultBytes(bitmap: Bitmap): ByteArray? {
        val outStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream)
        return outStream.toByteArray()
    }
}