package xyz.elzspikes.easypic

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.support.media.ExifInterface
import android.util.Log
import java.io.FileNotFoundException
import java.io.IOException

/**
 * @author Jameido
 * @since 3
 *
 * Class that contains all the functions needed to process the image
 */
class ImageProcessor {

    private val TAG = "ImageProcessor"

    /**
     * Decodes the file corresponding to the uri into a bitmap:
     * - if a required size is specified (> 0) it gets scaled to it
     * - if necessary is rotated
     *
     * @param context      the given context
     * @param uri          the uri of the image
     * @param requiredSize the scale size (0 if to keep original)
     * @return the resulting bitmap
     * @throws IOException thrown if an error happens in the process
     */
    @Throws(IOException::class)
    fun decodeAndResizeImageUri(context: Context, uri: Uri, requiredSize: Int): Bitmap {
        val o = BitmapFactory.Options()
        o.inJustDecodeBounds = true
        BitmapFactory.decodeStream(context.contentResolver.openInputStream(uri), null, o)

        var scale = 1

        if (requiredSize > 0) {
            val width = o.outWidth
            val height = o.outHeight
            val ratioBitmap = width.toFloat() / height.toFloat()
            val biggerSize = if (ratioBitmap < 1) height else width
            scale = biggerSize / requiredSize
        }

        val o2 = BitmapFactory.Options()
        o2.inSampleSize = scale
        o2.inJustDecodeBounds = false

        return rotateImageIfRequired(context, BitmapFactory.decodeStream(context.contentResolver.openInputStream(uri), null, o2), uri)
    }

    /**
     * Decodes the file corresponding to the uri into a bitmap:
     * - if a required size is specified (> 0) it gets scaled to it
     * - if necessary is rotated
     *
     * @param context      the given context
     * @param uri          the uri of the image
     * @param requiredSize the scale size (0 if to keep original)
     * @return the resulting bitmap
     * @throws IOException thrown if an error happens in the process
     */
    @Throws(IOException::class)
    fun decodeAndCropImageUri(context: Context, uri: Uri, requiredSize: Int): Bitmap {
        val o = BitmapFactory.Options()
        o.inJustDecodeBounds = true
        BitmapFactory.decodeStream(context.contentResolver.openInputStream(uri), null, o)

        var scale = 1

        if (requiredSize > 0) {
            val width = o.outWidth
            val height = o.outHeight
            val ratioBitmap = width.toFloat() / height.toFloat()
            val smallerSize = if (ratioBitmap >= 1) height else width
            scale = smallerSize / requiredSize
        }

        val o2 = BitmapFactory.Options()
        o2.inSampleSize = Math.ceil(scale.toDouble()).toInt()
        o2.inJustDecodeBounds = false

        val rotatedBitmap = rotateImageIfRequired(context, BitmapFactory.decodeStream(context.contentResolver.openInputStream(uri), null, o2), uri)

        val cropSize: Int
        var cropX = 0
        var cropY = 0

        if (rotatedBitmap.height > rotatedBitmap.width) {
            cropSize = rotatedBitmap.width
            cropY = (rotatedBitmap.height - rotatedBitmap.width) / 2
        } else {
            cropSize = rotatedBitmap.width
            cropX = (rotatedBitmap.width - rotatedBitmap.height) / 2
        }

        return Bitmap.createBitmap(rotatedBitmap, cropX, cropY, cropSize, cropSize)
    }

    /**
     * Decodes the file corresponding to the uri into a bitmap:
     * - if a required size is specified (> 0) it gets scaled and stretched to it
     * - if necessary is rotated
     *
     * @param context      the given context
     * @param uri          the uri of the image
     * @param requiredSize the scale size (0 if to keep original)
     * @return the resulting bitmap
     * @throws IOException thrown if an error happens in the process
     */
    @Throws(IOException::class)
    fun decodeAndScaleXYImageUri(context: Context, uri: Uri, requiredSize: Int): Bitmap {
        val o = BitmapFactory.Options()
        o.inJustDecodeBounds = true
        BitmapFactory.decodeStream(context.contentResolver.openInputStream(uri), null, o)

        var scale = 1

        if (requiredSize > 0) {
            val width = o.outWidth
            val height = o.outHeight
            val ratioBitmap = width.toFloat() / height.toFloat()
            val biggerSize = if (ratioBitmap <= 1) height else width
            scale = biggerSize / requiredSize
        }

        val o2 = BitmapFactory.Options()
        o2.inSampleSize = Math.ceil(scale.toDouble()).toInt()
        o2.inJustDecodeBounds = false

        val rotatedBitmap = rotateImageIfRequired(context, BitmapFactory.decodeStream(context.contentResolver.openInputStream(uri), null, o2), uri)

        var width = requiredSize
        var height = requiredSize
        if (requiredSize == 0) {
            width = rotatedBitmap.width
            height = rotatedBitmap.height
        }

        return Bitmap.createBitmap(rotatedBitmap, 0, 0, width, height)
    }

    /**
     * Check the EXIF orientation property of the original file and then if necessary rotates the
     * passed bitmap.
     * If there is an error in the process the original bitmap is returned
     *
     * @param bmp           the image bitmap
     * @param imageUri      image URI
     * @return the resulting bitmap after manipulation
     */
    private fun rotateImageIfRequired(context: Context, bmp: Bitmap, imageUri: Uri): Bitmap {
        return try {
            val contentResolver = context.contentResolver
            contentResolver?.let {
                val ei = ExifInterface(it.openInputStream(imageUri))
                val orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED)

                when (orientation) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> rotateBitmap(bmp, 90F)
                    ExifInterface.ORIENTATION_ROTATE_180 -> rotateBitmap(bmp, 180F)
                    ExifInterface.ORIENTATION_ROTATE_270 -> rotateBitmap(bmp, 270F)
                    else -> bmp
                }
            }
            return bmp
        } catch (ioEx: FileNotFoundException) {
            Log.e(TAG, "Rotate Image EXIF: ", ioEx)
            bmp
        }
    }

    /**
     * Rotates the bitmap by the given radians angle.
     * If there is an error in the rotation process the original bitmap is returned
     *
     * @param source          the bitmap to rotate
     * @param rad             the angle expressed in radians
     * @return the rotated bitmap
     */
    private fun rotateBitmap(source: Bitmap, rad: Double): Bitmap {
        return rotateBitmap(source, Math.toDegrees(rad).toFloat())
    }

    /**
     * Rotates the bitmap by the given degrees angle.
     * If there is an error in the rotation process the original bitmap is returned
     *
     * @param source          the bitmap to rotate
     * @param deg             the angle expressed in degrees
     * @return the rotated bitmap
     */
    private fun rotateBitmap(source: Bitmap, deg: Float): Bitmap {
        var result = source
        if (deg > 0) {
            try {
                val matrix = Matrix()
                matrix.setRotate(deg)
                result = Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, false)
            } catch (illEx: IllegalArgumentException) {
                Log.e(TAG, "Rotate Image illEx: ", illEx)
            } catch (oomErr: OutOfMemoryError) {
                Log.e(TAG, "Rotate Image oomErr: ", oomErr)
            }
        }
        return result
    }
}