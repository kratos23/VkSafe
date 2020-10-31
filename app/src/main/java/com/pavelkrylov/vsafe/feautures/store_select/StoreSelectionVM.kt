package com.pavelkrylov.vsafe.feautures.store_select

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pavelkrylov.vsafe.App
import com.pavelkrylov.vsafe.base.Screens
import com.pavelkrylov.vsafe.base.retry
import com.pavelkrylov.vsafe.feautures.stores.UIStore
import com.pavelkrylov.vsafe.logic.network.OkHttp
import com.pavelkrylov.vsafe.vkmarket.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class StoreSelectionVM : ViewModel() {
    val stores = MutableLiveData<List<UIStore>?>(null)

    @Suppress("BlockingMethodInNonBlockingContext")
    suspend fun loadStores() {
        val client = OkHttp.client
        val req = Request.Builder()
            .url(
                if (BuildConfig.DEBUG) {
                    OkHttp.USER_MARKETS_URL.toHttpUrl()
                        .newBuilder()
                        .addQueryParameter("forceList", "true")
                        .build()
                } else {
                    OkHttp.USER_MARKETS_URL.toHttpUrl()
                }
            )
            .get()
            .build()
        retry {
            val resp = client.newCall(req).execute()
            val json = JSONObject(resp.body!!.string())
            resp.close()
            if (json.has("marketId")) {
                withContext(Dispatchers.Main) {
                    App.INSTANCE.setMarketSelected(true)
                    App.INSTANCE.outerCicerone.router.replaceScreen(Screens.StoreOrdersScreen())
                }
            } else {
                val groups = JSONObject(json.getString("groups"))
                val groupsArr = groups.getJSONObject("response").getJSONArray("items")
                val storesList = mutableListOf<UIStore>()
                for (i in 0 until groupsArr.length()) {
                    val groupJSON = groupsArr.getJSONObject(i)
                    if (groupJSON.optJSONObject("market")?.optInt("enabled", 0) == 1) {
                        val uiStore = UIStore(
                            id = groupJSON.getLong("id"),
                            name = groupJSON.getString("name"),
                            photoUrl = groupJSON.getString("photo_200"),
                            activity = groupJSON.getString("activity")
                        )
                        storesList.add(uiStore)
                    }
                }
                withContext(Dispatchers.Main) {
                    stores.value = storesList
                }
            }
        }
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    fun storeClicked(storeId: Long) {
        viewModelScope.launch {
            retry {
                val code = withContext(Dispatchers.IO) {
                    val client = OkHttp.client
                    val req = Request.Builder()
                        .url(OkHttp.USER_MARKETS_URL.toHttpUrl())
                        .post(storeId.toString().toRequestBody(null))
                        .build()
                    val resp = client.newCall(req).execute()
                    resp.code
                }
                if (code == 200) {
                    App.INSTANCE.setMarketSelected(true)
                    App.INSTANCE.outerCicerone.router.replaceScreen(Screens.StoreOrdersScreen())
                }
            }
        }
    }

}