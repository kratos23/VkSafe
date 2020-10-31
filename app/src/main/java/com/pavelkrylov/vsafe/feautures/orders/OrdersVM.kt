package com.pavelkrylov.vsafe.feautures.orders

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pavelkrylov.vsafe.App
import com.pavelkrylov.vsafe.base.Screens
import com.pavelkrylov.vsafe.base.retry
import com.pavelkrylov.vsafe.logic.network.OkHttp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Request
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.roundToLong

class OrdersVM(val isCustomer: Boolean) : ViewModel() {
    val ordersLD = MutableLiveData<List<UIOrder>?>(null)

    @Suppress("BlockingMethodInNonBlockingContext")
    private suspend fun getCustomerOrders(): List<UIOrder> {
        val client = OkHttp.client
        val req = Request.Builder()
            .url(OkHttp.CUSTOMER_ORDERS_URL)
            .get()
            .build()
        return retry {
            val result = ArrayList<UIOrder>()
            val resp = client.newCall(req).execute()
            val json = JSONObject(resp.body!!.string())
            val groupsJSON = json.getJSONObject("groups").getJSONArray("response")
            val groupMap = mutableMapOf<Long, JSONObject>()
            for (i in 0 until groupsJSON.length()) {
                val groupJSON = groupsJSON.getJSONObject(i)
                val groupId = groupJSON.getLong("id")
                groupMap[groupId] = groupJSON
            }
            val ordersJson = json.getJSONArray("orders")
            for (i in 0 until ordersJson.length()) {
                val orderJSON = ordersJson.getJSONObject(i)
                val priceS = String.format(
                    Locale.getDefault(),
                    "%d",
                    (orderJSON.getLong("price").toDouble() / 100.0).roundToLong()
                )
                val groupJSON = groupMap[orderJSON.getLong("groupId")]!!
                val uiOrder = UIOrder(
                    id = orderJSON.getLong("id"),
                    displayName = groupJSON.getString("name"),
                    photoUrl = groupJSON.getString("photo_200"),
                    price = "$priceS ₽",
                    status = orderJSON.getString("status")
                )
                result.add(uiOrder)
            }
            resp.close()
            result
        }
    }

    private suspend fun getStoreOrders(): List<UIOrder> {
        val client = OkHttp.client
        val req = Request.Builder()
            .url(OkHttp.MARKET_ORDERS_URL)
            .get()
            .build()
        return retry {
            val result = ArrayList<UIOrder>()
            val resp = client.newCall(req).execute()
            val json = JSONObject(resp.body!!.string())
            val usersJSON = json.getJSONObject("groups").getJSONArray("response")
            val usersMap = mutableMapOf<Long, JSONObject>()
            for (i in 0 until usersJSON.length()) {
                val userJSON = usersJSON.getJSONObject(i)
                val groupId = userJSON.getLong("id")
                usersMap[groupId] = userJSON
            }
            val ordersJson = json.getJSONArray("orders")
            for (i in 0 until ordersJson.length()) {
                val orderJSON = ordersJson.getJSONObject(i)
                val priceS = String.format(
                    Locale.getDefault(),
                    "%d",
                    (orderJSON.getLong("price").toDouble() / 100.0).roundToLong()
                )
                val userJSON = usersMap[orderJSON.getLong("clientId")]!!
                val uiOrder = UIOrder(
                    id = orderJSON.getLong("id"),
                    displayName = userJSON.getString("first_name") + " " + userJSON.getString("last_name"),
                    photoUrl = userJSON.getString("photo_200"),
                    price = "$priceS ₽",
                    status = orderJSON.getString("status")
                )
                result.add(uiOrder)
            }
            resp.close()
            result
        }
    }

    private var updating = false

    private fun updateOrders() {
        if (updating) {
            return
        }
        updating = true
        viewModelScope.launch {
            ordersLD.value = null
            ordersLD.value = withContext(Dispatchers.IO) {
                if (isCustomer) {
                    getCustomerOrders()
                } else {
                    getStoreOrders()
                }
            }
            updating = false
        }
    }

    private val router = App.INSTANCE.outerCicerone.router

    fun orderClicked(orderId: Long) {
        router.navigateTo(Screens.OrderDetailsScreen(isCustomer, orderId))
    }

    fun onViewCreated() {
        updateOrders()
    }

    init {
        updateOrders()
    }
}

class OrdersVMFactory(val isCustomer: Boolean) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>) = OrdersVM(isCustomer) as T
}