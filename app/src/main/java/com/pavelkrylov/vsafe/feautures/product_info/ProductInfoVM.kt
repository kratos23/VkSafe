package com.pavelkrylov.vsafe.feautures.product_info

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ProductInfoVMFactory(val groupId:Long, val productId : Long) : ViewModelProvider.NewInstanceFactory(){
    override fun <T : ViewModel?> create(modelClass: Class<T>): T = ProductInfoVM(groupId, productId) as T
}

class ProductInfoVM(val groupId:Long, val productId : Long) : ViewModel() {
    val productInfoLD = MutableLiveData<UIProductInfo>()
    val cartCount = MutableLiveData<Int>()

    val presenter = ProductInfoPresenter(this)

    override fun onCleared() {
        super.onCleared()
        presenter.onDestroy()
    }
}