package com.hushbunny.app.ui.navigation

import android.net.Uri
import androidx.lifecycle.LiveData
import com.hushbunny.app.uitls.EventWrapper

interface NavigationRouterProvider {

    fun navigateToMomentDetail(momentID: String)

    val navigationObserver: LiveData<EventWrapper<NavigationModel>>
}
