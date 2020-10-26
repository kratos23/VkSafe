package com.pavelkrylov.vsafe.logic.network

import android.content.Context
import com.pavelkrylov.vsafe.App

object VkTokenStorage {
    private const val PREF_NAME = "token_prefs"
    private const val TOKEN_KEY = "token"

    private val prefs = App.INSTANCE.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun saveToken(token: String) {
        prefs.edit().putString(TOKEN_KEY, token).apply()
    }

    fun getToken(): String? {
        return prefs.getString(TOKEN_KEY, null)
    }
}