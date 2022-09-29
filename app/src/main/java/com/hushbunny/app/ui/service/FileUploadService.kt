package com.hushbunny.app.ui.service

import com.hushbunny.app.ui.model.FileUploadResponse
import com.hushbunny.app.ui.model.MomentFileUploadResponse
import java.io.File

interface FileUploadService {
    suspend fun uploadFile(filePath: File) : FileUploadResponse
    suspend fun uploadFileForMoment(filePath: List<File>) : MomentFileUploadResponse
}