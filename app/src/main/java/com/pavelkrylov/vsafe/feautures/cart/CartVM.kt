package com.pavelkrylov.vsafe.feautures.cart

import androidx.lifecycle.*
import com.pavelkrylov.vsafe.App
import com.pavelkrylov.vsafe.base.Screens
import com.pavelkrylov.vsafe.logic.CartStorage
import com.pavelkrylov.vsafe.logic.UICurrency
import com.pavelkrylov.vsafe.logic.getCurrencyShort
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

class CartVM(val groupId: Long) : ViewModel() {
    private val uiSateLd =
        MutableLiveData(CartUIState(cart = null, nextLoading = false))

    private val router = App.INSTANCE.outerCicerone.router

    private suspend fun getInitialCartList(): List<UICartItem> {
        val cart = CartStorage.getCart(groupId)
        val result = mutableListOf<UICartItem>()
        val productsDao = App.INSTANCE.db.value.productsDao()
        cart.countMap.filterValues { count -> count > 0 }
            .forEach { (productId, cnt) ->
                val productJson = JSONObject(productsDao.getProduct(productId).json)
                val priceJson = productJson.getJSONObject("price")
                val priceMicros = priceJson.getLong("amount")
                val currencyJson = priceJson.getJSONObject("currency")
                val currencyId = currencyJson.getInt("id")
                val currency = UICurrency(
                    currencyId,
                    currencyJson.getString("name"),
                    getCurrencyShort(currencyJson.getString("name"))
                )

                val uiCartItem = UICartItem(
                    id = productId,
                    count = cnt,
                    oneItemPrice = priceMicros,
                    currency = currency,
                    photoUrl = productJson.optString("thumb_photo", null),
                    productName = productJson.getString("title")
                )
                result.add(uiCartItem)
            }
        return result
    }

    init {
        viewModelScope.launch {
            val cartList = withContext(Dispatchers.IO) {
                getInitialCartList()
            }
            uiSateLd.value = uiSateLd.value?.copy(cart = cartList)
        }
    }

    fun getUiState(): LiveData<CartUIState> = uiSateLd

    fun productIncClicked(productId: String) {
        val state = uiSateLd.value ?: return
        if (state.nextLoading) {
            return
        }
        val oldProduct = state.cart?.find { it.id == productId }
        GlobalScope.launch {
            CartStorage.setCount(groupId, productId, oldProduct?.count?.plus(1) ?: 0)
        }
        val newCart = state.cart?.map { product ->
            if (product.id == productId) {
                product.copy(count = product.count + 1)
            } else {
                product
            }
        }
        uiSateLd.value = state.copy(cart = newCart)
    }

    fun productDecClicked(productId: String) {
        val state = uiSateLd.value ?: return
        if (state.nextLoading) {
            return
        }
        val oldProduct = state.cart?.find { it.id == productId }
        GlobalScope.launch {
            CartStorage.setCount(groupId, productId, oldProduct?.count?.minus(1) ?: 0)
        }
        val newCart = state.cart?.map { product ->
            if (product.id == productId) {
                product.copy(count = product.count - 1)
            } else {
                product
            }
        }?.filter { it.count > 0 }
        uiSateLd.value = state.copy(cart = newCart)
    }

    fun clearCartClicked() {
        val state = uiSateLd.value ?: return
        if (state.nextLoading) {
            return
        }
        state.cart?.forEach { product ->
            GlobalScope.launch {
                CartStorage.setCount(groupId, product.id, 0)
            }
        }
        uiSateLd.value = state.copy(cart = emptyList())
    }

    fun nextBtnClicked() {

    }

    fun productClicked(productId: String, productName: String) {
        router.navigateTo(Screens.ProductInfoScreen(productId.toLong(), groupId, productName))
    }

    fun returnToMarketBtnClicked() {
        router.backTo(Screens.ProductsScreen(groupId, ""))
    }
}

@Suppress("UNCHECKED_CAST")
class CartVMFactory(val groupId: Long) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>) = CartVM(groupId) as T
}