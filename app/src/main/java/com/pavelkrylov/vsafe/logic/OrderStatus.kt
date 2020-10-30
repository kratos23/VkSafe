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

val customerStatusText = mapOf(
    OrderStatus.PAID to "Ожидает подтверждения",
    OrderStatus.CREATED to "Ожидает оплаты",
    OrderStatus.CONFIRMED to "В процессе",
    OrderStatus.DISPUTE to "Открыт спор",
    OrderStatus.CANCELED to "Отменён",
    OrderStatus.CLOSED to "Завершён"
)

val storeStatusText = mapOf(
    OrderStatus.PAID to "Ожидает вашего подтверждения",
    OrderStatus.CREATED to "Ожидает оплаты",
    OrderStatus.CONFIRMED to "Ожидает подтвержения",
    OrderStatus.DISPUTE to "Открыт спор",
    OrderStatus.CANCELED to "Отменён",
    OrderStatus.CLOSED to "Завершён"
)


fun getStatusText(status: OrderStatus, isCustomer: Boolean): String {
    return if (isCustomer) {
        customerStatusText[status]!!
    } else {
        storeStatusText[status]!!
    }
}