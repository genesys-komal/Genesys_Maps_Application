package com.example.mapapplication.originCode.helpers

import com.example.mapapplication.originCode.helpers.getErrorResponse
import com.google.gson.annotations.SerializedName
import retrofit2.Response

sealed class OperationResult<out T> {

    class ResultSuccess<T>(val result: T) : OperationResult<T>()
    class ResultError(val error: String) : OperationResult<Nothing>()

    suspend fun onSuccess(func: suspend (T) -> Unit): OperationResult<T> {
        if (this is ResultSuccess) func(result)
        return this
    }

    suspend fun onError(func: suspend (String) -> Unit): OperationResult<T> {
        if (this is ResultError) func(error)
        return this
    }
}

fun <T, R> Response<T>.toOperationResult(
    onSuccess: (T) -> R
): OperationResult<R> {
    val body = body()
//    val badMsg = "Oops something went wrong please try again"
    val badMsg = "Connection error, please try again"
    val error = try {
        getErrorResponse<ErrorResponse>()?.errorMsg ?: badMsg
    } catch (e: Exception) {
        e.message ?: badMsg
    }

    return when {
        isSuccessful && body != null -> OperationResult.ResultSuccess(onSuccess(body))
        errorBody() != null -> OperationResult.ResultError(error)
        else -> OperationResult.ResultError(badMsg)
    }
}

data class ErrorResponse(
    val statusCode: Int,
    val message: String? = null,
    val timestamp: String? = null,
    @SerializedName("error_msg")
    val errorMsg: String
)