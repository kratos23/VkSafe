package com.pavelkrylov.vsafe.feautures.checkout

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.pavelkrylov.vsafe.App
import com.pavelkrylov.vsafe.logic.UICurrency
import com.pavelkrylov.vsafe.vkmarket.R
import kotlinx.android.synthetic.main.checkout.*
import kotlin.math.roundToLong

class CheckoutFragment : Fragment(R.layout.checkout) {
    companion object {
        const val GROUP_ID_KEY = "group_id"

        fun newInstance(groupId: Long): CheckoutFragment {
            val fragment = CheckoutFragment()
            fragment.arguments = Bundle().apply {
                putLong(GROUP_ID_KEY, groupId)
            }
            return fragment
        }
    }

    var groupId = -1L
    lateinit var vm: CheckoutVM

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        groupId = requireArguments().getLong(GROUP_ID_KEY)
        vm = ViewModelProvider(this, CheckoutVMFactory(groupId)).get(CheckoutVM::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vm.priceLD.observe(viewLifecycleOwner, this::onPrice)
        toolBar.setNavigationOnClickListener { App.INSTANCE.outerCicerone.router.exit() }
    }

    private fun onPrice(price: Pair<Long, UICurrency>?) {
        if (price == null) {
            priceGroup.visibility = View.GONE
        } else {
            val (amount, currency) = price
            val amountS = (amount / 100.0).roundToLong()
            val priceS = "$amountS ${currency.sign ?: currency.name}"
            totalPrice.text = priceS
            priceGroup.visibility = View.VISIBLE
        }
    }
}