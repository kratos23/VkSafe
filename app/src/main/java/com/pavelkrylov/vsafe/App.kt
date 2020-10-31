package com.pavelkrylov.vsafe

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.pavelkrylov.vsafe.base.Screens
import com.pavelkrylov.vsafe.logic.db.RoomDb
import com.pavelkrylov.vsafe.logic.network.OkHttp
import com.squareup.picasso.LruCache
import com.squareup.picasso.Picasso
import com.vk.api.sdk.VK
import com.vk.api.sdk.VKApiConfig
import com.vk.api.sdk.VKDefaultValidationHandler
import com.vk.api.sdk.VKTokenExpiredHandler
import ru.terrakok.cicerone.Cicerone
import ru.terrakok.cicerone.NavigatorHolder
import ru.terrakok.cicerone.Router


class App : Application() {

    companion object {
        lateinit var INSTANCE: App
        const val PICASSO_DISK_CACHE_SIZE = 1024 * 1024 * 30
        const val IS_CUSTOMER_KEY = "is_customer"
    }

    val outerCicerone = Cicerone.create()
    val customerCicerone = Cicerone.create()
    val db = lazy {
        Room.databaseBuilder(
            applicationContext,
            RoomDb::class.java, "roomDb"
        ).build()
    }

    private fun getPrefs() = getSharedPreferences("prefs", Context.MODE_PRIVATE)

    fun setIsCustomer(isCustomer: Boolean) {
        getPrefs().edit().putBoolean(IS_CUSTOMER_KEY, isCustomer).apply()
    }

    fun getIsCustomer(): Boolean {
        return getPrefs().getBoolean(IS_CUSTOMER_KEY, true)
    }

    private fun onInvalidToken() {
        outerCicerone.router.replaceScreen(Screens.LoginScreen())
    }

    override fun onCreate() {
        super.onCreate()
        Picasso.setSingletonInstance(
            Picasso.Builder(this)
                .memoryCache(LruCache(PICASSO_DISK_CACHE_SIZE))
                .build()
        )
        INSTANCE = this
        val config = VKApiConfig(
            context = this,
            validationHandler = VKDefaultValidationHandler(this),
            appId = VK.getAppId(this),
            lang = "ru"
        )
        VK.setConfig(config)

        VK.addTokenExpiredHandler(object : VKTokenExpiredHandler {
            override fun onTokenExpired() {
                onInvalidToken()
            }
        })
        OkHttp.onInvalidTokenListener = this::onInvalidToken
    }

    fun getNavigatorHolder(): NavigatorHolder {
        return outerCicerone.navigatorHolder
    }

    fun getRouter(): Router {
        return outerCicerone.router
    }
}