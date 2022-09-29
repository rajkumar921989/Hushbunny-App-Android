package com.hushbunny.app.ui.notifications

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hushbunny.app.providers.ResourceProvider
import com.hushbunny.app.ui.model.AcceptOrRejectNotificationRequest
import com.hushbunny.app.ui.model.NotificationShareId
import com.hushbunny.app.ui.model.UnReadNotificationRequest
import com.hushbunny.app.ui.onboarding.model.BaseResponse
import com.hushbunny.app.ui.repository.HomeRepository
import com.hushbunny.app.ui.sealedclass.KidsStatusInfo
import com.hushbunny.app.ui.sealedclass.NotificationInfoList
import com.hushbunny.app.uitls.APIConstants
import com.hushbunny.app.uitls.BaseViewModel
import com.hushbunny.app.uitls.EventWrapper
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class NotificationsViewModel(
    ioDispatcher: CoroutineDispatcher,
    private val notificationRepository: HomeRepository
) : BaseViewModel() {

    private var ioScope = CoroutineScope(ioDispatcher + cancellableJob)

    private val _notificationResponse: MutableLiveData<EventWrapper<NotificationInfoList>> = MutableLiveData()
    val notificationObserver: LiveData<EventWrapper<NotificationInfoList>> = _notificationResponse

    private val _acceptOrRejectNotification: MutableLiveData<EventWrapper<BaseResponse>> = MutableLiveData()
    val acceptOrRejectNotificationObserver: LiveData<EventWrapper<BaseResponse>> = _acceptOrRejectNotification

    fun getNotificationList(currentPage: Int) {
        val queryParams = hashMapOf<String, Any>()
        queryParams[APIConstants.QUERY_PARAMS_PAGE] = currentPage
        queryParams[APIConstants.QUERY_PARAMS_PER_PAGE] = APIConstants.QUERY_PARAMS_PER_PAGE_VALUE
        ioScope.launch {
            _notificationResponse.postValue(EventWrapper(notificationRepository.notificationList(queryParams)))
        }
    }

    fun acceptOrRejectNotification(shareId: String, type: String) {
        ioScope.launch {
            _acceptOrRejectNotification.postValue(
                EventWrapper(notificationRepository.acceptOrRejectNotification(
                    AcceptOrRejectNotificationRequest(
                        action = type,
                        shareId = shareId
                    ))
                )
            )
        }
    }
    fun unReadNotification(notificationId:String){
        ioScope.launch {
            notificationRepository.unReadNotification(UnReadNotificationRequest(notificationId = notificationId))
        }
    }
}