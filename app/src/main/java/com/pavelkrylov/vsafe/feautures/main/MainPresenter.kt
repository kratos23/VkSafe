package com.pavelkrylov.vsafe.feautures.main

import com.pavelkrylov.vsafe.App
import com.pavelkrylov.vsafe.base.BasePresenter
import com.pavelkrylov.vsafe.base.Screens
import com.pavelkrylov.vsafe.vkmarket.BuildConfig
import com.vk.api.sdk.VK

class MainPresenter : BasePresenter() {
    private val router = App.INSTANCE.outerCicerone.router

    override fun onFirstAttach() {
        super.onFirstAttach()
        if (VK.isLoggedIn()) {
            if (BuildConfig.DEBUG && false) {
                router.replaceScreen(Screens.OrderDetailsScreen(true, 5))
            } else {
                router.replaceScreen(Screens.MainCustomerScreen())
            }
        } else {
            router.replaceScreen(Screens.LoginScreen())
        }
    }

    override fun onAttach() {

    }

    override fun onDetach() {
    }
}