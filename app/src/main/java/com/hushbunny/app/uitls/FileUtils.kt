package com.hushbunny.app.uitls

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.OpenableColumns
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

class FileUtils {
    companion object {
        private const val FILE_NAME_FOR_MEDIA = "hushbunny"
        private const val FOLDER_NAME_FOR_MEDIA = "hushbunny_"
        private const val IMAGE_EXTENSION = ".jpg"
        private const val VIDEO_EXTENSION = ".mp4"
        private const val PROVIDER = "provider"
        const val INTENT_TYPE_IMAGE = "image/*"
        const val INTENT_TYPE_VIDEO = "video/*"

        @Throws(IOException::class)
        fun getImageFilePath(context: Context): File {
            return File.createTempFile("$FILE_NAME_FOR_MEDIA${System.currentTimeMillis()}", IMAGE_EXTENSION, getCacheFile(context))
        }

        @Throws(IOException::class)
        fun getVideoFilePath(context: Context): File {
            return File.createTempFile("$FILE_NAME_FOR_MEDIA${System.currentTimeMillis()}", VIDEO_EXTENSION, getCacheFile(context))
        }

        fun getAuthorities(context: Context): String {
            return context.packageName.plus(".").plus(PROVIDER)
        }

        private fun getCacheFile(context: Context): File? {
            val cacheDirectory = getCacheDirectory(context)
            if (cacheDirectory != null && !cacheDirectory.exists()) {
                cacheDirectory.mkdirs()
            }
            return cacheDirectory
        }

        private fun getCacheDirectory(context: Context): File? {
            return File(context.cacheDir, FOLDER_NAME_FOR_MEDIA)
        }

        @Synchronized
        fun deleteCacheDirectory(context: Context) {
            val storageDir: File? = getCacheDirectory(context)
            if (storageDir != null && storageDir.isDirectory) {
                storageDir.deleteRecursively()
            }
        }

        fun getImageThumbnail(context: Context, imagePath: Uri, reqWidth: Int, reqHeight: Int): Bitmap? {
            return BitmapFactory.Options().run {
                inJustDecodeBounds = true
                BitmapFactory.decodeStream(context.contentResolver.openInputStream(imagePath), null, this)
                // Calculate inSampleSize
                inSampleSize = getResizedBitmapInSample(this, reqWidth, reqHeight)
                // Decode bitmap with inSampleSize set
                inJustDecodeBounds = false
                BitmapFactory.decodeStream(context.contentResolver.openInputStream(imagePath), null, this)
            }
        }

        private fun getResizedBitmapInSample(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
            // Raw height and width of image
            val (height: Int, width: Int) = options.run { outHeight to outWidth }
            var inSampleSize = 1
            if (height > reqHeight || width > reqWidth) {
                val halfHeight: Int = height / 2
                val halfWidth: Int = width / 2
                // Calculate the largest inSampleSize value that is a power of 2 and keeps both
                // height and width larger than the requested height and width.
                while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                    inSampleSize *= 2
                }
            }
            return inSampleSize
        }

        private fun ContentResolver.getFileName(imagePath: Uri?): String {
            var fileName: String? = null
            val cursor = imagePath?.let { this.query(it, null, null, null, null) }
            if (cursor != null) {
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                cursor.moveToFirst()
                fileName = cursor.getString(nameIndex)
                cursor.close()
            }
            return fileName.orEmpty()
        }

        /** This method is to make a copy of the file that
        the user picks in the media gallery and add
        it to your application cache directory **/
        fun getImageFile(context: Context, imagePath: Uri?): File {
            var imageFile = File(context.cacheDir, context.contentResolver.getFileName(imagePath))
            val parcelFileDescriptor = imagePath?.let { context.contentResolver.openFileDescriptor(it, "r", null) }
            parcelFileDescriptor?.let {
                val inputStream = FileInputStream(parcelFileDescriptor.fileDescriptor)
                val outputStream = FileOutputStream(imageFile)
                inputStream.copyTo(outputStream)
            }
            return imageFile
        }
    }
}
