package xyz.elzspikes.easypic

/**
 * Created by Luca Rossi on 24/05/2018.
 */
interface  OnPickResultListener {
     fun onPicPickSuccess(result: PickerResult)

    fun onPicPickFailure(exception: Exception)
}