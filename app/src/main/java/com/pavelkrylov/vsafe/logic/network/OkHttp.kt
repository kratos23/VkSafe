package com.pavelkrylov.vsafe.logic.network

import okhttp3.OkHttpClient

object OkHttp {
    val client: OkHttpClient

    var onInvalidTokenListener = {}

    init {
        client = OkHttpClient.Builder()
            .addInterceptor(TokenInterceptor { onInvalidTokenListener() })
            .build()
    }

    const val BASE_URL = "http://10.110.196.52:8080"
    const val MARKETS_URL = "$BASE_URL/markets"
}