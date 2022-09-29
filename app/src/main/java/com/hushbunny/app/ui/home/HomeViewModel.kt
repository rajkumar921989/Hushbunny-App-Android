package com.hushbunny.app.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.hushbunny.app.R
import com.hushbunny.app.providers.ResourceProvider
import com.hushbunny.app.ui.enumclass.MomentType
import com.hushbunny.app.ui.model.*
import com.hushbunny.app.ui.onboarding.model.BaseResponse
import com.hushbunny.app.ui.repository.FileUploadRepository
import com.hushbunny.app.ui.repository.HomeRepository
import com.hushbunny.app.ui.sealedclass.BlockedUserList
import com.hushbunny.app.ui.sealedclass.KidsStatusInfo
import com.hushbunny.app.ui.sealedclass.MomentResponseInfo
import com.hushbunny.app.uitls.APIConstants
import com.hushbunny.app.uitls.AppConstants
import com.hushbunny.app.uitls.BaseViewModel
import com.hushbunny.app.uitls.DateFormatUtils.convertDateToISOFormat
import com.hushbunny.app.uitls.EventWrapper
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.io.File

class HomeViewModel(
    ioDispatcher: CoroutineDispatcher,
    private val resourceProvider: ResourceProvider,
    private val homeRepository: HomeRepository,
    private val fileUploadRepository: FileUploadRepository? = null,
) : BaseViewModel() {
    private var ioScope = CoroutineScope(ioDispatcher + cancellableJob)

    private val _kidsListResponse: MutableLiveData<EventWrapper<KidsStatusInfo>> = MutableLiveData()
    val kidsListObserver: LiveData<EventWrapper<KidsStatusInfo>> = _kidsListResponse

    private val _addKidsResponse: MutableLiveData<AddKidsResponseModel> = MutableLiveData()
    val addKidsObserver: LiveData<AddKidsResponseModel> = _addKidsResponse

    private val _errorValidation: MutableLiveData<String> = MutableLiveData()
    val errorValidationObserver: LiveData<String> = _errorValidation

    private val _fileUploadResponse: MutableLiveData<EventWrapper<FileUploadResponse>> = MutableLiveData()
    val fileUploadObserver: LiveData<EventWrapper<FileUploadResponse>> = _fileUploadResponse

    private val _blockedUserResponse: MutableLiveData<BaseResponse> = MutableLiveData()
    val blockedUserObserver: LiveData<BaseResponse> = _blockedUserResponse

    private val _notificationCountResponse: MutableLiveData<NotificationUnreadCountModel> = MutableLiveData()
    val notificationCountObserver: LiveData<NotificationUnreadCountModel> = _notificationCountResponse

    fun blockUser(userId: String, action: String) {
        ioScope.launch {
            _blockedUserResponse.postValue(
                homeRepository.blockUser(blockUnBlockUserRequest = BlockUnBlockUserRequest(userId = userId, action = action))
            )
        }
    }

    fun getKidsList() {
        ioScope.launch {
            _kidsListResponse.postValue(EventWrapper(homeRepository.getKidsList()))
        }
    }

    fun addOREditKid(
        isEditKid: Boolean,
        kidID: String? = null,
        name: String,
        gender: String,
        nickName: String? = null,
        dateOfBirth: String? = null,
        country: String? = null,
        city: String? = null,
        image: File? = null
    ) {
        when {
            name.isEmpty() -> _errorValidation.postValue(resourceProvider.getString(R.string.please_enter_name))
            gender.isEmpty() -> _errorValidation.postValue(resourceProvider.getString(R.string.please_choose_gender))
            else -> {
                ioScope.launch {
                    _errorValidation.postValue(APIConstants.SUCCESS)
                    if (image != null && image.exists()) {
                        val imageResponse = fileUploadRepository?.uploadFile(image)
                        if (imageResponse?.statusCode == APIConstants.API_RESPONSE_200) {
                            _addKidsResponse.postValue(
                                homeRepository.addOREditKid(
                                    isEditKid,
                                    AddKidRequest(
                                        _id = kidID,
                                        name = name,
                                        gender = gender,
                                        nickName = if(nickName.isNullOrEmpty()) null else nickName,
                                        dob = dateOfBirth?.convertDateToISOFormat(),
                                        birthCountryISO2 = country,
                                        birtCity = city,
                                        image = imageResponse.data?.url.orEmpty()
                                    )
                                )
                            )
                        } else {
                            _addKidsResponse.postValue(
                                homeRepository.addOREditKid(
                                    isEditKid,
                                    AddKidRequest(
                                        _id = kidID,
                                        name = name,
                                        gender = gender,
                                        nickName = if(nickName.isNullOrEmpty()) null else nickName,
                                        dob = dateOfBirth?.convertDateToISOFormat(),
                                        birthCountryISO2 = country,
                                        birtCity = city
                                    )
                                )
                            )
                        }
                    } else {
                        _addKidsResponse.postValue(
                            homeRepository.addOREditKid(
                                isEditKid,
                                AddKidRequest(
                                    _id = kidID,
                                    name = name,
                                    gender = gender,
                                    nickName = if(nickName.isNullOrEmpty()) null else nickName,
                                    dob = dateOfBirth?.convertDateToISOFormat(),
                                    birthCountryISO2 = country,
                                    birtCity = city
                                )
                            )
                        )
                    }
                }
            }
        }
    }

    fun updateKidImage(kidID: String, image: String) {
        ioScope.launch {
            _addKidsResponse.postValue(homeRepository.addOREditKid(true, AddKidRequest(_id = kidID, image = image)))
        }
    }

    fun addKidStepOne(name: String): String {
        return if (name.isEmpty()) resourceProvider.getString(R.string.please_enter_name)
        else ""
    }

    fun addKidStepTwo(isGenderSelected: Boolean): String {
        return if (isGenderSelected) "" else resourceProvider.getString(R.string.please_choose_gender)
    }

    fun addKidStepThree(dateOfBirth: String): String {
        return if (dateOfBirth.isEmpty()) resourceProvider.getString(R.string.please_choose_date_of_birth)
        else ""
    }

    fun uploadFile(imageFile: File) {
        ioScope.launch {
            fileUploadRepository?.uploadFile(imageFile)?.let {
                _fileUploadResponse.postValue(EventWrapper(it))
            }
        }
    }
    fun getNotificationCount(){
        ioScope.launch {
            _notificationCountResponse.postValue(homeRepository.unReadNotificationCount())
        }
    }
}