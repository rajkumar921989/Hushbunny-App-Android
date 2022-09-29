package com.hushbunny.app.ui.moment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.hushbunny.app.uitls.BaseViewModel
import com.hushbunny.app.uitls.EventWrapper

class NavigationViewModel : BaseViewModel() {

    private val _momentDetail: MutableLiveData<EventWrapper<String>> = MutableLiveData()
    val momentObserver: LiveData<EventWrapper<String>> = _momentDetail

    fun setMomentDetail(momentID: String) {
        if (momentID.isNotEmpty())
            _momentDetail.postValue(EventWrapper(momentID))
    }
}