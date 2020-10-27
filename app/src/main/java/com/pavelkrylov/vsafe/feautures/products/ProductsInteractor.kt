package com.pavelkrylov.vsafe.feautures.products

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.pavelkrylov.vsafe.base.VkRequestThread
import com.pavelkrylov.vsafe.logic.UICurrency
import com.pavelkrylov.vsafe.logic.getCurrencyShort
import com.squareup.picasso.Picasso
import com.vk.api.sdk.requests.VKRequest
import org.json.JSONObject

class ProductsInteractor(val groupId: Long) {

    companion object {
        const val PAGE_SIZE = 9
    }

    private var loading = false
    private var page = 0
    private var loadThread: Thread? = null
    public val endLD = MutableLiveData<Boolean>(false)


    fun loadMoreProducts(): LiveData<List<UIProduct>>? {
        if (loading) {
            return null
        }
        loading = true
        val result = MutableLiveData<List<UIProduct>>()
        result.observeForever {
            loading = false
            if (it.isEmpty()) {
                endLD.value = true
            }
        }
        loadThread = VkRequestThread(ProductsVkRequest(groupId, page), result).apply {
            start()
        }
        page++
        return result
    }



    inner class ProductsVkRequest(val groupId: Long, val page: Int) :
        VKRequest<List<UIProduct>>("market.get") {
        init {
            addParam("owner_id", -groupId)
            addParam("count", PAGE_SIZE)
            addParam("offset", page * PAGE_SIZE)
        }

        override fun parse(r: JSONObject): List<UIProduct> {
            val result = ArrayList<UIProduct>()
            val items = r.getJSONObject("response").getJSONArray("items")
            for (i in 0 until items.length()) {
                val productJson = items.getJSONObject(i)
                val id = productJson.getLong("id")
                val priceJson = productJson.getJSONObject("price")
                val price = priceJson.getLong("amount") / 100
                val currencyJson = priceJson.getJSONObject("currency")
                val currencyId = currencyJson.getInt("id")
                val currency = UICurrency(
                    currencyId,
                    currencyJson.getString("name"),
                    getCurrencyShort(currencyJson.getString("name"))
                )
                val photo = productJson.getString("thumb_photo")
                Picasso.get().load(photo).fetch()
                val name = productJson.getString("title")
                val product = UIProduct(name, price, currency, photo, id)
                result.add(product)
            }
            return result
        }
    }

    fun stop() {
        loadThread?.interrupt()
    }
}