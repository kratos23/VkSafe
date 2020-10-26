package com.pavelkrylov.vsafe.feautures.products

import com.pavelkrylov.vsafe.logic.UICurrency


data class UIProduct(
    val name: String, val price: Long, val priceCurrency: UICurrency,
    val photoUrl: String, val id: Long
)