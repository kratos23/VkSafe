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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        loginBtn.setOnClickListener {
            VK.login(activity!!, listOf(VKScope.MARKET, VKScope.GROUPS, VKScope.OFFLINE))
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val callback = object : VKAuthCallback {
            override fun onLogin(token: VKAccessToken) {
                VkTokenStorage.saveToken(token.accessToken)
                App.INSTANCE.cicerone.router.replaceScreen(Screens.StoresScreen())
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