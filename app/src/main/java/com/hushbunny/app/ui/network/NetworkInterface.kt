package com.hushbunny.app.ui.network

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

@JvmSuppressWildcards
interface NetworkInterface {

    @GET("/{hostEndPoint}")
    suspend fun getDataUsingCoroutine(
        @Path(value = "hostEndPoint", encoded = true) endPoint: String,
        @QueryMap(encoded = true) optionsMap: Map<String, Any>?,
        @HeaderMap headers: Map<String, String>?
    ): Response<String>

    @POST("/{hostEndPoint}")
    suspend fun postDataUsingCoroutine(
        @Path(value = "hostEndPoint", encoded = true) endPoint: String,
        @Body requestBody: Any,
        @HeaderMap headers: Map<String, String>?,
        @QueryMap(encoded = true) queryMap: Map<String, Any>?
    ): Response<String>


    @PUT("/{hostEndPoint}")
    suspend fun putDataUsingCoroutine(
        @Path(value = "hostEndPoint", encoded = true) endPoint: String,
        @Body requestBody: Any,
        @HeaderMap headers: Map<String, String>?,
        @QueryMap(encoded = true) queryParams: Map<String, String>?
    ): Response<String>


    @PATCH("/{hostEndPoint}")
    suspend fun patchStringDataUsingCoroutine(
        @Path(value = "hostEndPoint", encoded = true) endPoint: String,
        @Body requestBody: Any,
        @HeaderMap headers: Map<String, String>?
    ): Response<String>

    @DELETE("/{hostEndPoint}")
    suspend fun deleteDataUsingCoroutine(
        @Path(value = "hostEndPoint", encoded = true) endPoint: String,
        @HeaderMap headers: Map<String, String>?,
        @QueryMap(encoded = true) queryParams: Map<String, String>?
    ): Response<String>

    @POST("/{hostEndPoint}")
    suspend fun fileUploadUsingCoroutine(
        @Path(value = "hostEndPoint", encoded = true) endPoint: String,
        @Body requestBody: RequestBody,
        @HeaderMap headers: Map<String, String>?
    ): Response<String>

    @PUT("/{hostEndPoint}")
    suspend fun putDataWithoutRequestBodyUsingCoroutine(
        @Path(value = "hostEndPoint", encoded = true) endPoint: String,
        @HeaderMap headers: Map<String, String>?,
        @QueryMap(encoded = true) queryParams: Map<String, String>?
    ): Response<String>
}
