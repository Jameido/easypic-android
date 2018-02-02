package pro.eluzivespikes.easyphotopicker.demo

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import pro.eluzivespikes.easyphotopicker.ActivityEasyPhotoPicker
import pro.eluzivespikes.easyphotopicker.PickerResult
import java.lang.Exception
import java.util.ArrayList

class MainActivity : AppCompatActivity(), ActivityEasyPhotoPicker.OnResultListener {

    private lateinit var mEasyPhotoPicker: ActivityEasyPhotoPicker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<View>(R.id.button_take_picture)
                .setOnClickListener { mEasyPhotoPicker.openPicker("easyphotopickerdemo") }

        mEasyPhotoPicker = ActivityEasyPhotoPicker.Builder(this, "pro.eluzivespikes.easyphotopicker.demo.fileprovider")
                .withModes(
                        ActivityEasyPhotoPicker.PickerMode.BITMAP,
                        ActivityEasyPhotoPicker.PickerMode.BYTES,
                        ActivityEasyPhotoPicker.PickerMode.FILE
                )
                .withPictureSize(400
                )
                .withResultListener(this)
                .build()
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
        findViewById<ImageView>(R.id.image_result_bitmap).setImageBitmap(pickerResult.bitmap)
        findViewById<ImageView>(R.id.image_result_bytes).setImageBitmap(BitmapFactory.decodeByteArray(pickerResult.bytes, 0, pickerResult.bytes.size))
        findViewById<ImageView>(R.id.image_result_file).setImageURI(Uri.fromFile(pickerResult.file))
    }

    override fun onPickPhotoFailure(exception: Exception) {
        Toast.makeText(this, exception.message, Toast.LENGTH_LONG).show()
    }


}
