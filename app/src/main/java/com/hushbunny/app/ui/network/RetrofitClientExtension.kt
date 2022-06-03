package com.hushbunny.app.ui.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

fun RetrofitClient.addHttpLoggingInterceptor(builder: OkHttpClient.Builder) {
    val interceptor = HttpLoggingInterceptor()
    interceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
    builder.addInterceptor(interceptor)
}
