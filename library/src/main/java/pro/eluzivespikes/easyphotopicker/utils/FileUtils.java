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

package pro.eluzivespikes.easyphotopicker.utils;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
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

    public static File getPictureTempFile(Context context, @NonNull String filename) throws IOException {
        if(filename.endsWith(".jpg")){
            filename = filename.replace(".jpg", "");
        }
        File tempProfilePic = File.createTempFile(
                filename,
                ".jpg",
                context.getExternalCacheDir());
        tempProfilePic.deleteOnExit();

        return tempProfilePic;
    }

    public static File getPictureFile(Context context, @NonNull String filename) throws IOException {
        if(filename.endsWith(".jpg")){
            filename = filename.replace(".jpg", "");
        }
        return new File(
                context.getExternalFilesDir(Environment.DIRECTORY_PICTURES).getPath() +
                        File.separator +
                        filename +
                        ".jpg"
        );
    }


    public static boolean compressAndSavePicture(Context context, Uri src, File dst, int requiredSize) throws IOException {
        if (src.getPath().equals(dst.getAbsolutePath())) {
            return true;
        } else {
            Bitmap bmp = ImageUtils.decodeAndResizeImageUri(context, src, requiredSize);
            FileOutputStream out = null;
            try {
                out = new FileOutputStream(dst);
                bmp.compress(Bitmap.CompressFormat.JPEG, 100, out); // bmp is your Bitmap instance
                // PNG is a lossless format, the compression factor (100) is ignored
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

    public static boolean copyUriToFile(Uri src, File dst, Context context) throws IOException {
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

    public static void deleteFileFromUri(Uri fileUri, Context context) {
        try {
            int result = context.getContentResolver().delete(fileUri, null, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * When image is returned we get its real path
     **/
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
