package com.pavelkrylov.vsafe.feautures.stores

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.pavelkrylov.vsafe.feautures.stores.city_select.CityInteractor

class CityVM : ViewModel() {
    val selectedCity = MutableLiveData<UICity>(CityInteractor.getSavedCity())

    init {

    }
}