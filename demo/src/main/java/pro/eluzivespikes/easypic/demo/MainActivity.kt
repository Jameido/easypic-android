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

class MainActivity : AppCompatActivity() {

    private val onPickSuccess: (result: PickerResult) -> Unit = { result ->
        result.bitmap?.let { bitmap -> findViewById<ImageView>(R.id.image_result_bitmap).setImageBitmap(bitmap) }
        result.bytes?.let { bytes -> findViewById<ImageView>(R.id.image_result_bytes).setImageBitmap(BitmapFactory.decodeByteArray(bytes, 0, bytes.size)) }
        result.file?.let { file -> findViewById<ImageView>(R.id.image_result_file).setImageURI(Uri.fromFile(file)) }
    }

    private val onPickFailure: (exception: Exception) -> Unit = { exception ->
        Toast.makeText(this, exception.message, Toast.LENGTH_LONG).show()
    }

    private var mPicPicker: PicPicker = PicPickerBuilder(this)
            .showGallery()
            .withModes(
                    PicPicker.BITMAP,
                    PicPicker.BYTES,
                    PicPicker.FILE
            )
            .withPictureSize(400)
            .withSuccessListener(onPickSuccess)
            .withFailureListener(onPickFailure)
            .withScaleType(PicPicker.CROP)
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

}
