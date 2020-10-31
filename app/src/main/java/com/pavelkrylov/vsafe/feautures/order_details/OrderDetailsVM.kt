package com.pavelkrylov.vsafe.feautures.order_details

import android.graphics.Color
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pavelkrylov.vsafe.base.retry
import com.pavelkrylov.vsafe.logic.OrderStatus
import com.pavelkrylov.vsafe.logic.getStatusText
import com.pavelkrylov.vsafe.logic.network.OkHttp
import com.pavelkrylov.vsafe.logic.toOrderStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.roundToLong

class OrderDetailsVM(val isCustomer: Boolean, val orderId: Long) : ViewModel() {
    val state = MutableLiveData(UIState(orderId, null))

    init {
        updateStatus()
    }

    private val bgStatusColor = mapOf(
        OrderStatus.PAID to Color.parseColor("#5CFFA000"),
        OrderStatus.CREATED to Color.parseColor("#5C6D7885"),
        OrderStatus.CONFIRMED to Color.parseColor("#5C3F8AE0"),
        OrderStatus.DISPUTE to Color.parseColor("#5CEB4250"),
        OrderStatus.CLOSED to Color.parseColor("#5C4BB34B"),
        OrderStatus.CANCELED to Color.parseColor("#5C857250")
    )

    private val customerStatusDescription = mapOf(
        OrderStatus.PAID to """Заказ успешно оплачен и ожидает подтверждения от магазина. Если магазин не подтвердит заказ в течении 24 часов с момента оплаты, мы вернём вам деньги. Пока магазин не подтвердил заказ, вы можете отменить заказ.""",
        OrderStatus.CREATED to """Заказ ожидает вашей оплаты. Оплата осуществляется через нашего чат-бота ВКонтакте. Перейти к боту можно при помощи кнопки внизу экрана.""",
        OrderStatus.CONFIRMED to """Магазин подтвердил отправку заказа. После получения заказа, пожалуйста подтвердите получение кнопкой внизу экрана. Магазин получит деньги только после вашего подтверждения.""",
        OrderStatus.DISPUTE to """По вашему заказу был открыт спор. В ближайшее время, наш сервис свяжется с вами для того, что бы разобраться в ситуации. Вы можете написать нам первым.""",
        OrderStatus.CLOSED to """Заказ успешно доставлен.""",
        OrderStatus.CANCELED to """Заказ был отменён. В скором времени деньги будут возвращены на ваш VK Pay."""
    )


    private fun getStatusDescription(status: OrderStatus): String {
        if (isCustomer) {
            return customerStatusDescription[status]!!
        } else {
            return status.toString() // TODO
        }
    }

    private fun buildList(json: JSONObject): List<IOrderDetailsItem> {
        val orderJSON = json.getJSONObject("order")

        fun statusSection(): UIStatusItem {
            val status = orderJSON.getString("status").toOrderStatus()!!
            return UIStatusItem(
                bgColor = bgStatusColor[status]!!, statusName = getStatusText(status, isCustomer),
                description = getStatusDescription(status)
            )
        }

        fun contactSection(): UIContactItem {
            if (!isCustomer) {
                val userJSON =
                    JSONObject(json.getString("user")).getJSONArray("response").getJSONObject(0)
                val displayName =
                    userJSON.optString("first_name") + " " + userJSON.optString("last_name")
                return UIContactItem(
                    displayName = displayName,
                    photoUrl = userJSON.getString("photo_200"),
                    contactActionText = "Покупатель",
                    msgToId = userJSON.getLong("id")
                )
            } else {
                val groupJSON =
                    JSONObject(json.getString("group")).getJSONArray("response").getJSONObject(0)
                return UIContactItem(
                    msgToId = -groupJSON.getLong("id"),
                    displayName = groupJSON.getString("name"),
                    contactActionText = "Магазин",
                    photoUrl = groupJSON.getString("photo_200")
                )
            }
        }

        fun productsSection(): List<UICartItem> {
            val products = JSONArray(orderJSON.getString("productsJSON"))
            val productById = mutableMapOf<Long, JSONObject>()
            for (i in 0 until products.length()) {
                val productJSON = products.getJSONObject(i)
                productById[productJSON.getLong("id")] = productJSON
            }
            val cart = JSONObject(orderJSON.getString("cartJSON")).getJSONObject("countMap")
            val result = mutableListOf<UICartItem>()
            for (productId in cart.keys()) {
                val productJSON = productById[productId.toLong()]!!
                val amount = cart.getInt(productId)
                if (amount > 0) {
                    val uiCartItem = UICartItem(
                        productId = productId,
                        photoUrl = productJSON.getString("thumb_photo"),
                        productName = productJSON.getString("title"),
                        amount = amount,
                        price = productJSON.getJSONObject("price").getString("text")
                    )
                    result.add(uiCartItem)
                }
            }
            return result
        }

        fun footerSection(): UIOrderFooter {
            val totalPrice = "${(orderJSON.getLong("price") / 100.0).roundToLong()} ₽"
            return UIOrderFooter(
                totalPrice = totalPrice,
                address = orderJSON.getString("address"),
                comment = orderJSON.getString("comment")
            )
        }

        return listOf(statusSection()) + listOf(contactSection()) + productsSection() + footerSection()
    }


    @Suppress("BlockingMethodInNonBlockingContext")
    private suspend fun loadStatus() {
        withContext(Dispatchers.Main) {
            state.value = state.value?.copy(orderInfo = null)
        }
        val client = OkHttp.client
        val req = Request.Builder()
            .url(
                OkHttp.ORDER_DETAILS_URL
                    .toHttpUrl()
                    .newBuilder()
                    .addQueryParameter("orderId", orderId.toString())
                    .build()
            )
            .get()
            .build()
        val (list, status) = retry {
            val resp = client.newCall(req).execute()
            val json = JSONObject(resp.body!!.string())
            val orderJSON = json.getJSONObject("order")
            buildList(json) to orderJSON.getString("status").toOrderStatus()!!
        }
        withContext(Dispatchers.Main) {
            state.value = state.value?.copy(orderInfo = OrderInfo(status, list))
        }
    }

    var btnSending = false

    @Suppress("BlockingMethodInNonBlockingContext")
    fun btnPressed(btnId: String) {
        if (btnSending) {
            return
        }
        viewModelScope.launch {
            btnSending = true
            val oldStatus = state.value?.orderInfo?.orderStatus
            state.value = state.value?.copy(orderInfo = null)
            withContext(Dispatchers.IO) {
                retry {
                    val bodyJSON = JSONObject().apply {
                        put("from", oldStatus.toString())
                        put("orderId", orderId)
                        put("btn", btnId)
                    }.toString().toRequestBody("application/json".toMediaType())
                    val req = Request.Builder()
                        .url(OkHttp.ORDER_BUTTONS_URL)
                        .post(bodyJSON)
                        .build()

                    OkHttp.client.newCall(req).execute().close()
                }
            }
            btnSending = false
            updateStatus()
        }
    }

    var updating = false

    fun updateStatus() {
        if (!updating) {
            updating = true
            viewModelScope.launch {
                withContext(Dispatchers.IO) {
                    loadStatus()
                }
                updating = false
            }
        }
    }
}

@Suppress("UNCHECKED_CAST")
class OrderDetailsVMFactory(val isCustomer: Boolean, val orderId: Long) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>) =
        OrderDetailsVM(isCustomer, orderId) as T
}