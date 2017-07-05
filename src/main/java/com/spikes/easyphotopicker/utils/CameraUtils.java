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

package com.spikes.easyphotopicker.utils;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;

import com.spikes.easyphotopicker.R;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Luca Rossi on 05/07/2017.
 */

public class CameraUtils {

    /**
     * Starts a camera/gallery selector and returns the output file uri
     *
     * @param context
     * @param fragment    the fragment to start the selector from
     * @param authority   the fileprovider package
     * @param fileName    the requested name for the file
     * @param requestCode request code for the activity result
     * @return the intent chooser
     * @throws IOException
     */
    public static Uri startIntentChooser(Context context, Fragment fragment, String authority, String fileName, int requestCode, boolean showGallery) throws IOException {
        Uri outputFileUri = null;
        File photoFile = FileUtils.getPictureTempFile(context, fileName);
        // Continue only if the File was successfully created
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
     * Starts a camera/gallery selector and returns the output file uri
     *
     * @param activity    the activity to start the selector from
     * @param authority   the fileprovider package
     * @param fileName    the requested name for the file
     * @param requestCode request code for the activity result
     * @return the intent chooser
     * @throws IOException
     */
    public static Uri startIntentChooser(Activity activity, String authority, String fileName, int requestCode, boolean showGallery) throws IOException {
        Uri outputFileUri = null;
        File photoFile = FileUtils.getPictureTempFile(activity, fileName);
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

    private static Intent getIntentChooser(Context context, Uri outputFileUri, boolean showGallery) throws IOException {

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


    private static List<Intent> getCameraIntents(Context context, Uri outputFileUri) {
        // Add all camera apps as options
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

    private static List<Intent> getGalleryIntents(Context context) {
        // Add all gallery apps as options
        List<Intent> intents = new ArrayList<>();
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        List<ResolveInfo> listGallery = context.getPackageManager().queryIntentActivities(galleryIntent, 0);

        for (ResolveInfo res : listGallery) {
            //We remove the documents app
            if("com.android.documentsui.DocumentsActivity".equals(res.activityInfo.name)){
                continue;
            }

            Intent intent = new Intent(galleryIntent);
            intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            intent.setPackage(res.activityInfo.packageName);
            intents.add(intent);
        }

        return intents;
    }
}
