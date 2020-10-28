package com.pavelkrylov.vsafe.feautures.cart

import com.pavelkrylov.vsafe.logic.UICurrency

data class UICartItem(
    override val id: String,
    val count: Int,
    val oneItemPrice: Long,
    val currency: UICurrency,
    val photoUrl: String?,
    val productName: String
) : CartItem