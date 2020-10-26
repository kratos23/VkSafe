package com.pavelkrylov.vsafe.feautures.stores.city_select

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.pavelkrylov.vsafe.feautures.stores.UICity

class CitySelectVM : ViewModel() {
    val citiesLD = MutableLiveData<List<UICity>>()

    val citiesPresenter = CitySelectPresenter(this)
}