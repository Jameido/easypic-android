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

package com.spikes.easyphotopicker;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.spikes.easyphotopicker.utils.CameraUtils;
import com.spikes.easyphotopicker.utils.FileUtils;
import com.spikes.easyphotopicker.utils.PermissionsCompat;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Luca Rossi on 05/07/2017.
 * <p>
 * be sure to add the necessary permissions in the manifest:
 * <uses-permission android:name="android.permission.CAMERA"/>
 * <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"/>
 * <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
 * <p>
 * also don't forget to add the following provider:
 * <application>
 * <provider
 * android:name="android.support.v4.content.FileProvider"
 * android:authorities="{packagename}.fileprovider"
 * android:exported="false"
 * android:grantUriPermissions="true">
 * <meta-data
 * android:name="android.support.FILE_PROVIDER_PATHS"
 * android:resource="@xml/file_paths"/>
 * </provider>
 * </application>
 */

public class ActivityEasyCameraPicker {

    public static final int PERMISSION_CAMERA = 300;
    public static final int PERMISSION_WRITE_EXTERNAL_STORAGE = 400;
    public static final int PERMISSION_READ_EXTERNAL_STORAGE = 500;
    public static final int PERMISSION_CAMERA_STORAGE = 345;
    public static final int PERMISSION_STORAGE = 450;
    public static final String DEFAULT_FILENAME = "easy_photo_picker_picture.jpg";
    private static final String TAG = ActivityEasyCameraPicker.class.getSimpleName();
    private static final int REQUEST_RESULT_CAMERA_GALLERY_DEFAULT = 300;

    protected int mRequestCode = REQUEST_RESULT_CAMERA_GALLERY_DEFAULT;
    protected int mPermissionCode = PERMISSION_CAMERA_STORAGE;
    protected Activity mActivity;
    private CoordinatorLayout mCoordinatorLayout;
    protected Uri mOutputFileUri;
    protected String mFilename;
    protected String mProvider;
    protected boolean mShowGallery = true;


    private OnResult mOnResult;
    private OnPermissionResult mOnPermissionResult = new OnPermissionResult() {
        @Override
        public void onPermissionsGranted() {
            startIntentChooser();
        }

        @Override
        public void onPermissionsDenied(String[] permissions) {
            openPicker(mFilename);
        }
    };

    /**
     * @param activity the activity that calls the {@link ActivityEasyCameraPicker}
     * @param provider the project file provider
     */
    public ActivityEasyCameraPicker(Activity activity, String provider, boolean showGallery) {
        mActivity = activity;
        mProvider = provider;
        mShowGallery = showGallery;
    }

    public void setRequestCode(int requestCode) {
        mRequestCode = requestCode;
    }

    public void setCoordinatorLayout(CoordinatorLayout coordinatorLayout) {
        mCoordinatorLayout = coordinatorLayout;
    }

    public void setOnPermissionResult(OnPermissionResult onPermissionResult) {
        mOnPermissionResult = onPermissionResult;
    }

    public void setOnResult(OnResult onResult) {
        mOnResult = onResult;
    }

    public void onDestroy() {
        mActivity = null;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == mRequestCode) {
            if (resultCode == Activity.RESULT_OK) {
                boolean isCamera = true;
                if (data != null && data.getData() != null) {
                    String action = data.getAction();
                    isCamera = MediaStore.ACTION_IMAGE_CAPTURE.equals(action);
                }

                try {
                    Uri uriFileSrc = isCamera ? mOutputFileUri : data.getData();

                    File fileDest = FileUtils.getPictureFile(mActivity, mFilename);
                    FileUtils.compressAndSavePicture(mActivity, uriFileSrc, fileDest, 400);

                    if (mOnResult != null) {
                        mOnResult.onSuccess(fileDest);
                    }
                } catch (Exception ex) {
                    Toast.makeText(mActivity, R.string.error_creating_file, Toast.LENGTH_SHORT).show();
                }
            } else {
                FileUtils.deleteFileFromUri(mOutputFileUri, mActivity);
            }
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        if (requestCode != mPermissionCode) {
            return;
        }

        if (mOnPermissionResult == null) {
            return;
        }

        List<String> permissionsDenied = new ArrayList<>();

        for (int i = 0; i < permissions.length; i++) {
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                permissionsDenied.add(permissions[i]);
            }
        }

        if (permissionsDenied.size() > 0) {
            mOnPermissionResult.onPermissionsDenied(permissionsDenied.toArray(new String[permissionsDenied.size()]));
        } else {
            mOnPermissionResult.onPermissionsGranted();
        }
    }


    @TargetApi(Build.VERSION_CODES.M)
    private void askPermissions(final Pair<String[], String> missingPermissions) {
        if (TextUtils.isEmpty(missingPermissions.second)) {
            requestPermissions(missingPermissions.first);
        } else {
            showSnackBar(
                    missingPermissions.second,
                    Snackbar.LENGTH_INDEFINITE,
                    mActivity.getString(android.R.string.ok),
                    new View.OnClickListener() {
                        @RequiresApi(api = Build.VERSION_CODES.M)
                        @Override
                        public void onClick(View view) {
                            requestPermissions(missingPermissions.first);
                        }
                    }
            );
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    protected void requestPermissions(String[] permissions) {
        mActivity.requestPermissions(permissions, mPermissionCode);
    }

    private void showSnackBar(String message, int duration, String action, View.OnClickListener actionListener) {
        if (mCoordinatorLayout == null) {
            Log.e(TAG, "Unable to open snackbar: Coordinator layout not set");
            return;
        }
        Snackbar mSnackbar = Snackbar.make(mCoordinatorLayout, message, duration);
        TextView tv = (TextView) mSnackbar.getView().findViewById(R.id.snackbar_text);
        tv.setTextColor(Color.WHITE);
        if (action != null && actionListener != null) {
            mSnackbar.setAction(action, actionListener);
            mSnackbar.setActionTextColor(ContextCompat.getColor(mActivity, android.R.color.white));
        }
        mSnackbar.show();
    }

    protected String getTempFilename() {
        return "temp" + mFilename;
    }

    public void openPicker(String filename) {
        Pair<String[], String> missingPermissions = CameraUtils.checkPermissions(mActivity);

        if (missingPermissions.first.length == 0) {
            setFilename(filename);
            startIntentChooser();
        }else {
            askPermissions(missingPermissions);
        }
    }

    protected void startIntentChooser() {
        try {
            mOutputFileUri = CameraUtils.startIntentChooser(mActivity, mProvider, getTempFilename(), mRequestCode, mShowGallery);
        } catch (IOException ex) {
            Toast.makeText(mActivity, R.string.error_creating_file, Toast.LENGTH_SHORT).show();
        }
    }

    public void setFilename(String filename) {
        if (TextUtils.isEmpty(filename)) {
            filename = DEFAULT_FILENAME;
        } else if (filename.endsWith(".jpg")) {
            filename = filename.replace(".jpg", "");
        }
        mFilename = filename;
    }

    public int getRequestCode() {
        return mRequestCode;
    }

    public interface OnResult {
        void onSuccess(File file);
    }

    public interface OnPermissionResult {
        void onPermissionsGranted();

        void onPermissionsDenied(String[] permissions);
    }

    public interface OnPermissionCheckResult {
        void onSuccess();

        void onFailure(String[] missingPermissions, boolean rationale, String rationaleMessage);
    }
}
