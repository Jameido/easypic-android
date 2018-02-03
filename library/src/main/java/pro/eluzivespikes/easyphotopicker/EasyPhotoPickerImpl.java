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
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.util.Pair;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Luca Rossi on 02/02/2018.
 */
abstract class EasyPhotoPickerImpl implements EasyPhotoPicker {

    private static final int PERMISSION_CAMERA_STORAGE = 345;
    private static final String DEFAULT_FILENAME = "easy_photo_picker_picture";
    private static final String TAG = EasyPhotoPickerImpl.class.getSimpleName();
    private static final int REQUEST_RESULT_CAMERA_GALLERY_DEFAULT = 300;
    private static final int DEFAULT_PICTURE_SIZE = 0;

    private int mRequestCode = REQUEST_RESULT_CAMERA_GALLERY_DEFAULT;
    private int mPermissionCode = PERMISSION_CAMERA_STORAGE;
    private CoordinatorLayout mCoordinatorLayout;
    private Uri mOutputFileUri;
    private String mFilename = DEFAULT_FILENAME;
    private String mProvider;
    private boolean mShowGallery = false;
    private int mPictureSize = DEFAULT_PICTURE_SIZE;
    private int[] mModes;
    private EasyPhotoPickerImpl.ProcessResultTask mProcessResultTask;

    private EasyPhotoPickerImpl.OnResultListener mOnResultListener;

    /**
     * Set the result listener to be invoked when the picture has been successfully processed.
     *
     * @param resultListener the result listener
     */
    void setOnResultListener(EasyPhotoPickerImpl.OnResultListener resultListener) {
        mOnResultListener = resultListener;
    }

    /**
     * Sets the coordinator layout used to display the snackbar when rationale permissions have to
     * be asked to the user.
     *
     * @param coordinatorLayout the given coordinator
     */
    void setCoordinatorLayout(CoordinatorLayout coordinatorLayout) {
        mCoordinatorLayout = coordinatorLayout;
    }

    /**
     * Sets the requested size for the bigger side of the picture, 0 if no compression is required.
     *
     * @param pictureSize the requested size (0 if no compression is required)
     */
    void setPictureSize(int pictureSize) {
        mPictureSize = pictureSize;
    }

    /**
     * Sets the name of the resulting photo file
     * If it's empty the default one will be used instead
     * Also removes the possible jpg extension.
     * <p>
     * TODO: improve this function to accept also PNG
     *
     * @param filename name of the
     */
    void setFilename(String filename) {
        if (TextUtils.isEmpty(filename)) {
            filename = DEFAULT_FILENAME;
        } else if (filename.endsWith(".jpg")) {
            filename = filename.replace(".jpg", "");
        }
        mFilename = filename;
    }

    /**
     * Sets the request code used when invoking
     * {@link Activity#startActivityForResult(Intent, int)} or
     * {@link android.support.v4.app.Fragment#startActivityForResult(Intent, int)}.
     *
     * @param requestCode the given request code
     */
    void setRequestCode(int requestCode) {
        mRequestCode = requestCode;
    }

    /**
     * Sets a bool indicating if also the gallery activities should be shown.
     *
     * @param showGallery the bool
     */
    void setShowGallery(boolean showGallery) {
        mShowGallery = showGallery;
    }

    /**
     * Sets a list of modes on how the resulting picture can be returned, possible values are:
     * {@link PickerMode#BITMAP}
     * {@link PickerMode#BYTES}
     * {@link PickerMode#FILE}
     *
     * @param aModes the chosen modes
     */
    void setModes(int... aModes) {
        mModes = aModes;
    }

    /**
     * Sets the provider string used in {@link FileProvider#getUriForFile(Context, String, File)}
     *
     * @param provider the string provider.
     */
    void setProvider(String provider) {
        mProvider = provider;
    }

    /**
     * Returns the uri of the file where the picture is stored.
     *
     * @throws IOException thrown if an error happens while creating the temporary file where store
     *                     the picture
     */
    private Uri getOutputUri() throws IOException {
        Uri outputFileUri = null;
        File photoFile = FileUtils.createPictureTempFile(getContext(), getTempFilename());
        if (photoFile != null) {
            outputFileUri = FileProvider.getUriForFile(
                    getContext(),
                    mProvider,
                    photoFile);
        }
        return outputFileUri;
    }

    /**
     * Removes the activity reference when it gets destroyed
     * Must be called in {@link Activity#onDestroy()} to avoid memory leaks.
     */
    @CallSuper
    public void onDestroy() {
        if (mProcessResultTask != null) {
            mProcessResultTask.cancel(true);
        }
    }

