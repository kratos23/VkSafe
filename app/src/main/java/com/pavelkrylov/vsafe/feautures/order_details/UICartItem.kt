package com.pavelkrylov.vsafe.feautures.order_details

data class UICartItem(
    val productId: String,
    val photoUrl: String,
    val productName: String,
    val amount: Int,
    val price: String
) : IOrderDetailsItem {
    override val id = "cart_item_$productId"
}