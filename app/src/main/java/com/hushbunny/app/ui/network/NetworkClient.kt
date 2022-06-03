package com.hushbunny.app.ui.network

interface NetworkClient {
    fun getNetworkService(baseUrl: String): NetworkInterface
}
