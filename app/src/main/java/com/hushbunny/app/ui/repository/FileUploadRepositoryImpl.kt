package com.hushbunny.app.ui.repository

import com.hushbunny.app.ui.model.FileUploadResponse
import com.hushbunny.app.ui.service.FileUploadService
import java.io.File
import javax.inject.Inject

class FileUploadRepositoryImpl @Inject constructor(private val fileUploadService: FileUploadService) : FileUploadRepository {

    override suspend fun uploadFile(filePath: File): FileUploadResponse {
        return fileUploadService.uploadFile(filePath)
    }
}