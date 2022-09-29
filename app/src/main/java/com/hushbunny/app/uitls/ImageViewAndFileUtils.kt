package com.hushbunny.app.uitls

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import com.bumptech.glide.Glide
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File


object ImageViewAndFileUtils {
    fun ImageView.loadImageFromURL(imageUrl: String, isVideo: Boolean = false, isLocal: Boolean = false) {
        val imagePath =
            if(isLocal) "file:///$imageUrl"
            else if (isVideo && !imageUrl.contains(APIConstants.VIDEO_BASE_URL)) "${APIConstants.VIDEO_BASE_URL}$imageUrl"
            else if (!imageUrl.contains(APIConstants.IMAGE_BASE_URL)) "${APIConstants.IMAGE_BASE_URL}$imageUrl"
            else imageUrl
        Glide.with(this)
            .load(imagePath)
            .into(this)
    }

    fun ImageView.loadCircleImageFromURL(imageUrl: String) {
        val imagePath = if (!imageUrl.contains(APIConstants.IMAGE_BASE_URL)) "${APIConstants.IMAGE_BASE_URL}$imageUrl"
        else imageUrl
        Glide.with(this)
            .load(imagePath)
            .circleCrop()
            .into(this)
    }

    fun ImageView.loadLocalImage(image: Drawable?) {
        Glide.with(this)
            .load(image)
            .into(this)
    }

    fun createFileRequest(filePath: List<File>): RequestBody {
        val multipartBody = MultipartBody.Builder().setType(MultipartBody.FORM)
        for (file in filePath) {
            multipartBody.addFormDataPart("file", file.name, file.asRequestBody("application/octet-stream".toMediaTypeOrNull()))
        }
        return multipartBody.build()
    }

    fun View.hideKeyboard() {
        (context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?)?.apply {
            hideSoftInputFromWindow(windowToken, 0)
        }
    }
}