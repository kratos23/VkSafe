package com.pavelkrylov.vsafe.feautures.order_details

import androidx.annotation.ColorInt

data class UIStatusItem(
    val statusName: String,
    val description: String,
    @ColorInt
    val bgColor: Int
) : IOrderDetailsItem {
    override val id = "details_status"
}