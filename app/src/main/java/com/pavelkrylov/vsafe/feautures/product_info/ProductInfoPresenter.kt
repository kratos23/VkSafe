package com.pavelkrylov.vsafe.feautures.product_info

import com.pavelkrylov.vsafe.base.BasePresenter

class ProductInfoPresenter(val vm : ProductInfoVM) : BasePresenter() {

    val interactor = ProductInfoInteractor(vm.productId, vm.groupId)

    init {
        interactor.loadInfo()?.observeForever {
            vm.productInfoLD.value = it
            vm.addedToFavorite.value = it.isFavorite
        }
    }

    fun onDestroy() {
        interactor.stop()
    }

    fun favoriteBtnClicked() {
        vm.addedToFavorite.value = !(vm.addedToFavorite.value!!)
        if (vm.addedToFavorite.value!!) {
            interactor.addToFavorite()
        } else {
            interactor.removeFromFavorites()
        }
    }
}