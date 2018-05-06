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
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Luca Rossi on 05/07/2017.
 */

public class FileUtils {

    /**
     * Creates a temporary jpg file with the given name
     * If the name contains the extension it removes it
     *
     * @param context  the given context
     * @param filename the name of the created file
     * @return the created picture file
     * @throws IOException thrown if an error happens while creating the temp file
     */
    public static File createPictureTempFile(Context context, @NonNull String filename) throws IOException {
        if (filename.endsWith(".jpg")) {
            filename = filename.replace(".jpg", "");
        }
        File tempProfilePic = File.createTempFile(
                filename,
                ".jpg",
                context.getCacheDir()
                );
        tempProfilePic.deleteOnExit();

        return tempProfilePic;
    }

    /**
     * Creates a jpg file with the given name
     * If the name contains the extension it removes it
     *
     * @param context  the given context
     * @param filename the name of the created file
     * @return the created picture file
     */
    public static File createPictureFile(Context context, @NonNull String filename) {
        if (filename.endsWith(".jpg")) {
            filename = filename.replace(".jpg", "");
        }
        return new File(
                context.getFilesDir().getPath() +
                        File.separator +
                        filename +
                        ".jpg"
        );
    }

    /**
     * Takes the image from the given uri, scales it down to the given size and saves the result in
     * the given file
     *
     * @param context      the given context
     * @param src          the source image url
     * @param dst          the destination file
     * @param requiredSize the scale size
     * @return the compression result
     * @throws IOException thrown if an error happens
     */
    public static boolean compressAndSavePicture(Context context, Uri src, File dst, int requiredSize) throws IOException {
        if (src.getPath().equals(dst.getAbsolutePath())) {
            return true;
        } else {
            ImageProcessor vImageProcessor = new ImageProcessor();
            Bitmap bmp = vImageProcessor.decodeAndResizeImageUri(context, src, requiredSize);
            FileOutputStream out = null;
            try {
                out = new FileOutputStream(dst);
                bmp.compress(Bitmap.CompressFormat.JPEG, 100, out);
            } catch (IOException e) {
                e.printStackTrace();
                return false;

            } finally {
                try {
                    if (out != null) {
                        out.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                bmp.recycle();
            }
            return true;
        }
    }

    /**
     * Copies the content of the file with the given url in the destination one
     *
     * @param context the given context
     * @param src     the source file url
     * @param dst     the destination file
     * @return the v result
     * @throws IOException thrown if an error happens
     */
    static boolean copyUriToFile(Context context, Uri src, File dst) throws IOException {
        if (src.getPath().equals(dst.getAbsolutePath())) {
            return true;
        } else {
            InputStream inputStream = null;
            OutputStream outputStream = null;

            try {
                inputStream = context.getContentResolver().openInputStream(src);
                outputStream = new FileOutputStream(dst);

                int read;
                byte[] bytes = new byte[1024];

                while ((read = inputStream.read(bytes)) != -1) {
                    outputStream.write(bytes, 0, read);
                }
            } catch (IOException e) {
                return false;
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return true;
    }

    /**
     * Copies the content of the file with the given url in the destination one
     *
     * @param src the source file
     * @param dst the destination file
     * @return the copy result
     * @throws IOException thrown if an error happens
     */
    public static boolean copyFile(File src, File dst) throws IOException {
        if (src.getAbsolutePath().equals(dst.getAbsolutePath())) {
            return true;
        } else {
            InputStream inputStream = null;
            OutputStream outputStream = null;

            try {
                inputStream = new FileInputStream(src);
                outputStream = new FileOutputStream(dst);

                int read;
                byte[] bytes = new byte[1024];

                while ((read = inputStream.read(bytes)) != -1) {
                    outputStream.write(bytes, 0, read);
                }
            } catch (IOException e) {
                return false;
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return true;
    }

    /**
     * @param inputStream
     * @param dst
     * @return
     * @throws IOException
     */
    public static boolean copyFile(InputStream inputStream, File dst) throws IOException {
        OutputStream outputStream = null;

        try {
            outputStream = new FileOutputStream(dst);

            int read;
            byte[] bytes = new byte[1024];

            while ((read = inputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, read);
            }
        } catch (IOException e) {
            return false;
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return true;
    }

    /**
     * Deletes the file with the give uri
     *
     * @param fileUri the uri to delete
     * @param context the given context
     */
    public static void deleteFileFromUri(Context context, Uri fileUri) {
        try {
            int result = context.getContentResolver().delete(fileUri, null, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * When image is returned we get its real path
     *
     * @param context the given context
     * @param contentURI uri iof the content
     * @return returns the real uri
     */
    public static String getRealPathFromURI(Context context, Uri contentURI) {
        String result;
        Cursor cursor = context.getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) {
            // Source is Dropbox or other similar local file path
            result = contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
    }
}
