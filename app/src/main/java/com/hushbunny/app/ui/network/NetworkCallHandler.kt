package com.hushbunny.app.ui.network

import android.annotation.SuppressLint
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import com.hushbunny.app.uitls.AppConstants
import com.hushbunny.app.uitls.ImageViewAndFileUtils.createFileRequest
import retrofit2.Response
import java.io.File
import java.lang.reflect.Type

class NetworkCallHandler(private val networkMonitor: NetworkMonitor, val networkClient: NetworkClient) {

    companion object {
        const val NO_NETWORK_ERROR_CODE = 12001
        const val NO_SERVICE_RESPONSE_ERROR_CODE = 400
        const val NO_NETWORK_ERROR_MESSAGE =
            "No internet connection, kindly check your network strength."
        const val NETWORK_FAILURE_DEFAULT = "Server error."
        const val SUCCESS_RESPONSE = "success"
        const val SUCCESS_RESPONSE_CODE = 200

        private const val NO_CONTENT_RESPONSE_CODE = 204
        private const val NO_CONTENT_MESSAGE = "No Data Found"
        val TYPE_DEFAULTS = mapOf(
            "java.util.List" to "[]",
            "java.lang.Boolean" to "false"
        )
    }

    suspend inline fun <reified T : Any> getData(
        baseUrl: String,
        endPoint: String,
        queryParams: Map<String, Any>? = hashMapOf(),
        headers: Map<String, String>? = null
    ): T {
        if (!isNetworkConnected()) return getErrorResponseAsObjectOrDefault(T::class.java, getNoNetworkError())

        val retrofitResult = networkClient.getNetworkService(baseUrl).runCatching {
            getDataUsingCoroutine(endPoint, queryParams, headers)
        }

        val response = retrofitResult.getOrNull()

        if (retrofitResult.isSuccess && response != null) {
            return if (response.code() == SUCCESS_RESPONSE_CODE && !isValidJsonData(response.body().orEmpty())) {
                getErrorResponseAsObjectOrDefault(T::class.java, getNoContentMessage())
            } else {
                val validatedResponse = validateServerResponse(response)
                runCatching {
                    Gson().fromJson<T>(validatedResponse, object : TypeToken<T>() {}.type)
                }.getOrNull() ?: getErrorResponseAsObjectOrDefault(T::class.java, getServerUnhandledError(response.code()))
            }
        }
        return getErrorResponseAsObjectOrDefault(
            T::class.java, getServerUnhandledError(
                NO_SERVICE_RESPONSE_ERROR_CODE
            )
        )
    }

    @SuppressLint("NewApi")
    inline fun <reified T : Any> getErrorResponseAsObjectOrDefault(type: Type?, message: String): T {
        val defaultVal = TYPE_DEFAULTS.getOrDefault(type?.typeName, null)
        return Gson().fromJson(defaultVal ?: message, object : TypeToken<T>() {}.type)
    }


    suspend inline fun <reified T : Any> postDataHandler(
        baseUrl: String,
        endPoint: String,
        requestBody: Any,
        headers: Map<String, String>? = null,
        queryParams: Map<String, Any>? = hashMapOf()
    ): T {
        if (!isNetworkConnected()) {
            return getErrorResponseAsObjectOrDefault(T::class.java, getNoNetworkError())
        }

        val retrofitResult = networkClient.getNetworkService(baseUrl).runCatching {
            postDataUsingCoroutine(endPoint, requestBody, headers, queryParams)
        }

        val response = retrofitResult.getOrNull()

        if (retrofitResult.isSuccess && response != null) {
            return if (response.code() == SUCCESS_RESPONSE_CODE && !isValidJsonData(response.body().orEmpty())) {
                getErrorResponseAsObjectOrDefault(T::class.java, getNoContentMessage())
            } else {
                val validatedResponse = validateServerResponse(response)
                runCatching {
                    Gson().fromJson<T>(validatedResponse, object : TypeToken<T>() {}.type)
                }.getOrNull() ?: (getErrorResponseAsObjectOrDefault(T::class.java, getServerUnhandledError(response.code())))
            }
        }
        return getErrorResponseAsObjectOrDefault(
            T::class.java, getServerUnhandledError(
                NO_SERVICE_RESPONSE_ERROR_CODE
            )
        )
    }


