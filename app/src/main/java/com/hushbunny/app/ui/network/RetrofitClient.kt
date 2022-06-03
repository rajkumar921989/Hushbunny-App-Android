package com.hushbunny.app.ui.network

import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Retrofit Client Class
 */
class RetrofitClient() : NetworkClient {

    private val networkClient = getHttpClient()
    private val retrofitBuilder = Retrofit.Builder()
    private val gsonConverterFactory = GsonConverterFactory.create()
    private val timeOut = 60 // in seconds

    private fun getNewNetworkCall(request: Request, okHttpClient: OkHttpClient): Call {
        var req = request
        req = req.newBuilder().tag(arrayOf<Any?>(null)).build()
        return okHttpClient.newCall(req)
    }

    @Synchronized
    override fun getNetworkService(baseUrl: String): NetworkInterface {
        return retrofitBuilder.baseUrl(baseUrl)
            .client(networkClient)
            .callFactory { request -> getNewNetworkCall(request, networkClient) }
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(gsonConverterFactory)
            .build().create(NetworkInterface::class.java)
    }

    private fun getHttpClient(): OkHttpClient {
        val okHttpClient = OkHttpClient()
        val builder = okHttpClient.newBuilder()
        builder.connectTimeout(timeOut.toLong(), TimeUnit.SECONDS)
            .readTimeout(timeOut.toLong(), TimeUnit.SECONDS)
            .writeTimeout(timeOut.toLong(), TimeUnit.SECONDS)

        addHttpLoggingInterceptor(builder)

        return builder.build()
    }
}
