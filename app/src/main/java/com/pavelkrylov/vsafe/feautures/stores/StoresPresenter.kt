package com.pavelkrylov.vsafe.feautures.stores

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
        onLoadMore()
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

    var storesEnd = false

    fun setQuery(query: String) {
        interactor.updateQuery(query)
        storesEnd = false
        vm.firstStoresLD.value = emptyList()
        onLoadMore()
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
            vm.addStoresLD.value = it
            vm.firstStoresLD.value = list
        }
    }

    fun storeClicked(store: UIStore) {
        App.INSTANCE.getRouter().navigateTo(Screens.ProductsScreen(store.id, store.name))
    }
}