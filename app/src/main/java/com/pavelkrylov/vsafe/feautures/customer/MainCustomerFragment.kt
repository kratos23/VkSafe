package com.pavelkrylov.vsafe.feautures.customer

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.pavelkrylov.vsafe.App
import com.pavelkrylov.vsafe.base.Screens
import com.pavelkrylov.vsafe.feautures.main.MainActivity
import com.pavelkrylov.vsafe.feautures.orders.OrdersFragment
import com.pavelkrylov.vsafe.feautures.stores.StoresFragment
import com.pavelkrylov.vsafe.vkmarket.R
import kotlinx.android.synthetic.main.main_customer.*
import ru.terrakok.cicerone.android.support.SupportAppNavigator
import ru.terrakok.cicerone.android.support.SupportAppScreen
import ru.terrakok.cicerone.commands.Command
import ru.terrakok.cicerone.commands.Forward

class MainCustomerFragment : Fragment(R.layout.main_customer) {

    private val vm by viewModels<MainCustomerVM>()

    val storesScreen = Screens.StoresScreen()
    val ordersScreen = Screens.CustomerOrdersScreen()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            vm.onInitialCreate(this)
        }
    }

    override fun onResume() {
        super.onResume()
        val navigator = object : SupportAppNavigator(
            activity as MainActivity, childFragmentManager,
            R.id.customerContainer
        ) {
            override fun applyCommands(commands: Array<out Command>) {
                super.applyCommands(commands)
                childFragmentManager.executePendingTransactions()
                updateNavigationSelection()
            }

            override fun backToUnexisting(screen: SupportAppScreen) {
                fragmentForward(Forward(screen))
            }
        }
        childFragmentManager.addOnBackStackChangedListener {
            updateNavigationSelection()
        }
        App.INSTANCE.customerCicerone.navigatorHolder.setNavigator(navigator)
    }

    private fun updateNavigationSelection() {
        val id = when (childFragmentManager.findFragmentById(R.id.customerContainer)) {
            is StoresFragment -> R.id.stores
            is OrdersFragment -> R.id.orders
            else -> null
        }
        requireNotNull(id)
        bottomNavigation.setOnNavigationItemSelectedListener(null)
        bottomNavigation.selectedItemId = id
        bottomNavigation.setOnNavigationItemSelectedListener(navigationListener)
    }

    private val navigationListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        vm.navigationItemClicked(item.itemId, this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bottomNavigation.setOnNavigationItemSelectedListener(navigationListener)
    }

    override fun onPause() {
        App.INSTANCE.customerCicerone.navigatorHolder.removeNavigator()
        super.onPause()
    }
}