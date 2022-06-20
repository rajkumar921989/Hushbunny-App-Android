package com.hushbunny.app.ui.repository

import com.hushbunny.app.ui.model.FileUploadResponse
import java.io.File

interface FileUploadRepository {
    suspend fun uploadFile(filePath:File) : FileUploadResponse
}