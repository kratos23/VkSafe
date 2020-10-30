package com.pavelkrylov.vsafe.feautures.orders

data class UIOrder(
    val id: Long,
    val displayName: String,
    val photoUrl: String,
    val price: String,
    val status: String
) : IOrderItem {
    override val itemId: String
        get() = "order$id"
}