    /**
     * Called from {@link Activity#onActivityResult(int, int, Intent)} and if the request code
     * matches with {@link #mRequestCode} gets the image, compresses it if needed and copies it
     * in the internal storage.
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
                FileUtils.deleteFileFromUri(getContext(), mOutputFileUri);
            }
        }
    }

    /**
     * Called from {@link Activity#onRequestPermissionsResult(int, String[], int[])}
     * and if the request code matches with {@link #mPermissionCode} checks if the permissions have
     * been given and calls the appropriate {@link EasyPhotoPickerImpl.OnResultListener} method.
     *
     * @param requestCode  the permissions request code
     * @param permissions  the permissions asked
     * @param grantResults the grant permissions results
     */
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        if (requestCode != mPermissionCode) {
            return;
        }

        List<String> permissionsDenied = new ArrayList<>();

        for (int i = 0; i < permissions.length; i++) {
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                permissionsDenied.add(permissions[i]);
            }
        }

        if (permissionsDenied.size() > 0) {
            startIntentChooser();
        } else {
            openPicker();
        }
    }


    /**
     * Asks the user the missing permissions, with the standard system alert if they haven't been
     * already negated once otherwise are asked in a "rationale" way (custom alert or snackbar).
     *
     * @param missingPermissions list of missing permissions with the message for the rationale
     *                           request
     */
    @TargetApi(Build.VERSION_CODES.M)
    private void askPermissions(final Pair<String[], String> missingPermissions) {
        if (TextUtils.isEmpty(missingPermissions.second)) {
            requestPermissions(missingPermissions.first, mPermissionCode);
        } else {
            askRationalePermissions(missingPermissions);
        }
    }

    /**
     * Asks the user to give permissions showing a {@link Snackbar} if possible or an
     * {@link AlertDialog} as fallback
     *
     * @param missingPermissions list of missing permissions with the message to show
     */
    private void askRationalePermissions(final Pair<String[], String> missingPermissions) {
        if (mCoordinatorLayout != null) {
            Snackbar mSnackbar = Snackbar.make(mCoordinatorLayout, missingPermissions.second, Snackbar.LENGTH_INDEFINITE);
            TextView tv = mSnackbar.getView().findViewById(R.id.snackbar_text);
            tv.setTextColor(Color.WHITE);
            mSnackbar.setAction(
                    android.R.string.ok,
                    new View.OnClickListener() {
                        @RequiresApi(api = Build.VERSION_CODES.M)
                        @Override
                        public void onClick(View view) {
                            requestPermissions(missingPermissions.first, mPermissionCode);
                        }
                    });
            mSnackbar.setActionTextColor(ContextCompat.getColor(getContext(), android.R.color.white));
            mSnackbar.show();
        } else {
            new AlertDialog.Builder(getContext())
                    .setMessage(missingPermissions.second)
                    .setPositiveButton(
                            android.R.string.ok,
                            new DialogInterface.OnClickListener() {
                                @RequiresApi(api = Build.VERSION_CODES.M)
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    requestPermissions(missingPermissions.first, mPermissionCode);
                                }
                            })
                    .setNegativeButton(
                            android.R.string.cancel,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            })
                    .show();
        }
    }

    private String getTempFilename() {
        return "temp_" + mFilename;
    }

    /**
     * If the user has given all the necessary permissions opens the picker
     * If not asks the permissions
     */
    public void openPicker() {
        Pair<String[], String> missingPermissions = CameraUtils.getMissingPermissions(getActivity());

        if (missingPermissions.first.length == 0) {
            startIntentChooser();
        } else {
            askPermissions(missingPermissions);
        }
    }

    /**
     * Shows the application chooser to the user
     */
    private void startIntentChooser() {
        try {
            mOutputFileUri = getOutputUri();
            showSelector(CameraUtils.getIntentChooser(getContext(), mOutputFileUri, mShowGallery), mRequestCode);
        } catch (IOException ex) {
            Log.e(TAG, "photo picker", ex);
            if (mOnResultListener != null) {
                mOnResultListener.onPickPhotoFailure(ex);
            }
        }
    }

    private Context getContext() {
        return getActivity();
    }

    @TargetApi(Build.VERSION_CODES.M)
    protected abstract void requestPermissions(String[] permissions, int requestCode);

    protected abstract void showSelector(Intent selectorIntent, int requestCode);

    protected abstract Activity getActivity();

    class ProcessResultTask extends AsyncTask<Uri, Void, PickerResult> {
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
            return ImageUtils.decodeAndResizeImageUri(getContext(), aSourceUri, mPictureSize);
        }

        private File resultFile(Bitmap aBitmap) throws IOException {
            FileOutputStream out = null;
            File vDestFile = FileUtils.createPictureFile(getContext(), mFilename);
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
