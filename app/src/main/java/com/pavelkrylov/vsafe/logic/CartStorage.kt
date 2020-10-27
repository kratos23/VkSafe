package com.pavelkrylov.vsafe.logic

import androidx.annotation.MainThread
import com.pavelkrylov.vsafe.App
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.util.*
import kotlin.collections.HashMap
import kotlin.math.abs

@MainThread
object CartStorage {


    private val cartByGroupId = HashMap<Long, Cart>()
    private val groupCartsMutexes = HashMap<Long, Mutex>()

    private fun getCartFile(groupId: Long): File {
        val cartDir = File(App.INSTANCE.filesDir, "/carts")
        cartDir.mkdirs()
        val cartFileName = "${abs(groupId)}.json"
        val cartFile = File(cartDir, cartFileName)
        cartFile.createNewFile()
        return cartFile
    }

    private fun loadCartFromFile(groupId: Long): Cart {
        val jsonS = getCartFile(groupId).readText()
        if (jsonS.isEmpty()) {
            return Cart()
        } else {
            return Json.decodeFromString(jsonS)
        }
    }

    private fun saveCartToFile(groupId: Long) {
        val cart = cartByGroupId.getOrElse(groupId, { Cart() })
        val jsonS = Json.encodeToString(cart)
        val cartFile = getCartFile(groupId)
        cartFile.writeText(jsonS)
    }

    private suspend fun getGroupCart(groupId: Long): Cart {
        return withContext(Dispatchers.Main) {
            if (groupId !in cartByGroupId) {
                val mutex = groupCartsMutexes.getOrPut(groupId, { Mutex() })
                val cart = withContext(Dispatchers.IO) {
                    mutex.withLock {
                        loadCartFromFile(groupId)
                    }
                }
                cartByGroupId[groupId] = cart
                cart
            } else {
                cartByGroupId[groupId]!!
            }
        }
    }

    @Suppress("NAME_SHADOWING")
    suspend fun setCount(groupId: Long, productId: String, count: Int) {
        val groupId = abs(groupId)
        val groupCart = getGroupCart(groupId)
        groupCart.countMap[productId] = count
        val mutex = groupCartsMutexes.getOrPut(groupId, { Mutex() })
        withContext(Dispatchers.IO) {
            mutex.withLock {
                saveCartToFile(groupId)
            }
        }
    }

    suspend fun getCart(groupId: Long): Cart {
        val groupCart = getGroupCart(groupId)
        return groupCart.copy(countMap = TreeMap(groupCart.countMap))
    }
}