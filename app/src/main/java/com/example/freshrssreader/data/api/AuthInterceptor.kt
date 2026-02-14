package com.example.freshrssreader.data.api

import com.example.freshrssreader.data.repository.AuthRepository
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val authRepository: AuthRepository
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // Don't add auth header to login requests
        if (originalRequest.url.encodedPath.contains("accounts/ClientLogin")) {
            return chain.proceed(originalRequest)
        }

        val token = runBlocking { authRepository.getAuthToken() }

        val request = if (token != null) {
            originalRequest.newBuilder()
                .header("Authorization", "GoogleLogin auth=$token")
                .build()
        } else {
            originalRequest
        }

        return chain.proceed(request)
    }
}
