package com.pavelkrylov.vsafe.feautures.customer

import androidx.lifecycle.ViewModel
import com.pavelkrylov.vsafe.App
import com.pavelkrylov.vsafe.vkmarket.R

class MainCustomerVM : ViewModel() {

    private val router = App.INSTANCE.customerCicerone.router

    init {
    }

    fun onInitialCreate(fr: MainCustomerFragment) {
        router.newRootScreen(fr.storesScreen)
    }


    fun navigationItemClicked(id: Int, fr: MainCustomerFragment): Boolean {
        val nextScreen = when (id) {
            R.id.stores -> fr.storesScreen
            R.id.orders -> fr.ordersScreen
            else -> null
        }
        nextScreen?.let {
            router.backTo(nextScreen)
        }
        return nextScreen != null
    }
}