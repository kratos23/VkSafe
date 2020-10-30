package com.pavelkrylov.vsafe.feautures.order_details

data class UIContactItem(
    val msgToId: Long,
    val displayName: String,
    val photoUrl: String,
    val contactActionText: String
) : IOrderDetailsItem {
    override val id = "order_details_contact"
}