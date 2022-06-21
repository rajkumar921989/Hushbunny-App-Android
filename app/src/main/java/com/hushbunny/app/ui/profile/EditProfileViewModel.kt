package com.hushbunny.app.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.hushbunny.app.R
import com.hushbunny.app.providers.ResourceProvider
import com.hushbunny.app.ui.model.*
import com.hushbunny.app.ui.onboarding.model.BaseResponse
import com.hushbunny.app.ui.onboarding.model.LoginResponse
import com.hushbunny.app.ui.repository.UserActionRepository
import com.hushbunny.app.uitls.*
import com.hushbunny.app.uitls.DateFormatUtils.convertDateToISOFormat
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class EditProfileViewModel(
    ioDispatcher: CoroutineDispatcher,
    private val resourceProvider: ResourceProvider,
    private val userActionRepository: UserActionRepository
) : BaseViewModel() {

    private var ioScope = CoroutineScope(ioDispatcher + cancellableJob)

    private val _errorValidation: MutableLiveData<String> = MutableLiveData()
    val errorValidationObserver: LiveData<String> = _errorValidation

    private val _userDetailResponse: MutableLiveData<LoginResponse> = MutableLiveData()
    val userDetailObserver: LiveData<LoginResponse> = _userDetailResponse

    private val _editProfileResponse: MutableLiveData<EventWrapper<LoginResponse>> = MutableLiveData()
    val editProfileObserver: LiveData<EventWrapper<LoginResponse>> = _editProfileResponse

    private val _inviteSpouseResponse: MutableLiveData<BaseResponse> = MutableLiveData()
    val inviteSpouseObserver: LiveData<BaseResponse> = _inviteSpouseResponse

    private val _sendORResendOTPResponse: MutableLiveData<EventWrapper<BaseResponse>> = MutableLiveData()
    val sendORResendOTPObserver: LiveData<EventWrapper<BaseResponse>> = _sendORResendOTPResponse

    private val _verifyOTPResponse: MutableLiveData<EventWrapper<BaseResponse>> = MutableLiveData()
    val verifyOTPObserver: LiveData<EventWrapper<BaseResponse>> = _verifyOTPResponse
    var editedUserDetail: EditedUserDetail? = null

    fun validateEditProfile(
        name: String,
        dateOfBirth: String,
        country: String,
        mobileNumber: String? = null,
        callingCode: String? = null,
        email: String,
        isMale: Boolean,
        isFemale: Boolean,
        isOther: Boolean,
        otherText: String? = null,
        isFather: Boolean,
        isMother: Boolean
    ) {
        when {
            name.isEmpty() -> _errorValidation.postValue(resourceProvider.getString(R.string.please_enter_name))
            dateOfBirth.isEmpty() -> _errorValidation.postValue(resourceProvider.getString(R.string.please_choose_date_of_birth))
            country.isEmpty() -> _errorValidation.postValue(resourceProvider.getString(R.string.please_choose_country))
            email.isEmpty() && !AppConstants.isValidEmail(email) -> _errorValidation.postValue(resourceProvider.getString(R.string.please_enter_valid_email))
            !isMale && !isFemale && !isOther -> _errorValidation.postValue(resourceProvider.getString(R.string.please_choose_gender))
            isOther && otherText.isNullOrEmpty() -> _errorValidation.postValue(resourceProvider.getString(R.string.please_enter_other_gender))
            !isFather && !isMother -> _errorValidation.postValue(resourceProvider.getString(R.string.please_choose_association_with_the_kid))
            else -> {
                _errorValidation.postValue(APIConstants.SUCCESS)
                editedUserDetail = EditedUserDetail(
                    name = name, dateOfBirth = dateOfBirth, country = country,
                    isPhoneNumberEdited = phoneNumberEdited(phoneNumber = mobileNumber, callingCode = callingCode),
                    phoneNumber = mobileNumber.orEmpty(), callingCode = callingCode.orEmpty(), isEmailEdited = emailEdited(email),
                    email = email, gender = if (isMale) APIConstants.MALE else if (isFemale) APIConstants.FEMALE else otherText.orEmpty(),
                    associatedWith = if (isFather) APIConstants.FATHER else APIConstants.MOTHER,
                    isEmailAndPhoneNumberEdited = phoneNumberEdited(phoneNumber = mobileNumber, callingCode = callingCode) && emailEdited(email)
                )
                if (editedUserDetail?.isEmailAndPhoneNumberEdited == true) {
                    sendOTPForMobileNumber(phoneNumber = mobileNumber, callingCode = callingCode)
                } else if (editedUserDetail?.isPhoneNumberEdited == true) {
                    sendOTPForMobileNumber(phoneNumber = mobileNumber, callingCode = callingCode)
                } else if (editedUserDetail?.isEmailEdited == true) {
                    sendOTPForEmail(email)
                } else {
                    updateProfile(editedUserDetail)
                }
            }

        }
    }

    fun updateProfile(editedUserDetail: EditedUserDetail?) {
        ioScope.launch {
            _editProfileResponse.postValue(
                EventWrapper(
                    userActionRepository.editUserProfile(
                        EditProfileRequest(
                            name = editedUserDetail?.name?.ifEmpty { null },
                            countryId = editedUserDetail?.country?.ifEmpty { null },
                            gender = editedUserDetail?.gender?.ifEmpty { null },
                            associatedAs = editedUserDetail?.associatedWith?.ifEmpty { null },
                            dob = editedUserDetail?.dateOfBirth?.convertDateToISOFormat()?.ifEmpty { null },
                            email = if (editedUserDetail?.isEmailEdited == true) editedUserDetail.email else null,
                            callingCode = if (editedUserDetail?.isPhoneNumberEdited == true) editedUserDetail.callingCode else null,
                            phoneNumber = if (editedUserDetail?.isPhoneNumberEdited == true) editedUserDetail.phoneNumber else null
                        )
                    )
                )
            )
        }

    }

    fun getUserDetail() {
        ioScope.launch {
            _userDetailResponse.postValue(userActionRepository.getUserDetail())
        }
    }

    fun inviteSpouse(kidId:String,type: String, email: String, phoneNumber: String, name: String? = null, callingCode: String? = null) {
        when {
            type == APIConstants.EMAIL && email.isEmpty() -> _errorValidation.postValue(
                resourceProvider.getString(
                    R.string.please_enter_email
                )
            )
            type == APIConstants.EMAIL && !AppConstants.isValidEmail(email) -> _errorValidation.postValue(
                resourceProvider.getString(
                    R.string.please_enter_valid_email
                )
            )
            type == APIConstants.PHONE_NUMBER && phoneNumber.isEmpty() -> _errorValidation.postValue(
                resourceProvider.getString(
                    R.string.please_enter_phone_number
                )
            )
            else -> {
                _errorValidation.postValue(APIConstants.SUCCESS)
                ioScope.launch {
                    _inviteSpouseResponse.postValue(
                        userActionRepository.shareWithSpouse(
                            InviteSpouseRequest(
                                shareBy = type,
                                email = if (type == APIConstants.EMAIL) email else null,
                                phoneNumber = if (type == APIConstants.PHONE_NUMBER) phoneNumber else null,
                                callingCode = if (type == APIConstants.PHONE_NUMBER) callingCode else null,
                                name = name,
                                kidId = kidId
                            )
                        )
                    )
                }
            }
        }
    }

    fun verifyOTP(
        otpOne: String,
        otpTwo: String,
        otpThree: String,
        otpFour: String
    ) {
        if (otpOne.isEmpty() || otpTwo.isEmpty() || otpThree.isEmpty() || otpFour.isEmpty()) {
            _errorValidation.postValue(
                resourceProvider.getString(
                    R.string.please_enter_otp
                )
            )
        } else {
            _errorValidation.postValue(APIConstants.SUCCESS)
            ioScope.launch {
                _verifyOTPResponse.postValue(EventWrapper(userActionRepository.verifyEditProfileOTP(VerifyEditProfileOtpRequest(otp = "$otpOne$otpTwo$otpThree$otpFour"))))
            }
        }
    }

    fun sendOTPForEmail(email: String?) {
        ioScope.launch {
            _sendORResendOTPResponse.postValue(
                EventWrapper(
                    userActionRepository.sendEditProfileOTP(
                        EditProfileOtpRequest(
                            editFor = APIConstants.EMAIL,
                            email = email
                        )
                    )
                )

            )
        }
    }

    private fun sendOTPForMobileNumber(phoneNumber: String?, callingCode: String?) {
        ioScope.launch {
            _sendORResendOTPResponse.postValue(
                EventWrapper(
                    userActionRepository.sendEditProfileOTP(
                        EditProfileOtpRequest(
                            editFor = APIConstants.PHONE_NUMBER,
                            callingCode = callingCode,
                            phoneNumber = phoneNumber
                        )
                    )
                )
            )
        }
    }

    fun reSendOTP() {
        ioScope.launch {
            _sendORResendOTPResponse.postValue(EventWrapper(userActionRepository.reSendEditProfileOTP()))
        }
    }

    private fun emailEdited(email: String): Boolean {
        return !email.equals(PrefsManager.get().getString(AppConstants.USER_EMAIL, ""), true)
    }

    private fun phoneNumberEdited(phoneNumber: String?, callingCode: String?): Boolean {
        return  (!phoneNumber.equals(PrefsManager.get().getString(AppConstants.USER_PHONE_NUMBER, ""), true)
                || !callingCode.equals(PrefsManager.get().getString(AppConstants.USER_CALLING_CODE, ""), true))
    }
}