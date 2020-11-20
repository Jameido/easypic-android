package dev.jameido.easypic

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

/**
 * @author Jameido
 * @since 3
 *
 * Task that takes the output file uri and processes the image as wanted:
 * - scale to size
 * - fix rotation
 * - return as bitmap, file or bite array
 */
class ProcessResultTask(context: Context, private val picPickerImpl: PicPickerImpl) : AsyncTask<Uri, Void, PickerResult>() {

    private var exception: Exception? = null
    private var contextRef: WeakReference<Context> = WeakReference(context.applicationContext)

    /**
     * Process the image
     */
    override fun doInBackground(vararg params: Uri?): PickerResult {
        val pickerResult = PickerResult()
        if (params.isNotEmpty() && params[0] != null) {
            try {
                resultBitmap(params[0]!!)?.let { bitmap ->
                    picPickerImpl.modes.forEach { mode: Int ->
                        when (mode) {
                            PicPicker.BITMAP -> pickerResult.bitmap = bitmap
                            PicPicker.BYTES -> pickerResult.bytes = resultBytes(bitmap)
                            PicPicker.FILE -> pickerResult.file = resultFile(bitmap)
                        }
                    }
                }

            } catch (ex: Exception) {
                Log.e("ProcessResultTask", "Error while porcessing the image result: ", ex)
                exception = ex
            }
        }
        return pickerResult
    }

    /**
     * If [exception] has been initialized in [doInBackground]
     * failure callback is invoked, otherwise success one is invoked
     * @param result result of the image processing
     */
    override fun onPostExecute(result: PickerResult) {
        if (exception != null) {
            picPickerImpl.onPickFailure.invoke(exception!!)
            picPickerImpl.onPickResultListener.onPicPickFailure(exception!!)
        } else {
            picPickerImpl.onPickSuccess.invoke(result)
            picPickerImpl.onPickResultListener.onPicPickSuccess(result)
        }
    }

    /**
     * Creates a bitmap with the desired modifications from the given file uri
     *
     * @param source uri of the image file
     * @return the processed image
     * @throws IOException if an error happens in the process
     */
    @Throws(IOException::class)
    private fun resultBitmap(source: Uri): Bitmap? {
        val context = contextRef.get()
        context?.let {
            val imageProcessor = ImageProcessor()
            return when (picPickerImpl.scaleType) {
                PicPicker.KEEP_RATIO -> imageProcessor.decodeAndResizeImageUri(it, source, picPickerImpl.pictureSize)
                PicPicker.CROP -> imageProcessor.decodeAndCropImageUri(it, source, picPickerImpl.pictureSize)
                PicPicker.SCALE_XY -> imageProcessor.decodeAndScaleXYImageUri(context, source, picPickerImpl.pictureSize)
                else -> imageProcessor.decodeAndResizeImageUri(it, source, picPickerImpl.pictureSize)
            }
        }
        throw IOException("Context is null, cannot decode bitmap from taken image")
    }

    /**
     * Takes the given bitmap and stores it in a file with the name specified in
     * [PicPickerImpl.fileName]
     * @param bitmap the one processed by [resultBitmap]
     * @return the created/overridden file
     * @throws IOException if an error happens in the process
     */
    @Throws(IOException::class)
    private fun resultFile(bitmap: Bitmap): File {
        val context = contextRef.get()
        context?.let {
            var outStream: FileOutputStream? = null
            val vDestFile = FileUtils.createPictureFile(it, picPickerImpl.fileName)
            try {
                outStream = FileOutputStream(vDestFile, false)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream)
            } finally {
                outStream?.let { outStream.close() }
            }
            return vDestFile
        }
        throw IOException("Context is null, cannot decode bitmap from taken image")
    }

    /**
     * Takes the given bitmap and converts it in a array of bytes
     * @param bitmap the one processed by [resultBitmap]
     * @return the array of bytes
     */
    private fun resultBytes(bitmap: Bitmap): ByteArray? {
        val outStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream)
        return outStream.toByteArray()
    }
}