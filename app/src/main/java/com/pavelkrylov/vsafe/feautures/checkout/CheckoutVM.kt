package com.pavelkrylov.vsafe.feautures.checkout

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pavelkrylov.vsafe.logic.CartStorage
import com.pavelkrylov.vsafe.logic.UICurrency
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

    fun payBtnCLicked() {
        val price = priceLD.value ?: return
        val (amount, currency) = price
    }
}

@Suppress("UNCHECKED_CAST")
class CheckoutVMFactory(val groupId: Long) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>) = CheckoutVM(groupId) as T
}