/*
 * Copyright 2017.  Luca Rossi
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 *
 */

package pro.eluzivespikes.easypic;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.media.ExifInterface;
import android.util.Log;

import java.io.IOException;

/**
 * Created by Luca Rossi on 05/07/2017.
 */

public class ImageUtils {
    private static final String TAG = "ImageUtils";

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
    public static Bitmap decodeAndResizeImageUri(@NonNull final Context context, Uri uri, final int requiredSize)
            throws IOException {
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(context.getContentResolver().openInputStream(uri), null, o);

        int scale = 1;

        if (requiredSize > 0) {
            int width = o.outWidth;
            int height = o.outHeight;
            float ratioBitmap = (float) width / (float) height;
            int biggerSize = ratioBitmap < 1 ? height : width;
            scale = biggerSize / requiredSize;
        }

        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        o2.inJustDecodeBounds = false;

        return rotateImageIfRequired(context, BitmapFactory.decodeStream(context.getContentResolver().openInputStream(uri), null, o2), uri);
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
    public static Bitmap decodeAndCropImageUri(@NonNull final Context context, Uri uri, final int requiredSize)
            throws IOException {
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(context.getContentResolver().openInputStream(uri), null, o);

        int scale = 1;

        if (requiredSize > 0) {
            int width = o.outWidth;
            int height = o.outHeight;
            float ratioBitmap = (float) width / (float) height;
            int smallerSize = ratioBitmap >= 1 ? height : width;
            scale = smallerSize / requiredSize;
        }

        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = (int) Math.ceil(scale);
        o2.inJustDecodeBounds = false;

        Bitmap rotatedBitmap = rotateImageIfRequired(context, BitmapFactory.decodeStream(context.getContentResolver().openInputStream(uri), null, o2), uri);

        int cropSize;
        int cropX = 0;
        int cropY = 0;

        if (rotatedBitmap.getHeight() > rotatedBitmap.getWidth()) {
            cropSize = rotatedBitmap.getWidth();
            cropY = (rotatedBitmap.getHeight() - rotatedBitmap.getWidth()) / 2;
        } else {
            cropSize = rotatedBitmap.getWidth();
            cropX = (rotatedBitmap.getWidth() - rotatedBitmap.getHeight()) / 2;
        }

        return Bitmap.createBitmap(rotatedBitmap, cropX, cropY, cropSize, cropSize);
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
    public static Bitmap decodeAndScaleXYImageUri(@NonNull final Context context, Uri uri, final int requiredSize)
            throws IOException {
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(context.getContentResolver().openInputStream(uri), null, o);

        int scale = 1;

        if (requiredSize > 0) {
            int width = o.outWidth;
            int height = o.outHeight;
            float ratioBitmap = (float) width / (float) height;
            int biggerSize = ratioBitmap <= 1 ? height : width;
            scale = biggerSize / requiredSize;
        }

        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = (int) Math.ceil(scale);
        o2.inJustDecodeBounds = false;

        Bitmap rotatedBitmap = rotateImageIfRequired(context, BitmapFactory.decodeStream(context.getContentResolver().openInputStream(uri), null, o2), uri);

        int width = requiredSize;
        int height = requiredSize;
        if (requiredSize == 0) {
            width = rotatedBitmap.getWidth();
            height = rotatedBitmap.getHeight();
        }

        return Bitmap.createBitmap(rotatedBitmap, 0, 0, width, height);
    }

    /**
     * Check the EXIF orientation property of the original file and then if necessary rotates the
     * passed bitmap.
     *
     * @param img           The image bitmap
     * @param selectedImage Image URI
     * @return The resulted Bitmap after manipulation
     */
    private static Bitmap rotateImageIfRequired(@NonNull final Context context, Bitmap img, Uri selectedImage) throws IOException {

        ExifInterface ei = new ExifInterface(context.getContentResolver().openInputStream(selectedImage));
        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);

        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return rotateImage(img, 90);
            case ExifInterface.ORIENTATION_ROTATE_180:
                return rotateImage(img, 180);
            case ExifInterface.ORIENTATION_ROTATE_270:
                return rotateImage(img, 270);
            default:
                return img;
        }
    }

    /**
     * Rotates the bitmap by the given angle, if there is an error in the rotation process the
     * original bitmap is returned
     *
     * @param source          the bitmap to rotate
     * @param rotationDegrees the angle expressed in degrees
     * @return the rotated bitmap
     */
    public static Bitmap rotateImage(Bitmap source, int rotationDegrees) {
        Bitmap result = source;
        try {
            if (rotationDegrees != 0) {
                Matrix matrix = new Matrix();
                matrix.setRotate(rotationDegrees);
                result = Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
            }
        } catch (Exception e) {
            Log.e(TAG, "rotateImage: ", e);
        }
        return result;
    }
}
