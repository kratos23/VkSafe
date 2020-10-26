package com.pavelkrylov.vsafe.feautures.products

import com.pavelkrylov.vsafe.App
import com.pavelkrylov.vsafe.base.BasePresenter
import com.pavelkrylov.vsafe.base.Screens

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
        App.instance.getRouter().navigateTo(
            Screens.ProductInfoScreen(
                product.id,
                productsVM.groupId, product.name
            )
        )
    }
}