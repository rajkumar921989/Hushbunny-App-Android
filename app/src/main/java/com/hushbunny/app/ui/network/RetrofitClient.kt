package com.hushbunny.app.ui.network

import android.content.Context
import com.chuckerteam.chucker.api.ChuckerInterceptor
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Retrofit Client Class
 */
class RetrofitClient(context: Context) : NetworkClient {

    private val networkClient = getHttpClient(context)
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

    private fun getHttpClient(context: Context): OkHttpClient {
        val okHttpClient = OkHttpClient()
        val builder = okHttpClient.newBuilder()
        builder.connectTimeout(timeOut.toLong(), TimeUnit.SECONDS)
            .readTimeout(timeOut.toLong(), TimeUnit.SECONDS)
            .writeTimeout(timeOut.toLong(), TimeUnit.SECONDS)

        addHttpLoggingInterceptor(context, builder)

        return builder.build()
    }

    private fun addHttpLoggingInterceptor(context: Context, builder: OkHttpClient.Builder) {
        val interceptor = HttpLoggingInterceptor()
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
        builder.run {
            addInterceptor(ChuckerInterceptor.Builder(context).build())
            addInterceptor(interceptor)
        }
    }
}
