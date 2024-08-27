package com.example.mapapplication.originCode.net.infrastructure

import android.util.Log
import com.example.mapapplication.originCode.helpers.ErrorResponse
import com.google.gson.Gson
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody
import java.net.SocketTimeoutException

class TimeoutInterceptor() : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        return try {
            chain.proceed(chain.request())
        } catch (e: SocketTimeoutException) {
            val errorMessage = "Connection timed out: ${e.message}"
            Log.e("TimeoutInterceptor", errorMessage)

            // Create ErrorResponse object
            val errorResponse = ErrorResponse(
                statusCode = 408,
                errorMsg = "Request timed out"
            )

            // Serialize ErrorResponse object to JSON
            val errorResponseJson = Gson().toJson(errorResponse)

            Response.Builder()
                .request(chain.request())
                .protocol(Protocol.HTTP_1_1)
                .code(408) // HTTP status code for Request Timeout
//                .message("Request timed out")
                .message("Connection issue, please try again")
                .body(
                    ResponseBody.create("application/json".toMediaTypeOrNull(), errorResponseJson)
                )
                .build()
        }
    }
}