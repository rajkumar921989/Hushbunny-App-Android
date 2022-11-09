package com.hushbunny.app.ui.repository

import com.hushbunny.app.providers.ResourceProvider
import com.hushbunny.app.ui.model.*
import com.hushbunny.app.ui.onboarding.model.BaseResponse
import com.hushbunny.app.ui.sealedclass.*
import com.hushbunny.app.ui.service.MomentService
import com.hushbunny.app.uitls.APIConstants
import javax.inject.Inject

class MomentRepositoryImpl @Inject constructor(private val resourceProvider: ResourceProvider, private val momentService: MomentService) :
    MomentRepository {

    override suspend fun getKidsList(isOtherUser: Boolean, queryParams: Map<String, Any>): KidsStatusInfo {
        val responseModel = momentService.getKidsList(queryParams)
        val kidsList = arrayListOf<KidsResponseModel>()
        if (!isOtherUser)
            kidsList.add(addNewKid())
        return when (responseModel.statusCode) {
            APIConstants.API_RESPONSE_200 -> {
                if (responseModel.data?.listing.orEmpty().isNotEmpty()) {
                    kidsList.addAll(responseModel.data?.listing.orEmpty())
                }
                if (isOtherUser && kidsList.isEmpty())
                    KidsStatusInfo.NoKids
                else KidsStatusInfo.UserList(kidsList)
            }
            APIConstants.UNAUTHORIZED_CODE -> {
                KidsStatusInfo.ApiError
            }
            else -> {
                KidsStatusInfo.UserList(kidsList)
            }
        }
    }

    private fun addNewKid(): KidsResponseModel {
        return KidsResponseModel(type = APIConstants.ADD_KID, name = "Add kid")
    }

    override suspend fun getMomentList(queryParams: Map<String, Any>): MomentResponseInfo {
        val response = momentService.getMomentList(queryParams)
        return when (response.statusCode) {
            APIConstants.API_RESPONSE_200 -> {
                if (response.data?.listing.orEmpty().isNotEmpty()) {
                    MomentResponseInfo.MomentList(count = response.data?.count ?: "00", response.data?.listing.orEmpty())
                } else {
                    MomentResponseInfo.NoList
                }
            }
            APIConstants.UNAUTHORIZED_CODE -> {
                MomentResponseInfo.ApiError
            }
            else -> {
                MomentResponseInfo.NoList
            }
        }
    }

    override suspend fun getReactionList(queryParams: Map<String, Any>): ReactionResponseInfo {
        val response = momentService.getReactionList(queryParams)
        return when (response.statusCode) {
            APIConstants.API_RESPONSE_200 -> {
                if (response.data?.listing.orEmpty().isNotEmpty()) {
                    ReactionResponseInfo.HaveReactionList(
                        reactionCount = ReactionCountModel(
                            all = response.data?.headerCounts?.all ?: "0",
                            heart = response.data?.headerCounts?.heart ?: "0",
                            laugh = response.data?.headerCounts?.laugh ?: "0",
                            sad = response.data?.headerCounts?.sad ?: "0"
                        ), response.data?.listing.orEmpty()
                    )
                } else ReactionResponseInfo.NoReaction(
                    ReactionCountModel(
                        all = response.data?.headerCounts?.all ?: "0",
                        heart = response.data?.headerCounts?.heart ?: "0",
                        laugh = response.data?.headerCounts?.laugh ?: "0",
                        sad = response.data?.headerCounts?.sad ?: "0"
                    )
                )
            }
            APIConstants.UNAUTHORIZED_CODE -> {
                ReactionResponseInfo.ApiError
            }
            else -> {
                ReactionResponseInfo.NoReaction(
                    ReactionCountModel(
                        all = response.data?.headerCounts?.all ?: "0",
                        heart = response.data?.headerCounts?.heart ?: "0",
                        laugh = response.data?.headerCounts?.laugh ?: "0",
                        sad = response.data?.headerCounts?.sad ?: "0"
                    )
                )
            }
        }
    }

    override suspend fun getCommentList(type: String, queryParams: Map<String, Any>): CommentResponseInfo {
        val response = momentService.getCommentList(queryParams)
        return when (response.statusCode) {
            APIConstants.API_RESPONSE_200 -> {
                if (response.data?.listing.orEmpty().isNotEmpty()) {
                    CommentResponseInfo.HaveCommentList(type = type, response.data?.listing.orEmpty())
                } else CommentResponseInfo.NoComment
            }
            APIConstants.UNAUTHORIZED_CODE -> {
                CommentResponseInfo.ApiError
            }
            else -> {
                CommentResponseInfo.NoComment
            }
        }
    }

    override suspend fun postBookmark(position: Int, addBookmarkRequest: AddBookmarkRequest): BookMarkResponseInfo {
        val response = momentService.postBookmark(addBookmarkRequest)
        return when (response.statusCode) {
            APIConstants.API_RESPONSE_200 -> {
                BookMarkResponseInfo.BookMarkSuccess(position, response.data)
            }
            APIConstants.UNAUTHORIZED_CODE -> {
                BookMarkResponseInfo.ApiError
            }
            else -> {
                BookMarkResponseInfo.BookMarkFailure(response.message)
            }
        }
    }

    override suspend fun postComment(addNewCommentRequest: AddNewCommentRequest): CommentResponseInfo {
        val response = momentService.postComment(addNewCommentRequest)
        return when (response.statusCode) {
            APIConstants.API_RESPONSE_200 -> {
                val queryParams = hashMapOf<String, Any>()
                queryParams[APIConstants.QUERY_PARAMS_PAGE] = 1
                queryParams[APIConstants.QUERY_PARAMS_PER_PAGE] = APIConstants.QUERY_PARAMS_PER_PAGE_VALUE
                queryParams[APIConstants.QUERY_PARAMS_MOMENT_ID] = addNewCommentRequest.momentId
                getCommentList(queryParams = queryParams, type = "Post Comment")
            }
            APIConstants.UNAUTHORIZED_CODE -> {
                CommentResponseInfo.ApiError
            }
            else -> {
                CommentResponseInfo.HaveError(response.message.orEmpty())
            }
        }
    }

    override suspend fun postReaction(position: Int, addReactionRequest: AddReactionRequest): BookMarkResponseInfo {
        val response = momentService.postReaction(addReactionRequest)
        return when (response.statusCode) {
            APIConstants.API_RESPONSE_200 -> {
                BookMarkResponseInfo.BookMarkSuccess(position, response.data)
            }
            APIConstants.UNAUTHORIZED_CODE -> {
                BookMarkResponseInfo.ApiError
            }
            else -> {
                BookMarkResponseInfo.BookMarkFailure(response.message)
            }
        }
    }

    override suspend fun addOREditKid(isEditKid: Boolean, addKidRequest: AddKidRequest): AddKidsResponseModel {
        return momentService.addOREditKid(isEditKid, addKidRequest)
    }

    override suspend fun addOREditMoment(isEdit: Boolean, addMomentRequest: AddMomentRequest): BaseResponse {
        return momentService.addOREditMoment(isEdit = isEdit, addMomentRequest = addMomentRequest)
    }

    override suspend fun getUserDetail(queryParams: Map<String, Any>): UserDetailResponse {
        return momentService.getUserDetail(queryParams)
    }

    override suspend fun postReport(request: AddReportRequest): BaseResponse {
        return momentService.postReport(request)
    }

    override suspend fun getReportReasonList(queryParams: Map<String, Any>): ReportReasonInfo {
        val response = momentService.getReportReasonList(queryParams)
        return when (response.statusCode) {
            APIConstants.API_RESPONSE_200 -> {
                if (response.data?.listing.orEmpty().isNotEmpty()) {
                    ReportReasonInfo.HaveList(response.data?.listing.orEmpty())
                } else ReportReasonInfo.NoList
            }
            APIConstants.UNAUTHORIZED_CODE -> {
                ReportReasonInfo.ApiError
            }
            else -> {
                ReportReasonInfo.Error(response.message)
            }
        }
    }

    override suspend fun markMomentAsImportant(position: Int, momentId: String): BookMarkResponseInfo {
        val response = momentService.markMomentAsImportant(momentId)
        return when (response.statusCode) {
            APIConstants.API_RESPONSE_200 -> {
                BookMarkResponseInfo.BookMarkSuccess(position, response.data)
            }
            APIConstants.UNAUTHORIZED_CODE -> {
                BookMarkResponseInfo.ApiError
            }
            else -> {
                BookMarkResponseInfo.BookMarkFailure(response.message)
            }
        }
    }

    override suspend fun deleteMoment(position: Int, momentId: String): MomentDeletedResponseInfo {
        val response = momentService.deleteMoment(momentId)
        return when (response.statusCode) {
            APIConstants.API_RESPONSE_200 -> {
                MomentDeletedResponseInfo.MomentDelete(position)
            }
            APIConstants.UNAUTHORIZED_CODE -> {
                MomentDeletedResponseInfo.ApiError
            }
            else -> {
                MomentDeletedResponseInfo.HaveError(response.message.orEmpty())
            }
        }
    }

    override suspend fun deleteComment(position: Int, commentId: String): CommentDeletedResponseInfo {
        val response = momentService.deleteComment(commentId)
        return when (response.statusCode) {
            APIConstants.API_RESPONSE_200 -> {
                CommentDeletedResponseInfo.CommentDelete(position)
            }
            APIConstants.UNAUTHORIZED_CODE -> {
                CommentDeletedResponseInfo.ApiError
            }
            else -> {
                CommentDeletedResponseInfo.HaveError(response.message.orEmpty())
            }
        }
    }

    override suspend fun getMomentDetail(queryParams: Map<String, Any>): MomentDetailResponseModel {
        return momentService.getMomentDetail(queryParams)
    }

    override suspend fun getKidDetailByKidId(queryParams: Map<String, Any>): KidByIdResponseModel {
        return momentService.getKidDetailByKidId(queryParams)
    }

    override suspend fun shareMoment(shareMomentRequest: ShareMomentRequest): BaseResponse {
        return momentService.shareMoment(shareMomentRequest)
    }
}