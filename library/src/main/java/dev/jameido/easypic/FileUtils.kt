package dev.jameido.easypic

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.IOException

/**
 * @author Jameido
 * @since 3
 *
 * Collection of utils functions for managing files
 */
object FileUtils {

    /**
     * Creates a temporary jpg file with the given name
     * If the name contains the extension it removes it
     *
     * @param context  the given context
     * @param filename the name of the created file
     * @return the created picture file
     * @throws IOException thrown if an error happens while creating the temp file
     */
    @Throws(IOException::class)
    fun createPictureTempFile(context: Context, filename: String): File {
        var vFilename = filename
        if (vFilename.endsWith(".jpg")) {
            vFilename = vFilename.replace(".jpg", "")
        }
        val tempProfilePic = File.createTempFile(
                vFilename,
                ".jpg",
                context.cacheDir
        )
        tempProfilePic.deleteOnExit()

        return tempProfilePic
    }

    /**
     * Creates a jpg file with the given name
     * If the name contains the extension it removes it
     *
     * @param context  the given context
     * @param filename the name of the created file
     * @return the created picture file
     */
    fun createPictureFile(context: Context, filename: String): File {
        var vFilename = filename
        if (vFilename.endsWith(".jpg")) {
            vFilename = vFilename.replace(".jpg", "")
        }
        return File(
                context.filesDir.path +
                        File.separator +
                        vFilename +
                        ".jpg"
        )
    }

    /**
     * Deletes the file with the give uri
     *
     * @param fileUri the uri to delete
     * @param context the given context
     */
    fun deleteFileFromUri(context: Context, fileUri: Uri) {
        try {
            context.contentResolver?.delete(fileUri, null, null)
        } catch (ioEx: IOException) {
            ioEx.printStackTrace()
        } catch (illEx: IllegalArgumentException) {
            illEx.printStackTrace()
        }
    }
}