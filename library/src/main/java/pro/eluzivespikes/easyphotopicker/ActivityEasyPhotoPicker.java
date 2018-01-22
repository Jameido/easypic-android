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

public class ActivityEasyPhotoPicker {

    public static final int PERMISSION_CAMERA = 300;
    public static final int PERMISSION_WRITE_EXTERNAL_STORAGE = 400;
    public static final int PERMISSION_READ_EXTERNAL_STORAGE = 500;
    public static final int PERMISSION_CAMERA_STORAGE = 345;
    public static final int PERMISSION_STORAGE = 450;
    public static final String DEFAULT_FILENAME = "easy_photo_picker_picture";
    private static final String TAG = ActivityEasyPhotoPicker.class.getSimpleName();
    private static final int REQUEST_RESULT_CAMERA_GALLERY_DEFAULT = 300;
    private static final int DEFAULT_PICTURE_SIZE = 0;

    protected int mRequestCode = REQUEST_RESULT_CAMERA_GALLERY_DEFAULT;
    protected int mPermissionCode = PERMISSION_CAMERA_STORAGE;
    protected Activity mActivity;
    private CoordinatorLayout mCoordinatorLayout;
    protected Uri mOutputFileUri;
    protected String mFilename;
    protected String mProvider;
    protected boolean mShowGallery = true;
    private int mPictureSize = DEFAULT_PICTURE_SIZE;


    protected OnResultListener mOnResultListener;
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
     * @param activity the activity that calls the {@link ActivityEasyPhotoPicker}
     * @param provider the project file provider
     */
    public ActivityEasyPhotoPicker(Activity activity, String provider, boolean showGallery) {
        mActivity = activity;
        mProvider = provider;
        mShowGallery = showGallery;
    }

    /**
     * Sets the requested size for the bigger side of the picture, 0 if no compression is required
     *
     * @param aPictureSize the requested size (0 if no compression is required)
     */
    public void setPictureSize(int aPictureSize) {
        mPictureSize = aPictureSize;
    }

    /**
     * Sets the coordinator layout used to display the snackbar when rationale permissions have to
     * be asked to the user
     *
     * @param coordinatorLayout the given coordinator
     */
    public void setCoordinatorLayout(CoordinatorLayout coordinatorLayout) {
        mCoordinatorLayout = coordinatorLayout;
    }

    /**
     * Set the result listener to be invoked when the picture has been successfully processed
     *
     * @param aOnResultListener the result listener
     */
    public void setOnResultListener(OnResultListener aOnResultListener) {
        mOnResultListener = aOnResultListener;
    }

    /**
     * Removes the activity reference when it gets destroyed
     * Must be called in {@link Activity#onDestroy()} to avoid memory leaks
     */
    public void onDestroy() {
        mActivity = null;
    }

    /**
     * Called from {@link Activity#onActivityResult(int, int, Intent)} and if the request code
     * matches with {@link #mRequestCode} gets the image, compresses it if needed and copies it
     * in the internal storage
     *
     * @param requestCode the result request code
     * @param resultCode  the result result code
     * @param data        the data from the result
     */
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

                    File fileDest = FileUtils.createPictureFile(mActivity, mFilename);
                    if (mPictureSize > 0) {
                        FileUtils.compressAndSavePicture(mActivity, uriFileSrc, fileDest, mPictureSize);
                    } else {
                        FileUtils.copyUriToFile(mActivity, uriFileSrc, fileDest);
                    }

                    if (mOnResultListener != null) {
                        mOnResultListener.onPickPhotoSuccess(fileDest);
                    }
                } catch (Exception ex) {
                    Log.e(TAG, "photo picker", ex);
                    if (mOnResultListener != null) {
                        mOnResultListener.onPickPhotoFailure(ex);
                    }
                }
            } else {
                FileUtils.deleteFileFromUri(mActivity, mOutputFileUri);
            }
        }
    }

    /**
     * Called from {@link Activity#onRequestPermissionsResult(int, String[], int[])}
     * and if the request code matches with {@link #mPermissionCode} checks if the permissions have
     * been given and calls the appropriate {@link OnResultListener} method
     *
     * @param requestCode  the permissions request code
     * @param permissions  the permissions asked
     * @param grantResults the grant permissions results
     */
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


    /**
     * @param missingPermissions
     */
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

    /**
     * If the user has given all the necessary permissions opens the picker
     * If not asks the permissions
     *
     * @param filename
     */
    public void openPicker(String filename) {
        Pair<String[], String> missingPermissions = CameraUtils.checkPermissions(mActivity);
        setFilename(filename);

        if (missingPermissions.first.length == 0) {
            startIntentChooser();
        } else {
            askPermissions(missingPermissions);
        }
    }

    /**
     * Shows the application chooser to the user
     */
    protected void startIntentChooser() {
        try {
            mOutputFileUri = CameraUtils.startIntentChooser(mActivity, mProvider, getTempFilename(), mRequestCode, mShowGallery);
        } catch (IOException ex) {
            Log.e(TAG, "photo picker", ex);
            if (mOnResultListener != null) {
                mOnResultListener.onPickPhotoFailure(ex);
            }
        }
    }

    /**
     * Sets the name of the resulting photo file
     * If it's empty the default one will be used instead
     * Also removes the possible jpg extension
     * @param filename name of the
     */
    private void setFilename(String filename) {
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

    public interface OnResultListener {
        void onPickPhotoSuccess(File file);

        void onPickPhotoFailure(Exception exception);
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
