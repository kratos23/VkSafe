package com.pavelkrylov.vsafe.feautures.product_info

import com.pavelkrylov.vsafe.logic.UICurrency

data class UIProductInfo(
    val id: Long, val description: String?, val photoUrl: String,
    val price: Long, val priceCurrency: UICurrency, val name : String, val isFavorite:Boolean
)