package com.hushbunny.app.ui.network

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

internal class NetworkMonitorImp(private val application: Application) : NetworkMonitor {

    override fun isNetworkConnected(): Boolean {
        val connectivityManager =
            application.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager

        val isNetworkActive = connectivityManager?.activeNetwork
        val getNetworkCapability = connectivityManager?.getNetworkCapabilities(isNetworkActive)
        return getNetworkCapability != null &&
            getNetworkCapability.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}
