package com.pavelkrylov.vsafe.logic.network

import android.os.Handler
import android.os.Looper
import okhttp3.Interceptor
import okhttp3.Response

class TokenInterceptor(val onInvalidToken: () -> Unit) : Interceptor {

    private val mainHandler = Handler(Looper.getMainLooper())

    override fun intercept(chain: Interceptor.Chain): Response {
        val oldReq = chain.request()
        val newRequest = oldReq
            .newBuilder()
            .url(
                oldReq.url
                    .newBuilder()
                    .addQueryParameter("token", VkTokenStorage.getToken())
                    .build()
            )
            .build()
        val response = chain.proceed(newRequest)
        if (response.code == 401) {
            mainHandler.post { onInvalidToken() }
        }
        return response
    }
}