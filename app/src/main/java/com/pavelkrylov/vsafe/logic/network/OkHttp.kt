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

    const val BASE_URL = "http://185.104.113.150:8080"
    const val MARKETS_URL = "$BASE_URL/markets"
    const val NEW_ORDER_URL = "$BASE_URL/orders/new"
    const val CUSTOMER_ORDERS_URL = "$BASE_URL/customer/orders"
    const val ORDER_DETAILS_URL = "$BASE_URL/order"
    const val ORDER_BUTTONS_URL = "$BASE_URL/order/status"
    const val USER_MARKETS_URL = "$BASE_URL/user/stores"
    const val MARKET_ORDERS_URL = "$BASE_URL/store/orders"
}