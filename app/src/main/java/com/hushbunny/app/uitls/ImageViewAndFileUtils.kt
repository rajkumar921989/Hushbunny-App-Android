package com.hushbunny.app.uitls

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.hushbunny.app.ui.model.AddMomentImageUrl
import com.hushbunny.app.ui.model.FileRequest
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody


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

    fun createFileRequest(fileRequestList: List<FileRequest>, gson: Gson): RequestBody {
        val multipartBody = MultipartBody.Builder().setType(MultipartBody.FORM)
        val ogFileUrlsList = arrayListOf<AddMomentImageUrl>()
        for (fileRequest in fileRequestList) {
            val file = fileRequest.file
            multipartBody.addFormDataPart("file", file.name, file.asRequestBody("application/octet-stream".toMediaTypeOrNull()))
            fileRequest.fileWebUrl?.let {
                val imageUrl = AddMomentImageUrl(
                    fileName = file.name,
                    link = it
                )
                ogFileUrlsList.add(imageUrl)
            }
        }
        if(ogFileUrlsList.isNotEmpty()) {
            multipartBody.addFormDataPart("ogurls", gson.toJson(ogFileUrlsList))
        }
        return multipartBody.build()
    }

    fun View.hideKeyboard() {
        (context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?)?.apply {
            hideSoftInputFromWindow(windowToken, 0)
        }
    }

}