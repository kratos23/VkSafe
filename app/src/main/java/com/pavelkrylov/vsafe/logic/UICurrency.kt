package com.pavelkrylov.vsafe.logic


data class UICurrency(val id: Int, val name: String, val sign: String?)

fun formatPrice(price: Long, currency: UICurrency): String {
    var amountS = price.toString().reversed().chunked(3).reversed()
        .joinToString(" ") { it.reversed() }
    amountS += " "
    if (currency.sign != null) {
        amountS += currency.sign
    } else {
        amountS += currency.name
    }
    return amountS
}