package com.pavelkrylov.vsafe.feautures.products

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ProductsVMFactory(val groupId: Long) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T = ProductsVM(groupId) as T
}

class ProductsVM(val groupId: Long) : ViewModel() {
    val loadingLD = MutableLiveData<Boolean>()
    val firstProductsLD = MutableLiveData<List<UIProduct>>(emptyList())
    val addProductsLD = MutableLiveData<List<UIProduct>>()
    val showNoProductsLD = MutableLiveData<Boolean>(false)

    val presenter = ProductsPresenter(this)

    override fun onCleared() {
        super.onCleared()
        presenter.onDestroy()
    }
}