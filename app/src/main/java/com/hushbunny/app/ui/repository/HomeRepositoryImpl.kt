package com.hushbunny.app.ui.repository

import com.hushbunny.app.providers.ResourceProvider
import com.hushbunny.app.ui.model.AddKidRequest
import com.hushbunny.app.ui.model.AddKidsResponseModel
import com.hushbunny.app.ui.model.BlockUnBlockUserRequest
import com.hushbunny.app.ui.model.KidsResponseModel
import com.hushbunny.app.ui.sealedclass.BlockedUserList
import com.hushbunny.app.ui.sealedclass.KidsStatusInfo
import com.hushbunny.app.ui.service.HomeService
import com.hushbunny.app.uitls.APIConstants
import javax.inject.Inject

class HomeRepositoryImpl @Inject constructor(private val resourceProvider: ResourceProvider, private val homeService: HomeService) : HomeRepository {

    override suspend fun getKidsList(): KidsStatusInfo {
        val responseModel = homeService.getKidsList()
        val kidsList = arrayListOf<KidsResponseModel>()
        kidsList.add(addNewKid())
        return when (responseModel.statusCode) {
            APIConstants.API_RESPONSE_200 -> {
                if (responseModel.data?.listing.orEmpty().isNotEmpty()) {
                    kidsList.addAll(responseModel.data?.listing.orEmpty())
                }
                KidsStatusInfo.UserList(kidsList)
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

    override suspend fun addOREditKid(isEditKid: Boolean, addKidRequest: AddKidRequest): AddKidsResponseModel {
        return homeService.addNewKid(isEditKid, addKidRequest)
    }

    override suspend fun getBlockedUserList(): BlockedUserList {
        val responseModel = homeService.getBlockedUserList()
        return when (responseModel.statusCode) {
            APIConstants.API_RESPONSE_200 -> {
                if (responseModel.data?.listing.orEmpty().isNotEmpty()) {
                    BlockedUserList.UserList(responseModel.data?.listing.orEmpty())
                } else {
                    BlockedUserList.NoList
                }
            }
            else -> {
                BlockedUserList.Error(responseModel.message)
            }
        }
    }

    override suspend fun blockUnBlockUser(blockUnBlockUserRequest: BlockUnBlockUserRequest): BlockedUserList {
        val response = homeService.blockUnBlockUser(blockUnBlockUserRequest)
        return when (response.statusCode) {
            APIConstants.API_RESPONSE_200 -> {
                getBlockedUserList()
            }
            else -> {
                BlockedUserList.Error(response.message.orEmpty())
            }
        }

    }
}