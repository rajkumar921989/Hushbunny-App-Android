package com.hushbunny.app.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.hushbunny.app.R
import com.hushbunny.app.providers.ResourceProvider
import com.hushbunny.app.ui.model.AddKidRequest
import com.hushbunny.app.ui.model.AddKidsResponseModel
import com.hushbunny.app.ui.model.FileUploadResponse
import com.hushbunny.app.ui.repository.FileUploadRepository
import com.hushbunny.app.ui.repository.HomeRepository
import com.hushbunny.app.ui.sealedclass.KidsStatusInfo
import com.hushbunny.app.uitls.APIConstants
import com.hushbunny.app.uitls.BaseViewModel
import com.hushbunny.app.uitls.DateFormatUtils.convertDateToISOFormat
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

    private val _kidsListResponse: MutableLiveData<KidsStatusInfo> = MutableLiveData()
    val kidsListObserver: LiveData<KidsStatusInfo> = _kidsListResponse

    private val _addKidsResponse: MutableLiveData<AddKidsResponseModel> = MutableLiveData()
    val addKidsObserver: LiveData<AddKidsResponseModel> = _addKidsResponse

    private val _errorValidation: MutableLiveData<String> = MutableLiveData()
    val errorValidationObserver: LiveData<String> = _errorValidation

    private val _fileUploadResponse: MutableLiveData<FileUploadResponse> = MutableLiveData()
    val fileUploadObserver: LiveData<FileUploadResponse> = _fileUploadResponse


    fun getKidsList() {
        ioScope.launch {
            _kidsListResponse.postValue(homeRepository.getKidsList())
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
        image: String? = null
    ) {
        when {
            name.isEmpty() -> _errorValidation.postValue(resourceProvider.getString(R.string.please_enter_name))
            gender.isEmpty() -> _errorValidation.postValue(resourceProvider.getString(R.string.please_choose_gender))
            else -> {
                ioScope.launch {
                    _errorValidation.postValue(APIConstants.SUCCESS)
                    _addKidsResponse.postValue(
                        homeRepository.addOREditKid(
                            isEditKid,
                            AddKidRequest(
                                _id = kidID,
                                name = name,
                                gender = gender,
                                nickName = nickName,
                                dob = dateOfBirth?.convertDateToISOFormat(),
                                birthCountryISO2 = country,
                                birtCity = city,
                                image = image
                            )
                        )
                    )
                }
            }
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
            _fileUploadResponse.postValue(fileUploadRepository?.uploadFile(imageFile))
        }
    }

}