package com.pavelkrylov.vsafe.feautures.products

import androidx.lifecycle.LifecycleCoroutineScope
import com.pavelkrylov.vsafe.App
import com.pavelkrylov.vsafe.base.BasePresenter
import com.pavelkrylov.vsafe.base.Screens
import com.pavelkrylov.vsafe.logic.CartStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProductsPresenter(val productsVM: ProductsVM) : BasePresenter() {
    val interactor = ProductsInteractor(productsVM.groupId)

    var productsEnd = false

    init {
        onLoadMore()
        interactor.endLD.observeForever {
            productsEnd = it
            if (it && productsVM.firstProductsLD.value?.isEmpty() == true) {
                productsVM.showNoProductsLD.value = true
            }
        }
    }

    fun onViewCreated(scope: LifecycleCoroutineScope) {
        scope.launch {
            val cart = withContext(Dispatchers.IO) {
                CartStorage.getCart(productsVM.groupId)
            }
            if (cart.isEmpty()) {
                productsVM.cartPrice.value = null
            } else {
                productsVM.cartPrice.value = cart.getTotalPrice()
            }
        }
    }

    fun onDestroy() {
        interactor.stop()
    }


    fun onLoadMore() {
        if (productsEnd) {
            return
        }
        productsVM.loadingLD.value = true
        interactor.loadMoreProducts()?.observeForever {
            productsVM.loadingLD.value = false
            val list = ArrayList<UIProduct>(productsVM.firstProductsLD.value ?: emptyList())
            list.addAll(it)
            productsVM.firstProductsLD.value = list
            productsVM.addProductsLD.value = it
        }
    }


    fun productSelected(product: UIProduct) {
        App.INSTANCE.getRouter().navigateTo(
            Screens.ProductInfoScreen(
                product.id,
                productsVM.groupId, product.name
            )
        )
    }
}