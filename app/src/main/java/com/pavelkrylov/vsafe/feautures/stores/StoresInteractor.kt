package com.pavelkrylov.vsafe.feautures.stores

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.pavelkrylov.vsafe.base.VkRequestThread
import com.squareup.picasso.Picasso
import com.vk.api.sdk.requests.VKRequest
import org.json.JSONObject

class StoresInteractor {

    companion object {
        const val STORES_LEN = 1000
        const val PAGE_LEN = 20
    }

    private var loading = false
    private var page = 0
    private var cityId = 0L
    private var loadThread: Thread? = null

    val endLd = MutableLiveData<Boolean>(false)

    fun loadMoreStores(): LiveData<List<UIStore>>? {
        if (loading) {
            return null
        }
        val result = MutableLiveData<List<UIStore>>()
        result.observeForever {
            loading = false
        }
        loadThread = VkRequestThread(StoresRequest(page, cityId), result).apply {
            start()
        }
        page++
        if (page == STORES_LEN / PAGE_LEN) {
            endLd.value = true
        }
        loading = true
        return result
    }

    fun updateCity(cityId: Long) {
        this.cityId = cityId
        loadThread?.interrupt()
        loadThread = null
        endLd.value = false
        page = 0
    }

    class StoresRequest(val page: Int, val cityId: Long) :
        VKRequest<List<UIStore>>("execute.storesList") {
        init {
            addParam("page", page)
            addParam("city", cityId)
        }

        override fun parse(r: JSONObject): List<UIStore> {
            val array = r.getJSONArray("response")
            val result = ArrayList<UIStore>()
            for (i in 0 until array.length()) {
                val storeJson = array.getJSONObject(i)
                val name = storeJson.getString("name")
                val id = storeJson.getLong("id")
                val activity = storeJson.getString("activity")
                val photoUrl = storeJson.getString("photo_200")
                Picasso.get().load(photoUrl).fetch()
                result.add(UIStore(id, name, photoUrl, activity))
            }
            return result
        }
    }

    fun stop() {
        loadThread?.interrupt()
        loadThread = null
    }
}