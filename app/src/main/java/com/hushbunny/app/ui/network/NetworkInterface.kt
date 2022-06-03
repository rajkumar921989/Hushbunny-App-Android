package com.hushbunny.app.ui.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.HeaderMap
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.QueryMap

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
}
