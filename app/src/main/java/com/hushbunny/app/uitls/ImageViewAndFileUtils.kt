package com.hushbunny.app.uitls

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import com.bumptech.glide.Glide
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

object ImageViewAndFileUtils {
    fun ImageView.loadImageFromURL(imageUrl: String) {
        Glide.with(this)
            .load(imageUrl)
            .into(this)
    }

    fun ImageView.loadLocalImage(image: Drawable?) {
        Glide.with(this)
            .load(image)
            .into(this)
    }

    fun createFileRequest(filePath: File): RequestBody {
        return MultipartBody.Builder().setType(MultipartBody.FORM)
            .addFormDataPart("file", filePath.name, filePath.asRequestBody("application/octet-stream".toMediaTypeOrNull())).build()
    }

    fun View.hideKeyboard() {
        (context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?)?.apply {
            hideSoftInputFromWindow(windowToken, 0)
        }
    }
}