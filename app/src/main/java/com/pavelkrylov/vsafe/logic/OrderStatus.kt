package com.pavelkrylov.vsafe.logic

enum class OrderStatus {
    CREATED,
    CONFIRMED,
    DISPUTE,
    CANCELED,
    CLOSED,
    PAID
}

fun String.toOrderStatus(): OrderStatus? {
    val s = this
    return OrderStatus.values().firstOrNull { it.name == s }
}
