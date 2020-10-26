package com.pavelkrylov.vsafe.feautures.stores

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.pavelkrylov.vsafe.base.MyRequest
import com.pavelkrylov.vsafe.base.RequestThread
import com.pavelkrylov.vsafe.logic.network.OkHttp
import com.squareup.picasso.Picasso
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Request
import org.json.JSONObject
import java.lang.Thread.sleep

class StoresInteractor {
    private var loading = false
    private var page = 0
    private var q = ""
    private var loadThread: Thread? = null

    val endLd = MutableLiveData<Boolean>(false)

    fun loadMoreStores(): LiveData<List<UIStore>>? {
        println("loading $loading")
        if (loading) {
            return null
        }
        val result = MutableLiveData<List<UIStore>>()
        result.observeForever { list ->
            loading = false
            if (list.isEmpty()) {
                endLd.value = true
            }
        }
        loading = true
        loadThread = RequestThread(StoresRequest2(page, q), result).apply {
            start()
        }
        page++
        return result
    }

    fun updateQuery(q: String) {
        this.q = q
        loadThread?.interrupt()
        loading = false
        loadThread = null
        endLd.value = false
        page = 0
    }

    class StoresRequest2(val page: Int, val q: String) : MyRequest<List<UIStore>> {
        override fun run(): List<UIStore> {
            sleep(300) // debounce
            val client = OkHttp.client
            val request = Request.Builder()
                .url(
                    OkHttp.MARKETS_URL.toHttpUrl()
                        .newBuilder()
                        .addQueryParameter("page", page.toString())
                        .addQueryParameter("q", q)
                        .build()
                )
                .build()
            val resp = client.newCall(request).execute()
            val s = resp.body!!.string()
            val r = JSONObject(s)
            resp.close()
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