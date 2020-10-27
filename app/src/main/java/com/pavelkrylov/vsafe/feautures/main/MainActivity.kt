package com.pavelkrylov.vsafe.feautures.main

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.pavelkrylov.vsafe.App
import com.pavelkrylov.vsafe.feautures.customer.MainCustomerFragment
import com.pavelkrylov.vsafe.feautures.stores.city_select.CitySelectFragment
import com.pavelkrylov.vsafe.vkmarket.R
import ru.terrakok.cicerone.android.support.SupportAppNavigator
import ru.terrakok.cicerone.commands.Command


class MainActivity : AppCompatActivity() {
    private val presenter = MainPresenter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_layout)
        if (savedInstanceState == null) {
            presenter.onFirstAttach()
        }
    }


    override fun onResume() {
        super.onResume()
        val navigator = object : SupportAppNavigator(this, R.id.container) {
            override fun setupFragmentTransaction(
                command: Command, currentFragment: Fragment?,
                nextFragment: Fragment?,
                fragmentTransaction: FragmentTransaction
            ) {
                if (nextFragment is MainCustomerFragment) {
                    fragmentTransaction.setPrimaryNavigationFragment(nextFragment)
                }
                if (nextFragment is CitySelectFragment) {
                    fragmentTransaction.setCustomAnimations(
                        R.anim.slide_in_up,
                        R.anim.fade_exit, R.anim.fade_enter, R.anim.slide_out_up
                    )
                }
            }
        }
        App.INSTANCE.getNavigatorHolder().setNavigator(navigator)
        presenter.onAttach()
    }

    override fun onPause() {
        super.onPause()
        App.INSTANCE.getNavigatorHolder().removeNavigator()
        presenter.onDetach()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        for (fragment in supportFragmentManager.fragments) {
            fragment.onActivityResult(requestCode, resultCode, data)
        }
    }

}