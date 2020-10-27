package com.pavelkrylov.vsafe.feautures.product_info

import androidx.lifecycle.Observer
import androidx.lifecycle.viewModelScope
import com.pavelkrylov.vsafe.base.BasePresenter
import com.pavelkrylov.vsafe.logic.CartStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProductInfoPresenter(val vm: ProductInfoVM) : BasePresenter() {

    val interactor = ProductInfoInteractor(vm.productId, vm.groupId)

    private val cartCountObserver = Observer<Int> {
        updateCart()
    }

    private fun updateCart() {
        val count = vm.cartCount.value!!
        GlobalScope.launch {
            CartStorage.setCount(vm.groupId, vm.productId.toString(), count)
        }
    }

    init {
        interactor.loadInfo()?.observeForever {
            vm.viewModelScope.launch {
                val cart = withContext(Dispatchers.IO) {
                    CartStorage.getCart(vm.groupId)
                }
                vm.cartCount.value = cart.countMap.getOrElse(vm.productId.toString(), { 0 })
                vm.productInfoLD.value = it
            }
        }
        vm.cartCount.observeForever(cartCountObserver)
    }

    fun addToCartClicked() {
        vm.cartCount.value = 1
    }

    fun plusBtnClicked() {
        vm.cartCount.value = vm.cartCount.value!! + 1
    }

    fun minusBtnClicked() {
        vm.cartCount.value = vm.cartCount.value!! - 1
    }

    fun onDestroy() {
        interactor.stop()
        vm.cartCount.removeObserver(cartCountObserver)
    }
}