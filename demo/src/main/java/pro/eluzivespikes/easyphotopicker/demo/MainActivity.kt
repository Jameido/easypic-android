package pro.eluzivespikes.easyphotopicker.demo

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import pro.eluzivespikes.easyphotopicker.EasyPhotoPicker
import pro.eluzivespikes.easyphotopicker.EasyPhotoPickerBuilder
import pro.eluzivespikes.easyphotopicker.PickerResult
import java.lang.Exception

class MainActivity : AppCompatActivity(), EasyPhotoPicker.OnResultListener {

    private lateinit var mEasyPhotoPicker: EasyPhotoPicker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<View>(R.id.button_take_picture)
                .setOnClickListener { mEasyPhotoPicker.openPicker() }

        mEasyPhotoPicker = EasyPhotoPickerBuilder(this, "pro.eluzivespikes.easyphotopicker.demo.fileprovider")
                .withModes(
                        EasyPhotoPicker.PickerMode.BITMAP,
                        EasyPhotoPicker.PickerMode.BYTES,
                        EasyPhotoPicker.PickerMode.FILE
                )
                .withPictureSize(400)
                .withResultListener(this)
                .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        mEasyPhotoPicker.onDestroy()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        mEasyPhotoPicker.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        mEasyPhotoPicker.onActivityResult(requestCode, resultCode, data)
    }

    override fun onPickPhotoSuccess(pickerResult: PickerResult) {
        pickerResult.bitmap?.let { bitmap -> findViewById<ImageView>(R.id.image_result_bitmap).setImageBitmap(bitmap) }
        pickerResult.bytes?.let { bytes -> findViewById<ImageView>(R.id.image_result_bytes).setImageBitmap(BitmapFactory.decodeByteArray(bytes, 0, bytes.size)) }
        pickerResult.file?.let { file -> findViewById<ImageView>(R.id.image_result_file).setImageURI(Uri.fromFile(file)) }
    }

    override fun onPickPhotoFailure(exception: Exception) {
        Toast.makeText(this, exception.message, Toast.LENGTH_LONG).show()
    }
}
