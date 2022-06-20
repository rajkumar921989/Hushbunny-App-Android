package com.hushbunny.app.ui.setting

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.hushbunny.app.providers.ResourceProvider
import com.hushbunny.app.ui.model.NotificationSettingsRequest
import com.hushbunny.app.ui.model.UserActionResponseModel
import com.hushbunny.app.ui.onboarding.model.LoginResponse
import com.hushbunny.app.ui.repository.UserActionRepository
import com.hushbunny.app.uitls.BaseViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class SettingViewModel(
    ioDispatcher: CoroutineDispatcher,
    private val resourceProvider: ResourceProvider,
    private val userActionRepository: UserActionRepository
) : BaseViewModel() {
    private var ioScope = CoroutineScope(ioDispatcher + cancellableJob)

    private val _userActionResponse: MutableLiveData<UserActionResponseModel> = MutableLiveData()
    val userActionResponseObserver: LiveData<UserActionResponseModel> = _userActionResponse

    private val _notificationResponse: MutableLiveData<LoginResponse> = MutableLiveData()
    val notificationObserver: LiveData<LoginResponse> = _notificationResponse

    fun updateUserAction(type: String) {
        ioScope.launch {
            _userActionResponse.postValue(userActionRepository.updateUserAction(type))
        }
    }

    fun updateNotificationSetting(type: String, important: Boolean, optional: Boolean) {
        ioScope.launch {
            _notificationResponse.postValue(
                userActionRepository.updateNotificationSettings(
                    NotificationSettingsRequest(
                        important = important,
                        optional = optional,
                        type = type
                    )
                )
            )
        }
    }
}