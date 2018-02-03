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

package pro.eluzivespikes.easyphotopicker;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.support.v4.util.Pair;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Luca Rossi on 05/07/2017.
 */

public class CameraUtils {


    /**
     * Starts a camera/gallery selector and returns the output file uri.
     * If the file has been successfully created start the intent chooser from the given fragment.
     *
     * @param context     the given context
     * @param fragment    the fragment to start the selector from
     * @param authority   the file provider package
     * @param fileName    the requested name for the file
     * @param requestCode request code for the activity result
     * @return the intent chooser
     * @throws IOException thrown if an error happens while creating the temp file
     */
    public static Uri startIntentChooser(final Context context, Fragment fragment, String authority, String fileName, int requestCode, boolean showGallery) throws IOException {
        Uri outputFileUri = null;
        File photoFile = FileUtils.createPictureTempFile(context, fileName);
        if (photoFile != null) {
            outputFileUri = FileProvider.getUriForFile(
                    context,
                    authority,
                    photoFile);
            fragment.startActivityForResult(getIntentChooser(context, outputFileUri, showGallery), requestCode);
        }
        return outputFileUri;
    }

    /**
     * Starts a camera/gallery selector and returns the output file uri.
     *
     * @param activity    the activity to start the selector from
     * @param authority   the file provider package
     * @param fileName    the requested name for the file
     * @param requestCode request code for the activity result
     * @return the intent chooser
     * @throws IOException thrown if an error happens while creating the temp file
     */
    public static Uri startIntentChooser(Activity activity, String authority, String fileName, int requestCode, boolean showGallery) throws IOException {
        Uri outputFileUri = null;
        File photoFile = FileUtils.createPictureTempFile(activity, fileName);
        // Continue only if the File was successfully created
        if (photoFile != null) {
            outputFileUri = FileProvider.getUriForFile(
                    activity,
                    authority,
                    photoFile);
            activity.startActivityForResult(getIntentChooser(activity, outputFileUri, showGallery), requestCode);
        }
        return outputFileUri;
    }

    /**
     * Puts together the results of {@link #getGalleryIntents(Context)} and
     * {@link #getCameraIntents(Context, Uri)} to form and intent used to show the
     * chooser to the user
     *
     * @param context       the given context
     * @param outputFileUri the uri where store the image
     * @param showGallery   if gallery apps should be included
     * @return intent to display the chooser
     * @throws IOException thrown if an error happens while creating the temp file
     */
    public static Intent getIntentChooser(final Context context, Uri outputFileUri, boolean showGallery) throws IOException {

        List<Intent> selectorIntents = new ArrayList<>();
        selectorIntents.addAll(getCameraIntents(context, outputFileUri));
        if (showGallery) {
            selectorIntents.addAll(getGalleryIntents(context));
        }

        Intent targetIntent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            targetIntent = new Intent();
        else {
            targetIntent = selectorIntents.get(selectorIntents.size() - 1);
            selectorIntents.remove(selectorIntents.size() - 1);
        }

        final Intent chooserIntent = Intent.createChooser(targetIntent, context.getString(R.string.select_picture_source));
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, selectorIntents.toArray(new Parcelable[]{}));

        return chooserIntent;
    }


    /**
     * Builds a list of intents for all the camera apps installed on the device
     *
     * @param context       the given context
     * @param outputFileUri the uri where the file will be saved in
     * @return the intents used to launch a camera app
     */
    private static List<Intent> getCameraIntents(final Context context, Uri outputFileUri) {
        final List<Intent> cameraIntents = new ArrayList<>();
        final Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        final List<ResolveInfo> listCam = context.getPackageManager().queryIntentActivities(captureIntent, 0);

        for (ResolveInfo res : listCam) {
            final Intent intent = new Intent(captureIntent);
            intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            context.grantUriPermission(res.activityInfo.packageName, outputFileUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setPackage(res.activityInfo.packageName);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
            cameraIntents.add(intent);
        }

        return cameraIntents;
    }

    /**
     * Builds a list of intents for all the gallery apps installed on the device
     * Documents app are removed from the list
     *
     * @param context the given context
     * @return the intents used to launch a gallery app
     */
    private static List<Intent> getGalleryIntents(final Context context) {
        // Add all gallery apps as options
        List<Intent> intents = new ArrayList<>();
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        List<ResolveInfo> listGallery = context.getPackageManager().queryIntentActivities(galleryIntent, 0);

        for (ResolveInfo res : listGallery) {
            //We remove the documents app
            if ("com.android.documentsui.DocumentsActivity".equals(res.activityInfo.name)) {
                continue;
            }

            Intent intent = new Intent(galleryIntent);
            intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            intent.setPackage(res.activityInfo.packageName);
            intents.add(intent);
        }

        return intents;
    }


    /**
     * Checks if all the necessary permissions have been given and returns the ones that
     * still need to be granted by the user
     *
     * @param activity the activity from which the picker is called
     * @return the loist of missing permissions
     */
    @NonNull
    public static Pair<String[], String> getMissingPermissions(Activity activity) {
        final List<String> permissionsList = new ArrayList<>();
        String permissionsMessage = "";
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
            if (activity.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                permissionsList.add(Manifest.permission.CAMERA);

                if (activity.shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                    permissionsMessage += activity.getString(R.string.camera_permission_rationale);
                }
            }

            boolean askStorageRationale = false;

            if (activity.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissionsList.add(Manifest.permission.READ_EXTERNAL_STORAGE);
                askStorageRationale = activity.shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }

            if (activity.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissionsList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                askStorageRationale = activity.shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }

            //We add a single message both for read and write permissions
            if (askStorageRationale) {
                permissionsMessage += activity.getString(R.string.storage_permission_rationale);
            }

            if (permissionsList.size() > 0) {
                return new Pair<>(permissionsList.toArray(new String[permissionsList.size()]), permissionsMessage);
            }
        }
        return new Pair<>(new String[0], "");
    }
}
