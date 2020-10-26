package com.pavelkrylov.vsafe.feautures.stores

import android.os.Handler
import com.pavelkrylov.vsafe.App
import com.pavelkrylov.vsafe.base.BasePresenter
import com.pavelkrylov.vsafe.base.Screens

class StoresPresenter(val vm: StoresVM) : BasePresenter() {
    var view: StoresFragment? = null
    val interactor = StoresInteractor()

    init {
        interactor.endLd.observeForever {
            storesEnd = it
        }
    }

    fun onAttach(view: StoresFragment) {
        this.view = view
    }

    override fun onDetach() {
        super.onDetach()
        this.view = null
    }

    fun onDestroy() {
        interactor.stop()
    }

    private var selectedCityVM: CityVM? = null


    var storesEnd = false

    fun setSelectedCityVm(selectedCityVM: CityVM) {
        if (this.selectedCityVM == null) {
            selectedCityVM.selectedCity.observeForever {
                interactor.updateCity(it.id)
                storesEnd = false
                vm.firstStoresLD.value = emptyList()
                Handler().postDelayed({ onLoadMore() }, 300)
            }
        }
        this.selectedCityVM = selectedCityVM
    }

    fun citySelectClicked() {
        App.instance.cicerone.router.navigateTo(Screens.CitySelectScreen())
    }


    fun onLoadMore() {
        if (storesEnd) {
            return
        }
        vm.loadingLD.value = true
        interactor.loadMoreStores()?.observeForever {
            vm.loadingLD.value = false
            val list = ArrayList<UIStore>(vm.firstStoresLD.value ?: emptyList())
            list.addAll(it)
            vm.firstStoresLD.value = list
            vm.addStoresLD.value = it
        }
    }

    fun storeClicked(store: UIStore) {
        App.instance.getRouter().navigateTo(Screens.ProductsScreen(store.id, store.name))
    }
}