package com.hushbunny.app.ui.repository

import com.hushbunny.app.ui.model.FileRequest
import com.hushbunny.app.ui.model.FileUploadResponse
import com.hushbunny.app.ui.model.MomentFileUploadResponse
import java.io.File

interface FileUploadRepository {
    suspend fun uploadFile(filePath: File): FileUploadResponse
    suspend fun uploadFileForMoment(fileRequestList: List<FileRequest>): MomentFileUploadResponse
}