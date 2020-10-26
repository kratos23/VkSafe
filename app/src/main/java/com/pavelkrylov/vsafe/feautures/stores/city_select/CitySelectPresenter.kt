package com.pavelkrylov.vsafe.feautures.stores.city_select

import com.pavelkrylov.vsafe.App
import com.pavelkrylov.vsafe.base.BasePresenter
import com.pavelkrylov.vsafe.feautures.stores.CityVM
import com.pavelkrylov.vsafe.feautures.stores.UICity

class CitySelectPresenter(val citySelectVM: CitySelectVM) : BasePresenter() {
    val interactor = CityInteractor()

    init {
        interactor.loadCities().observeForever {
            citySelectVM.citiesLD.postValue(it)
        }
    }

    private lateinit var selectedCityVM: CityVM

    fun setSelectedCityVm(selectedCityVM: CityVM) {
        this.selectedCityVM = selectedCityVM
    }

    fun citySelected(city: UICity) {
        selectedCityVM.selectedCity.value = city
        CityInteractor.saveCity(city)
        App.instance.getRouter().exit()
    }
}