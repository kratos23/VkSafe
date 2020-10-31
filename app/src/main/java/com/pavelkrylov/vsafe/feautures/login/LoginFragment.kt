package com.pavelkrylov.vsafe.feautures.login

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.pavelkrylov.vsafe.App
import com.pavelkrylov.vsafe.base.Screens
import com.pavelkrylov.vsafe.logic.network.VkTokenStorage
import com.pavelkrylov.vsafe.vkmarket.R
import com.vk.api.sdk.VK
import com.vk.api.sdk.auth.VKAccessToken
import com.vk.api.sdk.auth.VKAuthCallback
import com.vk.api.sdk.auth.VKScope
import kotlinx.android.synthetic.main.login_layout.*

class LoginFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.login_layout, container, false)
    }

    private fun loginToVk() {
        VK.login(activity!!, listOf(VKScope.MARKET, VKScope.GROUPS, VKScope.OFFLINE))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        loginMarketBtn.setOnClickListener {
            App.INSTANCE.setIsCustomer(false)
            loginToVk()
        }
        loginCustomerBtn.setOnClickListener {
            App.INSTANCE.setIsCustomer(true)
            loginToVk()
        }
    }

    val router = App.INSTANCE.outerCicerone.router

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val callback = object : VKAuthCallback {
            override fun onLogin(token: VKAccessToken) {
                VkTokenStorage.saveToken(token.accessToken)
                if (App.INSTANCE.getIsCustomer()) {
                    router.replaceScreen(Screens.MainCustomerScreen())
                } else {
                    router.replaceScreen(Screens.StoreSelectionScreen())
                }
            }

            override fun onLoginFailed(errorCode: Int) {
                Toast.makeText(context, "Авторизация не удалась", Toast.LENGTH_SHORT).show();
            }
        }
        if (data == null || !VK.onActivityResult(requestCode, resultCode, data, callback)) {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }
}