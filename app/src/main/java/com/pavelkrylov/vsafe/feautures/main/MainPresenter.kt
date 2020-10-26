package com.pavelkrylov.vsafe.feautures.main

import com.pavelkrylov.vsafe.App
import com.pavelkrylov.vsafe.base.BasePresenter
import com.pavelkrylov.vsafe.base.Screens
import com.vk.api.sdk.VK

class MainPresenter : BasePresenter() {
    override fun onFirstAttach() {
        super.onFirstAttach()
        if (VK.isLoggedIn()) {
            App.INSTANCE.cicerone.router.replaceScreen(Screens.StoresScreen())
        } else {
            App.INSTANCE.cicerone.router.replaceScreen(Screens.LoginScreen())
        }
    }

    override fun onAttach() {

    }

    override fun onDetach() {
    }
}