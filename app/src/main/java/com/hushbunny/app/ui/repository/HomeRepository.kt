package com.hushbunny.app.ui.repository

import com.hushbunny.app.ui.model.AddKidRequest
import com.hushbunny.app.ui.model.AddKidsResponseModel
import com.hushbunny.app.ui.model.BlockUnBlockUserRequest
import com.hushbunny.app.ui.sealedclass.BlockedUserList
import com.hushbunny.app.ui.sealedclass.KidsStatusInfo

interface HomeRepository {
    suspend fun getKidsList(): KidsStatusInfo
    suspend fun addOREditKid(isEditKid: Boolean, addKidRequest: AddKidRequest): AddKidsResponseModel
    suspend fun getBlockedUserList(): BlockedUserList
    suspend fun blockUnBlockUser(blockUnBlockUserRequest: BlockUnBlockUserRequest): BlockedUserList
  }