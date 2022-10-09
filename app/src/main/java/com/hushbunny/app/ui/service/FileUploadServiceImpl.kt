package com.hushbunny.app.ui.service

import com.hushbunny.app.R
import com.hushbunny.app.providers.ResourceProvider
import com.hushbunny.app.ui.model.FileRequest
import com.hushbunny.app.ui.model.FileUploadResponse
import com.hushbunny.app.ui.model.MomentFileUploadResponse
import com.hushbunny.app.ui.network.NetworkCallHandler
import com.hushbunny.app.uitls.APIConstants
import java.io.File
import javax.inject.Inject

class FileUploadServiceImpl @Inject constructor(
    private val resourceProvider: ResourceProvider,
    private val networkCallHandler: NetworkCallHandler
) : FileUploadService {

    override suspend fun uploadFile(filePath: File): FileUploadResponse {
        val filesList = FileRequest(file = filePath)
        return networkCallHandler.uploadFileDataHandler(
            baseUrl = resourceProvider.getString(R.string.env_base_url),
            endPoint = "${resourceProvider.getString(R.string.env_upload_url)}${resourceProvider.getString(R.string.env_file_upload_url)}",
            headers = hashMapOf(Pair(APIConstants.ACCEPT_LANGUAGE, APIConstants.ENGLISH)),
            fileRequestList = listOf(filesList)
        )
    }

    override suspend fun uploadFileForMoment(fileRequestList: List<FileRequest>): MomentFileUploadResponse {
        return networkCallHandler.uploadFileDataHandler(
            baseUrl = resourceProvider.getString(R.string.env_base_url),
            endPoint = "${resourceProvider.getString(R.string.env_upload_url)}${resourceProvider.getString(R.string.env_upload_moment_file_url)}",
            headers = hashMapOf(
                Pair(APIConstants.AUTHORIZATION, APIConstants.getAuthorization()),
                Pair(APIConstants.ACCEPT_LANGUAGE, APIConstants.ENGLISH)
            ),
            fileRequestList = fileRequestList
        )
    }

}