package com.pavelkrylov.vsafe.feautures.checkout

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.pavelkrylov.vsafe.App
import com.pavelkrylov.vsafe.base.Screens
import com.pavelkrylov.vsafe.vkmarket.R
import kotlinx.android.synthetic.main.checkout_success.*

class CheckoutSuccessFragment : Fragment(R.layout.checkout_success) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        goToOrdersBtn.setOnClickListener {
            App.INSTANCE.customerCicerone.router.backTo(Screens.CustomerOrdersScreen())
            App.INSTANCE.outerCicerone.router.backTo(Screens.MainCustomerScreen())
        }
    }
}