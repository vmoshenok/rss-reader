package com.example.freshrssreader.data.api

import com.example.freshrssreader.data.repository.AuthRepository
import kotlinx.coroutines.runBlocking
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class DynamicBaseUrlInterceptor @Inject constructor(
    private val authRepository: AuthRepository
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val serverUrl = runBlocking { authRepository.getServerUrl() }

        if (serverUrl == null) {
            return chain.proceed(originalRequest)
        }

        val baseUrl = serverUrl.trimEnd('/') + "/"
        val newBaseUrl = baseUrl.toHttpUrlOrNull() ?: return chain.proceed(originalRequest)

        val originalUrl = originalRequest.url
        // Replace only the scheme, host, port, and base path
        val pathSegments = originalUrl.encodedPath.trimStart('/')
        val newUrl = newBaseUrl.newBuilder()
            .encodedPath(newBaseUrl.encodedPath.trimEnd('/') + "/" + pathSegments)
            .encodedQuery(originalUrl.encodedQuery)
            .build()

        val newRequest = originalRequest.newBuilder()
            .url(newUrl)
            .build()

        return chain.proceed(newRequest)
    }
}
