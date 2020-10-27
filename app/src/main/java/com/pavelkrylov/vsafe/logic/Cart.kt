package com.pavelkrylov.vsafe.logic

import com.pavelkrylov.vsafe.App
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import okhttp3.internal.toImmutableMap
import org.json.JSONObject
import java.util.*

@Serializable
data class Cart(val countMap: MutableMap<String, Int> = TreeMap()) {
    fun isEmpty(): Boolean {
        for ((id, count) in countMap) {
            if (count > 0) {
                return false
            }
        }
        return true
    }

    suspend fun getTotalPrice(): Long {
        val currentMap = countMap.toImmutableMap()
        var totalPrice = 0L
        currentMap.forEach { (productId, amount) ->
            val productJson = withContext(Dispatchers.IO) {
                val productsDao = App.INSTANCE.db.value.productsDao()
                JSONObject(productsDao.getProduct(productId).json)
            }
            val priceJson = productJson.getJSONObject("price")
            val priceMicros = priceJson.getLong("amount")
            totalPrice += priceMicros * amount
        }
        return totalPrice
    }
}