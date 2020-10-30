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

    const val BASE_URL = "http://192.168.68.105:8080"
    const val MARKETS_URL = "$BASE_URL/markets"
    const val NEW_ORDER_URL = "$BASE_URL/orders/new"
    const val CUSTOMER_ORDERS_URL = "$BASE_URL/customer/orders"
    const val ORDER_DETAILS_URL = "$BASE_URL/order"
}