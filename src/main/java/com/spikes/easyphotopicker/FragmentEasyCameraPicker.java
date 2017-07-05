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

import android.support.v4.app.Fragment;
import android.widget.Toast;

import com.spikes.easyphotopicker.utils.CameraUtils;

import java.io.IOException;

/**
 * Created by Luca Rossi on 05/07/2017.
 * <p>
 * Extends {@link ActivityEasyCameraPicker} and uses a fragment instead of the activity
 * </p>
 */

public class FragmentEasyCameraPicker extends ActivityEasyCameraPicker {

    private Fragment mFragment;

    /**
     *
     * @param fragment the fragment that calls the {@link ActivityEasyCameraPicker}
     * @param provider the project file provider
     */
    public FragmentEasyCameraPicker(Fragment fragment, String provider, boolean showGallery) {
        super(fragment.getActivity(), provider, showGallery);
        mFragment = fragment;
    }


    @Override
    protected void requestPermissions(String[] permissions) {
        mFragment.requestPermissions(permissions, mPermissionCode);
    }

    @Override
    protected void startIntentChooser() {
        try {
            mOutputFileUri = CameraUtils.startIntentChooser(mActivity, mFragment, mProvider, getTempFilename(), mRequestCode, mShowGallery);
        } catch (IOException ex) {
            Toast.makeText(mActivity, R.string.error_creating_file, Toast.LENGTH_SHORT).show();
        }
    }
}
