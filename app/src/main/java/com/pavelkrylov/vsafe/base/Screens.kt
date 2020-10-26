package com.pavelkrylov.vsafe.base

import androidx.fragment.app.Fragment
import com.pavelkrylov.vsafe.feautures.login.LoginFragment
import com.pavelkrylov.vsafe.feautures.product_info.ProductInfoFragment
import com.pavelkrylov.vsafe.feautures.products.ProductsFragment
import com.pavelkrylov.vsafe.feautures.stores.StoresFragment
import com.pavelkrylov.vsafe.feautures.stores.city_select.CitySelectFragment
import ru.terrakok.cicerone.android.support.SupportAppScreen

class Screens {
    class LoginScreen() : SupportAppScreen() {
        override fun getFragment(): Fragment {
            return LoginFragment()
        }
    }

    class StoresScreen() : SupportAppScreen() {
        override fun getFragment(): Fragment? {
            return StoresFragment()
        }
    }

    class CitySelectScreen() : SupportAppScreen() {
        override fun getFragment(): Fragment? {
            return CitySelectFragment()
        }
    }

    class ProductsScreen(val groupId: Long, val groupName: String) : SupportAppScreen() {
        override fun getFragment(): Fragment {
            return ProductsFragment.newInstance(groupId, groupName)
        }
    }

    class ProductInfoScreen(val productId : Long, val groupId: Long, val productName:String) : SupportAppScreen() {
        override fun getFragment(): Fragment {
            return ProductInfoFragment.newInstance(productId, groupId, productName)
        }
    }
}