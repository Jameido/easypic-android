package pro.eluzivespikes.easypic.demo

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import pro.eluzivespikes.easypic.PicPicker
import pro.eluzivespikes.easypic.PicPickerBuilder
import pro.eluzivespikes.easypic.PickerResult
import java.lang.Exception

class MainActivity : AppCompatActivity(), PicPicker.OnResultListener {

    private var mPicPicker: PicPicker = PicPickerBuilder(this)
            .withGallery(true)
            .withModes(
                    PicPicker.PickerMode.BITMAP,
                    PicPicker.PickerMode.BYTES,
                    PicPicker.PickerMode.FILE
            )
            .withPictureSize(400)
            .withResultListener(this)
            .withScaleType(PicPicker.ScaleType.CROP)
            .build()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<View>(R.id.button_take_picture)
                .setOnClickListener { mPicPicker.openPicker() }
    }

    override fun onDestroy() {
        super.onDestroy()
        mPicPicker.onDestroy()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        mPicPicker.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        mPicPicker.onActivityResult(requestCode, resultCode, data)
    }

    override fun onPicPickSuccess(pickerResult: PickerResult) {
        pickerResult.bitmap?.let { bitmap -> findViewById<ImageView>(R.id.image_result_bitmap).setImageBitmap(bitmap) }
        pickerResult.bytes?.let { bytes -> findViewById<ImageView>(R.id.image_result_bytes).setImageBitmap(BitmapFactory.decodeByteArray(bytes, 0, bytes.size)) }
        pickerResult.file?.let { file -> findViewById<ImageView>(R.id.image_result_file).setImageURI(Uri.fromFile(file)) }
    }

    override fun onPicPickFailure(exception: Exception) {
        Toast.makeText(this, exception.message, Toast.LENGTH_LONG).show()
    }
}
