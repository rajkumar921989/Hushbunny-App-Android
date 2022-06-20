package com.hushbunny.app.ui.service

import com.hushbunny.app.ui.model.FileUploadResponse
import java.io.File

interface FileUploadService {
    suspend fun uploadFile(filePath: File) : FileUploadResponse
}