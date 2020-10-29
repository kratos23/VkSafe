package com.pavelkrylov.vsafe.base

import androidx.fragment.app.Fragment
import com.pavelkrylov.vsafe.feautures.cart.CartFragment
import com.pavelkrylov.vsafe.feautures.checkout.CheckoutFragment
import com.pavelkrylov.vsafe.feautures.customer.MainCustomerFragment
import com.pavelkrylov.vsafe.feautures.login.LoginFragment
import com.pavelkrylov.vsafe.feautures.orders.CustomerOrdersFragment
import com.pavelkrylov.vsafe.feautures.product_info.ProductInfoFragment
import com.pavelkrylov.vsafe.feautures.products.ProductsFragment
import com.pavelkrylov.vsafe.feautures.stores.StoresFragment
import ru.terrakok.cicerone.android.support.SupportAppScreen

class Screens {
    class LoginScreen() : SupportAppScreen() {
        override fun getFragment(): Fragment {
            return LoginFragment()
        }
    }

    class StoresScreen(val fr: StoresFragment = StoresFragment()) : SupportAppScreen() {
        override fun getFragment(): Fragment? {
            return fr
        }
    }

    class ProductsScreen(val groupId: Long, val groupName: String) : SupportAppScreen() {
        override fun getFragment(): Fragment {
            return ProductsFragment.newInstance(groupId, groupName)
        }

        override fun getScreenKey() = "products_$groupId"
    }

    class ProductInfoScreen(val productId: Long, val groupId: Long, val productName: String) :
        SupportAppScreen() {

        override fun getScreenKey() = "product_info${productId}_${productName}"

        override fun getFragment(): Fragment {
            return ProductInfoFragment.newInstance(productId, groupId, productName)
        }
    }

    class MainCustomerScreen() : SupportAppScreen() {
        override fun getFragment(): Fragment {
            return MainCustomerFragment()
        }
    }

    class CustomerOrdersScreen(val fr: CustomerOrdersFragment = CustomerOrdersFragment()) :
        SupportAppScreen() {
        override fun getFragment(): Fragment {
            return fr
        }
    }

    class CartScreen(val groupId: Long) : SupportAppScreen() {
        override fun getFragment(): Fragment {
            return CartFragment.newInstance(groupId)
        }

        override fun getScreenKey() = "cart_$groupId"
    }

    class CheckoutScreen(val groupId: Long) : SupportAppScreen() {
        override fun getScreenKey() = "checkout_$groupId"

        override fun getFragment(): Fragment {
            return CheckoutFragment.newInstance(groupId)
        }
    }
}