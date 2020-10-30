package com.pavelkrylov.vsafe.feautures.order_details

data class UIOrderFooter(val totalPrice: String, val address: String, val comment: String) :
    IOrderDetailsItem {
    override val id = "footer"
}