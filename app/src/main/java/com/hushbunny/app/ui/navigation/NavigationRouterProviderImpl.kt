package com.hushbunny.app.ui.navigation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.hushbunny.app.providers.ResourceProvider
import com.hushbunny.app.ui.enumclass.NavigationPages
import com.hushbunny.app.uitls.BaseViewModel
import com.hushbunny.app.uitls.EventWrapper

class NavigationRouterProviderImpl(private val resourceProvider: ResourceProvider) : BaseViewModel(), NavigationRouterProvider {

    private val _navigationObserver: MutableLiveData<EventWrapper<NavigationModel>> = MutableLiveData()
    override val navigationObserver: LiveData<EventWrapper<NavigationModel>> get() = _navigationObserver
    override fun navigateToMomentDetail(momentID: String) {
        if (momentID.isNotEmpty()) {
            _navigationObserver.postValue(EventWrapper(NavigationModel(type = NavigationPages.MOMENT_DETAIL.name, momentID = momentID)))
        }
    }
}