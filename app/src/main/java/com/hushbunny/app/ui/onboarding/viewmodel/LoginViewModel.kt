package com.hushbunny.app.ui.onboarding.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.hushbunny.app.R
import com.hushbunny.app.providers.ResourceProvider
import com.hushbunny.app.ui.onboarding.model.*
import com.hushbunny.app.ui.repository.LoginResponseStatus
import com.hushbunny.app.ui.repository.OnBoardingRepository
import com.hushbunny.app.uitls.APIConstants
import com.hushbunny.app.uitls.AppConstants
import com.hushbunny.app.uitls.AppConstants.Companion.isHavingLowerCaseLetter
import com.hushbunny.app.uitls.AppConstants.Companion.isHavingNumber
import com.hushbunny.app.uitls.AppConstants.Companion.isHavingSpecialCharacter
import com.hushbunny.app.uitls.AppConstants.Companion.isHavingUpperCaseLetter
import com.hushbunny.app.uitls.BaseViewModel
import com.hushbunny.app.uitls.DateFormatUtils.convertDateToISOFormat
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class LoginViewModel(
    ioDispatcher: CoroutineDispatcher,
    private val resourceProvider: ResourceProvider,
    private val onBoardingRepository: OnBoardingRepository
) : BaseViewModel() {
    private var ioScope = CoroutineScope(ioDispatcher + cancellableJob)

    private val _errorValidation: MutableLiveData<String> = MutableLiveData()
    val errorValidationObserver: LiveData<String> = _errorValidation

    private val _loginResponse: MutableLiveData<LoginResponseStatus> = MutableLiveData()
    val loginResponseObserver: LiveData<LoginResponseStatus> = _loginResponse

    private val _forgetPasswordResponse: MutableLiveData<OTPResponse> = MutableLiveData()
    val forgetPasswordObserver: LiveData<OTPResponse> = _forgetPasswordResponse

    private val _verifyForgetPasswordResponse: MutableLiveData<PasswordResponse> =
        MutableLiveData()
    val verifyForgetPasswordObserver: LiveData<PasswordResponse> =
        _verifyForgetPasswordResponse

    private val _resendOTPResponse: MutableLiveData<PasswordResponse> =
        MutableLiveData()
    val resendOTPObserver: LiveData<PasswordResponse> = _resendOTPResponse

    private val _createPasswordResponse: MutableLiveData<PasswordResponse> =
        MutableLiveData()
    val createPasswordObserver: LiveData<PasswordResponse> = _createPasswordResponse

    private val _newAccountOTPResponse: MutableLiveData<OTPResponse> = MutableLiveData()
    val newAccountOTPObserver: LiveData<OTPResponse> = _newAccountOTPResponse

    private val _verifyNewUserOTPResponse: MutableLiveData<NewUserResponse> = MutableLiveData()
    val verifyNewUserOTPObserver: LiveData<NewUserResponse> = _verifyNewUserOTPResponse

    private val _changePasswordResponse: MutableLiveData<PasswordResponse> = MutableLiveData()
    val changePasswordObserver: LiveData<PasswordResponse> = _changePasswordResponse

    fun onLoginClick(
        type: String,
        email: String,
        phoneNumber: String,
        password: String,
        callingCode: String? = null
    ) {
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
            password.isEmpty() -> _errorValidation.postValue(
                resourceProvider.getString(
                    R.string.please_enter_password
                )
            )
            else -> {
                _errorValidation.postValue(APIConstants.SUCCESS)
                ioScope.launch {
                    _loginResponse.postValue(
                        onBoardingRepository.userLogin(
                            loginRequest = LoginRequest(
                                loginBy = type,
                                email = if (type == APIConstants.EMAIL) email else null,
                                password = password,
                                phoneNumber = if (type == APIConstants.PHONE_NUMBER) phoneNumber else null,
                                callingCode = if (type == APIConstants.PHONE_NUMBER) callingCode else null
                            )
                        )
                    )
                }
            }
        }
    }

    fun onForgetClick(
        type: String,
        email: String,
        phoneNumber: String,
        callingCode: String? = null
    ) {
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
                    _forgetPasswordResponse.postValue(
                        onBoardingRepository.forgetUser(
                            forgetPasswordRequest = ForgetPasswordRequest(
                                forgotBy = type,
                                email = if (type == APIConstants.EMAIL) email else null,
                                phoneNumber = if (type == APIConstants.PHONE_NUMBER) phoneNumber else null,
                                callingCode = if (type == APIConstants.PHONE_NUMBER) callingCode else null
                            )
                        )
                    )
                }
            }
        }
    }

    fun verifyForgetOTP(
        otpID: String,
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
                _verifyForgetPasswordResponse.postValue(
                    onBoardingRepository.verifyForgetOTP(
                        verifyForgetOTPRequest = VerifyForgetOTPRequest(
                            forgotId = otpID,
                            otp = "$otpOne$otpTwo$otpThree$otpFour"
                        )
                    )
                )
            }
        }

    }

    fun onResendOTP(otpID: String, type: String) {
        _errorValidation.postValue(APIConstants.SUCCESS)
        ioScope.launch {
            _resendOTPResponse.postValue(
                onBoardingRepository.resendOTP(
                    resendOTPRequest = ResendOTPRequest(
                        otpId = otpID, type = type
                    )
                )
            )
        }
    }

    fun createPassword(password: String, confirmPassword: String, forgotId: String) {
        when {
            password.isEmpty() -> _errorValidation.postValue(resourceProvider.getString(R.string.please_enter_password))
            password.length < 8 -> _errorValidation.postValue(resourceProvider.getString(R.string.password_minimum_character_error))
            !password.isHavingUpperCaseLetter() -> _errorValidation.postValue(
                resourceProvider.getString(
                    R.string.password_upper_letter_error
                )
            )
            !password.isHavingLowerCaseLetter() -> _errorValidation.postValue(
                resourceProvider.getString(
                    R.string.password_lower_letter_error
                )
            )
            !password.isHavingNumber() -> _errorValidation.postValue(
                resourceProvider.getString(
                    R.string.password_number_error
                )
            )
            !password.isHavingSpecialCharacter() -> _errorValidation.postValue(
                resourceProvider.getString(
                    R.string.password_special_character_error
                )
            )
            confirmPassword.isEmpty() -> _errorValidation.postValue(resourceProvider.getString(R.string.please_enter_confirm_password))
            password != confirmPassword -> _errorValidation.postValue(resourceProvider.getString(R.string.password_mismatch_error))
            else -> {
                _errorValidation.postValue(APIConstants.SUCCESS)
                ioScope.launch {
                    _createPasswordResponse.postValue(
                        onBoardingRepository.createPassword(
                            createPasswordRequest = CreatePasswordRequest(
                                forgotId = forgotId, password = password
                            )
                        )
                    )
                }
            }
        }
    }

    fun signUpStepOne(name: String): String {
        return if (name.isEmpty()) resourceProvider.getString(R.string.please_enter_name)
        else ""
    }

    fun signUpStepTwo(date: String): String {
        return if (date.isEmpty()) resourceProvider.getString(R.string.please_choose_date_of_birth)
        else ""
    }

    fun signUpStepThree(
        isMale: Boolean,
        isFemale: Boolean,
        isOthers: Boolean,
        others: String
    ): String {
        return if (!isMale && !isFemale && !isOthers)
            resourceProvider.getString(R.string.please_choose_gender)
        else if (isOthers && others.isEmpty())
            resourceProvider.getString(R.string.please_enter_others_gender_type)
        else ""
    }

    fun signUpStepFour(
        isFather: Boolean,
        isMother: Boolean
    ): String {
        return if (!isFather && !isMother)
            resourceProvider.getString(R.string.please_choose_association_with_the_kid)
        else ""
    }

    fun signUpStepFive(
        country: String
    ): String {
        return if (country.isEmpty())
            resourceProvider.getString(R.string.please_choose_country)
        else ""
    }

    fun getOTPForNewAccount(
        type: String,
        email: String,
        phoneNumber: String,
        password: String,
        callingCode: String? = null, userModel: SignInUserModel?
    ) {
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
            password.isEmpty() -> _errorValidation.postValue(
                resourceProvider.getString(
                    R.string.please_enter_password
                )
            )
            else -> {
                _errorValidation.postValue(APIConstants.SUCCESS)
                ioScope.launch {
                    _newAccountOTPResponse.postValue(
                        onBoardingRepository.createNewAccount(
                            CreateNewAccountRequest(
                                registrationBy = type,
                                name = userModel?.name.orEmpty(),
                                email = if (type == APIConstants.EMAIL) email else null,
                                password = password,
                                phoneNumber = if (type == APIConstants.PHONE_NUMBER) phoneNumber else null,
                                callingCode = if (type == APIConstants.PHONE_NUMBER) callingCode else null,
                                countryId = userModel?.country.orEmpty(),
                                associatedAs = userModel?.relationShipWithKid.orEmpty().uppercase(),
                                gender = userModel?.gender.orEmpty(),
                                dob = userModel?.dateOfBirth?.convertDateToISOFormat()
                            )
                        )
                    )
                }
            }
        }

    }

    fun verifyOTPForNewUser(
        otpID: String,
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
                _verifyNewUserOTPResponse.postValue(
                    onBoardingRepository.verifyNewAccountOTP(
                        verifyNewUserOTPRequest = VerifyNewUserOTPRequest(
                            otpId = otpID,
                            otp = "$otpOne$otpTwo$otpThree$otpFour"
                        )
                    )
                )
            }
        }

    }

    fun changePassword(oldPassword: String, password: String, confirmPassword: String) {
        when {
            oldPassword.isEmpty() -> _errorValidation.postValue(resourceProvider.getString(R.string.please_enter_old_password))
            oldPassword.length < 8 -> _errorValidation.postValue(resourceProvider.getString(R.string.password_minimum_character_error))
            password.isEmpty() -> _errorValidation.postValue(resourceProvider.getString(R.string.please_enter_password))
            password.length < 8 -> _errorValidation.postValue(resourceProvider.getString(R.string.password_minimum_character_error))
            !password.isHavingUpperCaseLetter() -> _errorValidation.postValue(
                resourceProvider.getString(
                    R.string.password_upper_letter_error
                )
            )
            !password.isHavingLowerCaseLetter() -> _errorValidation.postValue(resourceProvider.getString(R.string.password_lower_letter_error))
            !password.isHavingSpecialCharacter() -> _errorValidation.postValue(
                resourceProvider.getString(
                    R.string.password_special_character_error
                )
            )
            !password.isHavingNumber() -> _errorValidation.postValue(
                resourceProvider.getString(
                    R.string.password_number_error
                )
            )
            confirmPassword.isEmpty() -> _errorValidation.postValue(resourceProvider.getString(R.string.please_enter_confirm_password))
            password != confirmPassword -> _errorValidation.postValue(resourceProvider.getString(R.string.password_mismatch_error))
            else -> {
                _errorValidation.postValue(APIConstants.SUCCESS)
                ioScope.launch {
                    _changePasswordResponse.postValue(
                        onBoardingRepository.changePassword(
                            changePasswordRequest = ChangePasswordRequest(
                                oldPassword = oldPassword,
                                newPassword = password
                            )
                        )
                    )
                }
            }
        }
    }

}