    suspend inline fun <reified T : Any> putDataHandler(
        baseUrl: String,
        endPoint: String,
        requestBody: Any? = null,
        headers: Map<String, String>? = null,
        queryParams: Map<String, String>? = hashMapOf()
    ): T {
        if (!isNetworkConnected()) {
            return Gson().fromJson(getNoNetworkError(), T::class.java)
        }
        val retrofitResult = networkClient.getNetworkService(baseUrl).runCatching {
            if (requestBody == null)
                putDataWithoutRequestBodyUsingCoroutine(endPoint, headers, queryParams)
            else putDataUsingCoroutine(endPoint, requestBody, headers, queryParams)
        }

        val response = retrofitResult.getOrNull()

        return if (retrofitResult.isSuccess && response != null) {
            Gson().fromJson(validateServerResponse(response), T::class.java)
        } else {
            Gson().fromJson(
                getServerUnhandledError(NO_SERVICE_RESPONSE_ERROR_CODE),
                T::class.java
            )
        }
    }

    suspend inline fun <reified T : Any> deleteDataHandler(
        baseUrl: String,
        endPoint: String,
        headers: Map<String, String>? = null,
        queryParams: Map<String, String>? = null
    ): T {
        if (!isNetworkConnected()) {
            return Gson().fromJson(getNoNetworkError(), T::class.java)
        }

        val retrofitResult = networkClient.getNetworkService(baseUrl).runCatching {
            deleteDataUsingCoroutine(endPoint, headers, queryParams)
        }

        val response = retrofitResult.getOrNull()

        return if (retrofitResult.isSuccess && response != null) {
            Gson().fromJson(validateServerResponse(response), T::class.java)
        } else {
            Gson().fromJson(
                getServerUnhandledError(NO_SERVICE_RESPONSE_ERROR_CODE),
                T::class.java
            )
        }
    }

    suspend inline fun <reified T : Any> uploadFileDataHandler(
        baseUrl: String,
        endPoint: String,
        filePath: File,
        headers: Map<String, String>? = null
    ): T {
        if (!isNetworkConnected()) {
            return Gson().fromJson(getNoNetworkError(), T::class.java)
        }

        val retrofitResult = networkClient.getNetworkService(baseUrl).runCatching {
            val fileUploadRequest = createFileRequest(filePath)
            fileUploadUsingCoroutine(endPoint, fileUploadRequest, headers)
        }

        val response = retrofitResult.getOrNull()

        return if (retrofitResult.isSuccess && response != null) {
            Gson().fromJson(validateServerResponse(response), T::class.java)
        } else {
            Gson().fromJson(
                getServerUnhandledError(NO_SERVICE_RESPONSE_ERROR_CODE),
                T::class.java
            )
        }
    }

    fun validateServerResponse(response: Response<String>): String {
        return if (response.code() == NO_CONTENT_RESPONSE_CODE || (response.isSuccessful && response.body()?.isNullOrEmpty() == true)) {
            return getNoContentMessage(response.code())
        } else if (response.isSuccessful && response.body()?.isNullOrEmpty() == false) {
            return response.body() ?: getNoContentMessage()
        } else if (response.errorBody() != null) {
            // All 400, 500 sequence with proper error messages will be handled here
            val errorResponseBody = response.errorBody()?.string() ?: ""
            if (isValidJsonData(errorResponseBody)) {
                // add in the response error code for the BaseNetworkResponse if the error body is valid
                // and does not contain the status field
                val jsonData = JsonParser.parseString(errorResponseBody)
                if (jsonData.asJsonObject.get("status") == null) {
                    val jsonObject = jsonData.asJsonObject
                    jsonObject.addProperty("status", response.code())
                    Gson().toJson(jsonObject)
                } else {
                    errorResponseBody
                }
            } else {
                getServerUnhandledError(response.code())
            }
        } else {
            // Default error message
            getServerUnhandledError(response.code())
        }
    }

    fun getServerUnhandledError(errorCode: Int): String {
        return Gson().toJson(NetworkErrorModel(errorCode, NETWORK_FAILURE_DEFAULT))
    }

    fun getNoNetworkError(): String {
        return Gson().toJson(NetworkErrorModel(NO_NETWORK_ERROR_CODE, NO_NETWORK_ERROR_MESSAGE))
    }

    fun isNetworkConnected(): Boolean {
        return networkMonitor.isNetworkConnected()
    }

    fun isValidJsonData(responseBody: String): Boolean {
        return try {
            if (JsonParser.parseString(responseBody).isJsonObject) {
                true
            } else {
                JsonParser.parseString(responseBody).isJsonArray || JsonParser.parseString(responseBody).asBoolean
            }
        } catch (exception: Exception) {
            false
        }
    }

    fun getNoContentMessage(code: Int = NO_CONTENT_RESPONSE_CODE): String {
        return Gson().toJson(NetworkErrorModel(code, NO_CONTENT_MESSAGE))
    }
}
