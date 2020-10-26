package com.pavelkrylov.vsafe.feautures.stores

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class StoresVM : ViewModel() {
    val loadingLD = MutableLiveData<Boolean>()
    val firstStoresLD = MutableLiveData<List<UIStore>>()
    val addStoresLD = MutableLiveData<List<UIStore>>()

    val presenter = StoresPresenter(this)
}