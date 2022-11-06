package com.hushbunny.app.core

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.hushbunny.app.uitls.BaseViewModel
import com.hushbunny.app.uitls.EventWrapper

class HomeSharedViewModel: BaseViewModel() {

    private val homeTabClicked: MutableLiveData<Boolean> = MutableLiveData()
    val homeTabClickedObserver: LiveData<Boolean> = homeTabClicked

    private val notificationTabClicked: MutableLiveData<Boolean> = MutableLiveData()
    val notificationTabClickedObserver: LiveData<Boolean> = notificationTabClicked

    private val profileTabClicked: MutableLiveData<EventWrapper<Boolean>> = MutableLiveData()
    val profileTabClickedTabClickedObserver: LiveData<EventWrapper<Boolean>> = profileTabClicked

    private val notificationCountRefresh: MutableLiveData<Boolean> = MutableLiveData()
    val notificationCountRefreshObserver: LiveData<Boolean> = notificationCountRefresh

    fun onProfileTabClicked() {
        profileTabClicked.postValue(EventWrapper(true))
    }

    fun onHomeTabClicked() {
        homeTabClicked.postValue(true)
    }

    fun onNotificationTabClicked() {
        notificationTabClicked.postValue(true)
    }

    fun refreshNotificationUnReadCount() {
        notificationCountRefresh.postValue(true)
    }
}