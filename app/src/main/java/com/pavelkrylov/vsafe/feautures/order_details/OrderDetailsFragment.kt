package com.pavelkrylov.vsafe.feautures.order_details

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.pavelkrylov.vsafe.App
import com.pavelkrylov.vsafe.vkmarket.R
import kotlinx.android.synthetic.main.order_details.*

class OrderDetailsFragment : Fragment(R.layout.order_details) {
    companion object {
        private const val IS_CUSTOMER_KEY = "is_customer"
        private const val ORDER_ID_KEY = "order_id"

        fun newInstance(isCustomer: Boolean, orderId: Long): OrderDetailsFragment {
            val fragment = OrderDetailsFragment()
            fragment.arguments = Bundle().apply {
                putBoolean(IS_CUSTOMER_KEY, isCustomer)
                putLong(ORDER_ID_KEY, orderId)
            }
            return fragment
        }
    }

    var isCustomer = false
    var orderId = -1L

    lateinit var vm: OrderDetailsVM

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCustomer = requireArguments().getBoolean(IS_CUSTOMER_KEY)
        orderId = requireArguments().getLong(ORDER_ID_KEY)
        vm = OrderDetailsVMFactory(isCustomer, orderId).create(OrderDetailsVM::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vm.state.observe(viewLifecycleOwner, this::onUIState)
        toolBar.setNavigationOnClickListener { App.INSTANCE.outerCicerone.router.exit() }
    }

    private fun onUIState(state: UIState) {
        toolBar.title = getString(R.string.order_id_string, state.orderId)
        if (state.orderInfo == null) {
            buttonsContainer.visibility = View.INVISIBLE
            progress.visibility = View.VISIBLE
        } else {
            buttonsContainer.visibility = View.VISIBLE
            progress.visibility = View.INVISIBLE
            println(state.orderInfo.orderList)
        }
    }
}