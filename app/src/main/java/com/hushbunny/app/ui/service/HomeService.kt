package com.hushbunny.app.ui.service

import com.hushbunny.app.ui.model.*
import com.hushbunny.app.ui.onboarding.model.BaseResponse

interface HomeService {
    suspend fun getKidsList(): KidsListResponseModel
    suspend fun addNewKid(isEditKid: Boolean, addKidRequest: AddKidRequest): AddKidsResponseModel
    suspend fun getBlockedUserList(): BlockedUserListResponseModel
    suspend fun blockUnBlockUser(blockUnBlockUserRequest: BlockUnBlockUserRequest): BaseResponse
}