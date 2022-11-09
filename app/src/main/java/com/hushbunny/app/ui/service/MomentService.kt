package com.hushbunny.app.ui.service

import com.hushbunny.app.ui.model.*
import com.hushbunny.app.ui.onboarding.model.BaseResponse
import com.hushbunny.app.ui.onboarding.model.LoginResponse

interface MomentService {
    suspend fun getKidsList(queryParams: Map<String, Any>): KidsListResponseModel
    suspend fun getMomentList(queryParams: Map<String, Any>): MomentResponseModel
    suspend fun getReactionList(queryParams: Map<String, Any>): ReactionResponseModel
    suspend fun getCommentList(queryParams: Map<String, Any>): CommentResponseModel
    suspend fun postBookmark(addBookmarkRequest: AddBookmarkRequest): BookMarkResponse
    suspend fun postComment(addNewCommentRequest: AddNewCommentRequest): BaseResponse
    suspend fun postReaction(addReactionRequest: AddReactionRequest): BookMarkResponse
    suspend fun addOREditKid(isEditKid: Boolean, addKidRequest: AddKidRequest): AddKidsResponseModel
    suspend fun addOREditMoment(isEdit: Boolean, addMomentRequest: AddMomentRequest): BaseResponse
    suspend fun getUserDetail(queryParams: Map<String, Any>): UserDetailResponse
    suspend fun postReport(request: AddReportRequest): BaseResponse
    suspend fun getReportReasonList(queryParams: Map<String, Any>): ReportResponseModel
    suspend fun markMomentAsImportant(momentId: String): BookMarkResponse
    suspend fun deleteComment(commentId: String): BaseResponse
    suspend fun deleteMoment(momentId: String): BaseResponse
    suspend fun getMomentDetail(queryParams: Map<String, Any>): MomentDetailResponseModel
    suspend fun getKidDetailByKidId(queryParams: Map<String, Any>): KidByIdResponseModel
    suspend fun shareMoment(shareMomentRequest: ShareMomentRequest): BaseResponse
}