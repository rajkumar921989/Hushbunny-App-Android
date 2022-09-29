package com.hushbunny.app.ui.service

import com.hushbunny.app.R
import com.hushbunny.app.providers.ResourceProvider
import com.hushbunny.app.ui.model.*
import com.hushbunny.app.ui.network.NetworkCallHandler
import com.hushbunny.app.ui.onboarding.model.BaseResponse
import com.hushbunny.app.uitls.APIConstants
import javax.inject.Inject

class MomentServiceImpl @Inject constructor(
    private val resourceProvider: ResourceProvider,
    private val networkCallHandler: NetworkCallHandler
) : MomentService {

    override suspend fun getKidsList(queryParams: Map<String, Any>): KidsListResponseModel {
        return networkCallHandler.getData(
            baseUrl = resourceProvider.getString(R.string.env_base_url),
            endPoint = "${resourceProvider.getString(R.string.env_kids_url)}${resourceProvider.getString(R.string.env_kids_list_url)}",
            headers = hashMapOf(
                Pair(APIConstants.AUTHORIZATION, APIConstants.getAuthorization()),
                Pair(APIConstants.ACCEPT_LANGUAGE, APIConstants.ENGLISH)
            ), queryParams = queryParams
        )
    }

    override suspend fun getMomentList(queryParams: Map<String, Any>): MomentResponseModel {
        return networkCallHandler.getData(
            baseUrl = resourceProvider.getString(R.string.env_base_url),
            endPoint = "${resourceProvider.getString(R.string.env_moment_url)}${resourceProvider.getString(R.string.env_kids_list_url)}",
            headers = hashMapOf(
                Pair(APIConstants.AUTHORIZATION, APIConstants.getAuthorization()),
                Pair(APIConstants.ACCEPT_LANGUAGE, APIConstants.ENGLISH)
            ), queryParams = queryParams
        )
    }

    override suspend fun getReactionList(queryParams: Map<String, Any>): ReactionResponseModel {
        return networkCallHandler.getData(
            baseUrl = resourceProvider.getString(R.string.env_base_url),
            endPoint = "${resourceProvider.getString(R.string.env_moment_url)}${resourceProvider.getString(R.string.env_list_of_reaction_url)}",
            headers = hashMapOf(
                Pair(APIConstants.AUTHORIZATION, APIConstants.getAuthorization()),
                Pair(APIConstants.ACCEPT_LANGUAGE, APIConstants.ENGLISH)
            ),
            queryParams = queryParams
        )
    }

    override suspend fun getCommentList(queryParams: Map<String, Any>): CommentResponseModel {
        return networkCallHandler.getData(
            baseUrl = resourceProvider.getString(R.string.env_base_url),
            endPoint = "${resourceProvider.getString(R.string.env_moment_url)}${resourceProvider.getString(R.string.env_list_comment_url)}",
            headers = hashMapOf(
                Pair(APIConstants.AUTHORIZATION, APIConstants.getAuthorization()),
                Pair(APIConstants.ACCEPT_LANGUAGE, APIConstants.ENGLISH)
            ), queryParams = queryParams
        )
    }

    override suspend fun postBookmark(addBookmarkRequest: AddBookmarkRequest): BookMarkResponse {
        return networkCallHandler.postDataHandler(
            baseUrl = resourceProvider.getString(R.string.env_base_url),
            endPoint = "${resourceProvider.getString(R.string.env_moment_url)}${resourceProvider.getString(R.string.env_bookmark_url)}",
            headers = hashMapOf(
                Pair(APIConstants.AUTHORIZATION, APIConstants.getAuthorization()),
                Pair(APIConstants.ACCEPT_LANGUAGE, APIConstants.ENGLISH)
            ), requestBody = addBookmarkRequest
        )
    }

    override suspend fun postComment(addNewCommentRequest: AddNewCommentRequest): BaseResponse {
        return networkCallHandler.postDataHandler(
            baseUrl = resourceProvider.getString(R.string.env_base_url),
            endPoint = "${resourceProvider.getString(R.string.env_moment_url)}${resourceProvider.getString(R.string.env_comment_url)}",
            headers = hashMapOf(
                Pair(APIConstants.AUTHORIZATION, APIConstants.getAuthorization()),
                Pair(APIConstants.ACCEPT_LANGUAGE, APIConstants.ENGLISH)
            ), requestBody = addNewCommentRequest
        )
    }

    override suspend fun postReaction(addReactionRequest: AddReactionRequest): BookMarkResponse {
        return networkCallHandler.postDataHandler(
            baseUrl = resourceProvider.getString(R.string.env_base_url),
            endPoint = "${resourceProvider.getString(R.string.env_moment_url)}${resourceProvider.getString(R.string.env_reaction_url)}",
            headers = hashMapOf(
                Pair(APIConstants.AUTHORIZATION, APIConstants.getAuthorization()),
                Pair(APIConstants.ACCEPT_LANGUAGE, APIConstants.ENGLISH)
            ), requestBody = addReactionRequest
        )
    }

    override suspend fun addOREditKid(isEditKid: Boolean, addKidRequest: AddKidRequest): AddKidsResponseModel {
        return if (isEditKid) {
            networkCallHandler.putDataHandler(
                baseUrl = resourceProvider.getString(R.string.env_base_url),
                endPoint = "${resourceProvider.getString(R.string.env_kids_url)}${resourceProvider.getString(R.string.env_edit_kid_url)}",
                headers = hashMapOf(
                    Pair(APIConstants.AUTHORIZATION, APIConstants.getAuthorization()),
                    Pair(APIConstants.ACCEPT_LANGUAGE, APIConstants.ENGLISH)
                ),
                requestBody = addKidRequest
            )
        } else {
            networkCallHandler.postDataHandler(
                baseUrl = resourceProvider.getString(R.string.env_base_url),
                endPoint = "${resourceProvider.getString(R.string.env_kids_url)}${resourceProvider.getString(R.string.env_add_kid_url)}",
                headers = hashMapOf(
                    Pair(APIConstants.AUTHORIZATION, APIConstants.getAuthorization()),
                    Pair(APIConstants.ACCEPT_LANGUAGE, APIConstants.ENGLISH)
                ),
                requestBody = addKidRequest
            )
        }
    }

    override suspend fun addOREditMoment(isEdit: Boolean, addMomentRequest: AddMomentRequest): BaseResponse {
        return if (isEdit) {
            networkCallHandler.putDataHandler(
                baseUrl = resourceProvider.getString(R.string.env_base_url),
                endPoint = "${resourceProvider.getString(R.string.env_moment_url)}${resourceProvider.getString(R.string.env_edit_kid_url)}",
                headers = hashMapOf(
                    Pair(APIConstants.AUTHORIZATION, APIConstants.getAuthorization()),
                    Pair(APIConstants.ACCEPT_LANGUAGE, APIConstants.ENGLISH)
                ),
                requestBody = addMomentRequest
            )
        } else {
            networkCallHandler.postDataHandler(
                baseUrl = resourceProvider.getString(R.string.env_base_url),
                endPoint = "${resourceProvider.getString(R.string.env_moment_url)}${resourceProvider.getString(R.string.env_add_kid_url)}",
                headers = hashMapOf(
                    Pair(APIConstants.AUTHORIZATION, APIConstants.getAuthorization()),
                    Pair(APIConstants.ACCEPT_LANGUAGE, APIConstants.ENGLISH)
                ),
                requestBody = addMomentRequest
            )
        }
    }

    override suspend fun getUserDetail(queryParams: Map<String, Any>): UserDetailResponse {
        return networkCallHandler.getData(
            baseUrl = resourceProvider.getString(R.string.env_base_url),
            endPoint = "${resourceProvider.getString(R.string.env_user_url)}${resourceProvider.getString(R.string.env_user_detail_url)}",
            headers = hashMapOf(
                Pair(APIConstants.AUTHORIZATION, APIConstants.getAuthorization()),
                Pair(APIConstants.ACCEPT_LANGUAGE, APIConstants.ENGLISH)
            ), queryParams = queryParams
        )
    }

    override suspend fun postReport(request: AddReportRequest): BaseResponse {
        return networkCallHandler.postDataHandler(
            baseUrl = resourceProvider.getString(R.string.env_base_url),
            endPoint = "${resourceProvider.getString(R.string.env_report_url)}${resourceProvider.getString(R.string.env_add_kid_url)}",
            headers = hashMapOf(
                Pair(APIConstants.AUTHORIZATION, APIConstants.getAuthorization()),
                Pair(APIConstants.ACCEPT_LANGUAGE, APIConstants.ENGLISH)
            ),
            requestBody = request
        )
    }

    override suspend fun getReportReasonList(queryParams: Map<String, Any>): ReportResponseModel {
        return networkCallHandler.getData(
            baseUrl = resourceProvider.getString(R.string.env_base_url),
            endPoint = "${resourceProvider.getString(R.string.env_report_url)}${resourceProvider.getString(R.string.env_report_reason_url)}",
            headers = hashMapOf(
                Pair(APIConstants.AUTHORIZATION, APIConstants.getAuthorization()),
                Pair(APIConstants.ACCEPT_LANGUAGE, APIConstants.ENGLISH)
            ), queryParams = queryParams
        )
    }

    override suspend fun markMomentAsImportant(momentId: String): BookMarkResponse {
        return networkCallHandler.patchData(
            baseUrl = resourceProvider.getString(R.string.env_base_url),
            endPoint = "${resourceProvider.getString(R.string.env_moment_url)}${resourceProvider.getString(R.string.env_important_moment_url)}$momentId",
            headers = hashMapOf(
                Pair(APIConstants.AUTHORIZATION, APIConstants.getAuthorization()),
                Pair(APIConstants.ACCEPT_LANGUAGE, APIConstants.ENGLISH)
            ), requestBody = ""
        )
    }

    override suspend fun deleteComment(commentId: String): BaseResponse {
        return networkCallHandler.deleteDataHandler(
            baseUrl = resourceProvider.getString(R.string.env_base_url),
            endPoint = "${resourceProvider.getString(R.string.env_moment_url)}${resourceProvider.getString(R.string.env_delete_comment_url)}$commentId",
            headers = hashMapOf(
                Pair(APIConstants.AUTHORIZATION, APIConstants.getAuthorization()),
                Pair(APIConstants.ACCEPT_LANGUAGE, APIConstants.ENGLISH)
            ), queryParams = hashMapOf()
        )
    }

    override suspend fun getMomentDetail(queryParams: Map<String, Any>): MomentDetailResponseModel {
        return networkCallHandler.getData(
            baseUrl = resourceProvider.getString(R.string.env_base_url),
            endPoint = "${resourceProvider.getString(R.string.env_moment_url)}${resourceProvider.getString(R.string.env_moment_detail_url)}",
            headers = hashMapOf(
                Pair(APIConstants.AUTHORIZATION, APIConstants.getAuthorization()),
                Pair(APIConstants.ACCEPT_LANGUAGE, APIConstants.ENGLISH)
            ), queryParams = queryParams
        )
    }

    override suspend fun getKidDetailByKidId(queryParams: Map<String, Any>): KidByIdResponseModel {
        return networkCallHandler.getData(
            baseUrl = resourceProvider.getString(R.string.env_base_url),
            endPoint = "${resourceProvider.getString(R.string.env_kids_url)}${resourceProvider.getString(R.string.env_kid_by_id_url)}",
            headers = hashMapOf(
                Pair(APIConstants.AUTHORIZATION, APIConstants.getAuthorization()),
                Pair(APIConstants.ACCEPT_LANGUAGE, APIConstants.ENGLISH)
            ), queryParams = queryParams
        )
    }

    override suspend fun shareMoment(shareMomentRequest: ShareMomentRequest): BaseResponse {
        return networkCallHandler.putDataHandler(
            baseUrl = resourceProvider.getString(R.string.env_base_url),
            endPoint = "${resourceProvider.getString(R.string.env_moment_url)}${resourceProvider.getString(R.string.env_share_url)}",
            headers = hashMapOf(
                Pair(APIConstants.AUTHORIZATION, APIConstants.getAuthorization()),
                Pair(APIConstants.ACCEPT_LANGUAGE, APIConstants.ENGLISH)
            ),
            requestBody = shareMomentRequest
        )
    }


}