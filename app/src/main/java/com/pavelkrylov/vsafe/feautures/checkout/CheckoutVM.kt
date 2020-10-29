package com.pavelkrylov.vsafe.feautures.checkout

import android.content.Intent
import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pavelkrylov.vsafe.App
import com.pavelkrylov.vsafe.base.Screens
import com.pavelkrylov.vsafe.base.retry
import com.pavelkrylov.vsafe.logic.CartStorage
import com.pavelkrylov.vsafe.logic.UICurrency
import com.pavelkrylov.vsafe.logic.network.OkHttp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

class CheckoutVM(val groupId: Long) : ViewModel() {
    val priceLD = MutableLiveData<Pair<Long, UICurrency>?>(null)

    init {
        viewModelScope.launch {
            val totalPrice = withContext(Dispatchers.IO) {
                CartStorage.getCart(groupId).getTotalPrice()
            }
            priceLD.value = totalPrice
        }
    }


    @Suppress("BlockingMethodInNonBlockingContext")
    suspend fun createNewOrder(comment: String, address: String): Long? {
        val body = JSONObject().apply {
            val cart = CartStorage.getCart(groupId)
            val products = cart.countMap.map { (id, _) ->
                val productsDao = App.INSTANCE.db.value.productsDao()
                JSONObject(productsDao.getProduct(id).json)
            }
            val productsArray = JSONArray()
            products.forEach { productsArray.put(it) }
            put("cart", Json.encodeToString(cart))
            put("products", products.toString())
            put("groupId", groupId)
            put("comment", comment)
            put("address", address)
        }.toString().toRequestBody("application/json".toMediaType())
        val req = Request.Builder()
            .url(OkHttp.NEW_ORDER_URL)
            .post(body)
            .build()
        val client = OkHttp.client
        return retry {
            val resp = client.newCall(req).execute()
            val bodyS = resp.body!!.string()
            val json = JSONObject(bodyS)
            val id = json.optLong("orderId", -1)
            if (id == -1L) {
                null
            } else {
                id
            }
        }
    }

    var orderCreating = false

    fun payBtnCLicked(comment: String, address: String) {
        viewModelScope.launch {
            if (!orderCreating) {
                orderCreating = true
                kotlin.runCatching {
                    val orderId = withContext(Dispatchers.IO) {
                        createNewOrder(comment, address)
                    }
                    if (orderId != null) {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(GROUP_MESSAGE_URL))
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        App.INSTANCE.startActivity(intent)

                        delay(5000)
                        App.INSTANCE.getRouter().backTo(Screens.MainCustomerScreen())
                        App.INSTANCE.getRouter().navigateTo(Screens.CheckoutSuccessScreen())
                    }
                }
                orderCreating = false
            }
        }
    }

    companion object {
        const val GROUP_MESSAGE_URL = "https://vk.com/im?media=&sel=-199853701"
    }
}

@Suppress("UNCHECKED_CAST")
class CheckoutVMFactory(val groupId: Long) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>) = CheckoutVM(groupId) as T
}