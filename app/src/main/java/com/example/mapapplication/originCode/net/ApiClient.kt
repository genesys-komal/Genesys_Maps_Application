package com.example.mapapplication.originCode.net

import com.example.mapapplication.originCode.net.auth.ApiKeyAuth
import com.example.mapapplication.originCode.net.infrastructure.Serializer
import com.example.mapapplication.originCode.net.infrastructure.TimeoutInterceptor
import com.google.gson.GsonBuilder
import okhttp3.Call
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.CallAdapter
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

internal class ApiClient(
    private var baseUrl: String = "",
    private val okHttpClientBuilder: OkHttpClient.Builder? = null,
    private val serializerBuilder: GsonBuilder = Serializer.gsonBuilder,
    private val callFactory: Call.Factory? = null,
    private val callAdapterFactories: List<CallAdapter.Factory> = listOf(),
    private val converterFactories: List<Converter.Factory> = listOf(
        GsonConverterFactory.create(serializerBuilder.create()),
    )
) {
    private val apiAuthorizations = mutableMapOf<String, Interceptor>()
    var logger: ((String) -> Unit)? = null

    private val retrofitBuilder: Retrofit.Builder by lazy {
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .apply {
                callAdapterFactories.forEach {
                    addCallAdapterFactory(it)
                }
            }
            .apply {
                converterFactories.forEach {
                    addConverterFactory(it)
                }
            }
    }

    private val clientBuilder: OkHttpClient.Builder by lazy {
        okHttpClientBuilder ?: defaultClientBuilder
    }

    private val defaultClientBuilder: OkHttpClient.Builder by lazy {
        OkHttpClient()
            .newBuilder()
            .addInterceptor(TimeoutInterceptor())
    }

    init {
        normalizeBaseUrl()
    }

    constructor(
        baseUrl: String = "",
        okHttpClientBuilder: OkHttpClient.Builder? = null,
        serializerBuilder: GsonBuilder = Serializer.gsonBuilder,
        authNames: Array<String>
    ) : this(baseUrl, okHttpClientBuilder, serializerBuilder) {
        authNames.forEach { authName ->
            val auth: Interceptor = when (authName) {
                "api_key" -> ApiKeyAuth("query", "api_key")
                else -> throw RuntimeException("auth name $authName not found in available auth names")
            }

            addAuthorization(authName, auth)
        }
    }

    /**
     * Adds an authorization to be used by the client
     * @param authName Authentication name
     * @param authorization Authorization interceptor
     * @return ApiClient
     */
    fun addAuthorization(authName: String, authorization: Interceptor): ApiClient {
        if (apiAuthorizations.containsKey(authName)) {
            throw RuntimeException("auth name $authName already in api authorizations")
        }

        apiAuthorizations[authName] = authorization
        clientBuilder.addInterceptor(authorization)
        return this
    }

    fun setLogger(logger: (String) -> Unit): ApiClient {
        this.logger = logger
        return this
    }

    fun <S> createService(serviceClass: Class<S>): S {
        val usedCallFactory = this.callFactory ?: clientBuilder.build()
        return retrofitBuilder.callFactory(usedCallFactory).build().create(serviceClass)
    }

    private fun normalizeBaseUrl() {
        if (!baseUrl.endsWith("/")) {
            baseUrl += "/"
        }
    }
}