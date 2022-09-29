package com.hushbunny.app.ui.service

import com.hushbunny.app.R
import com.hushbunny.app.providers.ResourceProvider
import com.hushbunny.app.ui.model.FileUploadResponse
import com.hushbunny.app.ui.model.MomentFileUploadResponse
import com.hushbunny.app.ui.network.NetworkCallHandler
import com.hushbunny.app.uitls.APIConstants
import com.hushbunny.app.uitls.AppConstants
import java.io.File
import javax.inject.Inject

class FileUploadServiceImpl @Inject constructor(
    private val resourceProvider: ResourceProvider,
    private val networkCallHandler: NetworkCallHandler
) : FileUploadService {

    override suspend fun uploadFile(filePath: File): FileUploadResponse {
        return networkCallHandler.uploadFileDataHandler(
            baseUrl = resourceProvider.getString(R.string.env_base_url),
            endPoint = "${resourceProvider.getString(R.string.env_upload_url)}${resourceProvider.getString(R.string.env_file_upload_url)}",
            headers = hashMapOf(Pair(APIConstants.ACCEPT_LANGUAGE, APIConstants.ENGLISH)),
            filePath = listOf(filePath)
        )
    }

    override suspend fun uploadFileForMoment(filePath: List<File>): MomentFileUploadResponse {
        return networkCallHandler.uploadFileDataHandler(
            baseUrl = resourceProvider.getString(R.string.env_base_url),
            endPoint = "${resourceProvider.getString(R.string.env_upload_url)}${resourceProvider.getString(R.string.env_upload_moment_file_url)}",
            headers = hashMapOf(
                Pair(APIConstants.AUTHORIZATION, APIConstants.getAuthorization()),
                Pair(APIConstants.ACCEPT_LANGUAGE, APIConstants.ENGLISH)
            ),
            filePath = filePath
        )
    }

}