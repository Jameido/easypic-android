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
import android.os.Build;

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
class ActivityEasyPhotoPicker extends EasyPhotoPickerImpl {

    private Activity mActivity;

    ActivityEasyPhotoPicker(Activity activity) {
        mActivity = activity;
    }

    @TargetApi(Build.VERSION_CODES.M)
    protected void requestPermissions(String[] permissions, int requestCode) {
        getActivity().requestPermissions(permissions, requestCode);
    }

    @Override
    protected void showSelector(Intent selectorIntent, int requestCode) {
        getActivity().startActivityForResult(selectorIntent, requestCode);
    }

    @Override
    protected Activity getActivity() {
        return mActivity;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mActivity = null;
    }
}
