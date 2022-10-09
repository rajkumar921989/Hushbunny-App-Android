package com.hushbunny.app.ui.sealedclass

import android.graphics.Bitmap

sealed class FileDownLoadState {
    data class Success(val bitmap: Bitmap, val imageText: String) : FileDownLoadState()
    object Error : FileDownLoadState()
    object Loading: FileDownLoadState()
}
