# Easy Pic

An Android library that makes taking pictures from camera or gallery easy as taking a glass of water.

## Installation

Add the following dependency to build.gradle:
```gradle
dependencies {
    compile 'TBD'
}
```

Then include the node below in your app manifest:
```xml
<application
     ...>
    <provider
        android:name="xyz.elzspikes.easypic.PicturesProvider"
        android:authorities="${applicationId}.easypicprovider"
        android:exported="false"
        android:grantUriPermissions="true"
        tools:replace="android:authorities">
        <meta-data
            android:name="android.support.FILE_PROVIDER_PATHS"
            android:resource="@xml/pictures_paths" />   <!--picture_paths.xml is located inside easypic -->
    </provider>
    ...
</application>
```

## Usage

Build a `PickPicker` instance using `PicPickerBuilder`:

```kotlin
private var mPicPicker: PicPicker = PicPickerBuilder(this)
            .showGallery()                      //Show also gallery applications in the selector
            .withModes(                         //Return the taken picture as:
                    PicPicker.BITMAP,           //Bitmap
                    PicPicker.BYTES,            //Array of bytes
                    PicPicker.FILE              //File
            )                                   //Kinda self explainatory
            .withPictureSize(400)               //Desired picture size, 0 if no resize is wanted
            .withScaleType(PicPicker.CROP)      //How the picture will be scaled to the value above
            .withSuccessListener(onPickSuccess) //See below
            .withFailureListener(onPickFailure) //See below
            .build()

```

To open the app selector call:
```kotlin
mPicPicker.openPicker()
```

Pick picker handles itself requesting permissions, just add:
```kotlin
override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    mPicPicker.onRequestPermissionsResult(requestCode, permissions, grantResults)
}

```

Pass the data got from the camera/gallery app to Pick picker with:

```kotlin
override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    mPicPicker.onActivityResult(requestCode, resultCode, data)
}
```

Don't forget to call to avoid memory leaks:

```kotlin
override fun onDestroy() {
    super.onDestroy()
    mPicPicker.onDestroy()
}
```

Finally you want to get your images no?  
There are 3 ways to get the images: as Bitmap, array of bytes or File. If you want you can get all of 3, but that's a bit overkill don't you think?  
To get the image and the error pass the handlers to the builder as shown above.

```kotlin
private val onPickSuccess: (result: PickerResult) -> Unit = { result ->
    //Access Picker result props and show the image
}

private val onPickFailure: (exception: Exception) -> Unit = { exception ->
    //Ops!
}
```

Depending on the chosen mode/s the result properties will be initialized.

## License
[MIT](https://github.com/Jameido/easypic-android/blob/dev/LICENSE)

Well maybe is not easy as I said before 😇
