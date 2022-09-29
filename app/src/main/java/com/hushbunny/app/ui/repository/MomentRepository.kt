package com.hushbunny.app.ui.repository

import com.hushbunny.app.ui.model.*
import com.hushbunny.app.ui.onboarding.model.BaseResponse
import com.hushbunny.app.ui.sealedclass.*

interface MomentRepository {
    suspend fun getKidsList(isOtherUser: Boolean = false, queryParams: Map<String, Any>): KidsStatusInfo
    suspend fun getMomentList(queryParams: Map<String, Any>): MomentResponseInfo
    suspend fun getReactionList(queryParams: Map<String, Any>): ReactionResponseInfo
    suspend fun getCommentList(type: String = "Comment List", queryParams: Map<String, Any>): CommentResponseInfo
    suspend fun postBookmark(position: Int, addBookmarkRequest: AddBookmarkRequest): BookMarkResponseInfo
    suspend fun postComment(addNewCommentRequest: AddNewCommentRequest): CommentResponseInfo
    suspend fun postReaction(position: Int, addReactionRequest: AddReactionRequest): BookMarkResponseInfo
    suspend fun addOREditKid(isEditKid: Boolean, addKidRequest: AddKidRequest): AddKidsResponseModel
    suspend fun addOREditMoment(isEdit: Boolean, addMomentRequest: AddMomentRequest): BaseResponse
    suspend fun getUserDetail(queryParams: Map<String, Any>): UserDetailResponse
    suspend fun postReport(request: AddReportRequest): BaseResponse
    suspend fun getReportReasonList(queryParams: Map<String, Any>): ReportReasonInfo
    suspend fun markMomentAsImportant(position: Int, momentId: String): BookMarkResponseInfo
    suspend fun deleteComment(position: Int, commentId: String): CommentDeletedResponseInfo
    suspend fun getMomentDetail(queryParams: Map<String, Any>): MomentDetailResponseModel
    suspend fun getKidDetailByKidId(queryParams: Map<String, Any>): KidByIdResponseModel
    suspend fun shareMoment(shareMomentRequest: ShareMomentRequest): BaseResponse
}