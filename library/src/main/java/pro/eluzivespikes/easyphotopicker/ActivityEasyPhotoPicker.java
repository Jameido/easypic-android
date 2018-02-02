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
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.IntDef;
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
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
    protected boolean mShowGallery = false;
    private int mPictureSize = DEFAULT_PICTURE_SIZE;
    private int[] mModes;
    private ProcessResultTask mProcessResultTask;


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
    protected ActivityEasyPhotoPicker(Activity activity, String provider) {
        mActivity = activity;
        mProvider = provider;
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
        if (mProcessResultTask != null) {
            mProcessResultTask.cancel(true);
        }
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
                Uri uriFileSrc = isCamera ? mOutputFileUri : data.getData();
                mProcessResultTask = new ProcessResultTask();
                mProcessResultTask.execute(uriFileSrc);
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
     *
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

    public void setRequestCode(int aRequestCode) {
        mRequestCode = aRequestCode;
    }

    public void setShowGallery(boolean aShowGallery) {
        mShowGallery = aShowGallery;
    }

    public void setModes(int[] aModes) {
        mModes = aModes;
    }

    public int getRequestCode() {
        return mRequestCode;
    }

    public interface OnResultListener {
        void onPickPhotoSuccess(@NonNull PickerResult pickerResult);

        void onPickPhotoFailure(@NonNull Exception exception);
    }

    public interface OnPermissionResult {
        void onPermissionsGranted();

        void onPermissionsDenied(String[] permissions);
    }

    public interface OnPermissionCheckResult {
        void onSuccess();

        void onFailure(String[] missingPermissions, boolean rationale, String rationaleMessage);
    }

    public static class Builder {

        private ActivityEasyPhotoPicker mActivityEasyPhotoPicker;

        public Builder(Activity activity, String provider) {
            mActivityEasyPhotoPicker = new ActivityEasyPhotoPicker(activity, provider);
        }

        public Builder withResultListener(OnResultListener resultListener) {
            this.mActivityEasyPhotoPicker.setOnResultListener(resultListener);
            return this;
        }

        public Builder withCoordinatorLayout(CoordinatorLayout coordinatorLayout) {
            this.mActivityEasyPhotoPicker.setCoordinatorLayout(coordinatorLayout);
            return this;
        }

        public Builder withPictureSize(int pictureSize) {
            this.mActivityEasyPhotoPicker.setPictureSize(pictureSize);
            return this;
        }

        public Builder withRequestCode(int requestCode) {
            this.mActivityEasyPhotoPicker.setRequestCode(requestCode);
            return this;
        }

        public Builder withGallery(boolean showGallery) {
            this.mActivityEasyPhotoPicker.setShowGallery(showGallery);
            return this;
        }

        public Builder withFileName(String fileName) {
            this.mActivityEasyPhotoPicker.setFilename(fileName);
            return this;
        }

        public Builder withModes(int... modes) {
            this.mActivityEasyPhotoPicker.setModes(modes);
            return this;
        }

        public ActivityEasyPhotoPicker build() {
            return mActivityEasyPhotoPicker;
        }
    }

    /**
     * Used to indicate the type of
     */
    @Target({ElementType.FIELD,
            ElementType.LOCAL_VARIABLE,
            ElementType.PARAMETER,
            ElementType.ANNOTATION_TYPE})
    @IntDef({
            PickerMode.BITMAP,
            PickerMode.BYTES,
            PickerMode.FILE
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface PickerMode {
        /**
         * A bitmap representing the image will be returned
         */
        int BITMAP = 0;
        /**
         * A byte array representing the image will be returned
         */
        int BYTES = 1;
        /**
         * A file containing the image will be returned
         */
        int FILE = 2;
    }

    public class ProcessResultTask extends AsyncTask<Uri, Void, PickerResult> {
        Exception mException;

        @Override
        protected PickerResult doInBackground(Uri... aUris) {
            PickerResult vPickerResult = new PickerResult();
            if (aUris.length > 0 && aUris[0] != null) {
                try {
                    Bitmap vBitmap = resultBitmap(aUris[0]);

                    for (int vMode : mModes) {
                        switch (vMode) {
                            case PickerMode.BITMAP:
                                vPickerResult.setBitmap(vBitmap);
                                break;
                            case PickerMode.BYTES:
                                vPickerResult.setBytes(resultBytes(vBitmap));
                                break;
                            case PickerMode.FILE:
                                vPickerResult.setFile(resultFile(vBitmap));
                                break;
                        }
                    }
                } catch (Exception ex) {
                    Log.e(TAG, "photo picker", ex);
                    mException = ex;
                }
            }

            return vPickerResult;
        }

        @Override
        protected void onPostExecute(@NonNull PickerResult aPickerResult) {
            if (mOnResultListener != null) {
                if (mException != null) {
                    mOnResultListener.onPickPhotoFailure(mException);
                } else {
                    mOnResultListener.onPickPhotoSuccess(aPickerResult);
                }
            }
        }


        private Bitmap resultBitmap(Uri aSourceUri) throws IOException {
            return ImageUtils.decodeAndResizeImageUri(mActivity, aSourceUri, mPictureSize);
        }

        private File resultFile(Bitmap aBitmap) throws IOException {
            FileOutputStream out = null;
            File vDestFile = FileUtils.createPictureFile(mActivity, mFilename);
            try {
                out = new FileOutputStream(vDestFile);
                aBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            } finally {
                if (out != null) {
                    out.close();
                }
            }
            return vDestFile;
        }

        private byte[] resultBytes(Bitmap aBitmap) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            aBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            return stream.toByteArray();
        }
    }
}
