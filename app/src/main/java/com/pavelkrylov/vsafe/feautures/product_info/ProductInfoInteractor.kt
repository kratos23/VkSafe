package com.pavelkrylov.vsafe.feautures.product_info

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.pavelkrylov.vsafe.App
import com.pavelkrylov.vsafe.base.VkRequestThread
import com.pavelkrylov.vsafe.logic.FavoriteStorage
import com.pavelkrylov.vsafe.logic.UICurrency
import com.pavelkrylov.vsafe.logic.db.entities.DbProduct
import com.pavelkrylov.vsafe.logic.getCurrencyShort
import com.vk.api.sdk.requests.VKRequest
import org.json.JSONObject
import kotlin.concurrent.thread

class ProductInfoInteractor(val productId: Long, val groupId: Long) {

    var loading = false

    var loadingThread: Thread? = null

    fun loadInfo(): LiveData<UIProductInfo>? {
        if (loading) {
            return null
        }
        val result = MutableLiveData<UIProductInfo>()
        result.observeForever {
            loading = false
            loadingThread = null
        }
        loadingThread = VkRequestThread(ProductInfoVKRequest(groupId, productId), result).apply {
            start()
        }

        loading = true
        return result
    }

    class ProductInfoVKRequest(val groupId: Long, val productId: Long) :
        VKRequest<UIProductInfo>("market.getById") {

        companion object {
            val SIZES_TO_CHECK = setOf("y", "x", "m", "s")
            const val THUMBNAIL_SIZE = 400
        }

        init {
            addParam("item_ids", "-${groupId}_$productId")
            addParam("extended", "1")
        }

        override fun parse(r: JSONObject): UIProductInfo {
            val productInfo = r.getJSONObject("response").getJSONArray("items").getJSONObject(0)
            val description: String? = productInfo.optString("description", null)
            val id = productInfo.getLong("id")
            val priceJson = productInfo.getJSONObject("price")
            val currencyJson = priceJson.getJSONObject("currency")
            val currencyId = currencyJson.getInt("id")
            val name = productInfo.getString("title")
            val amount = priceJson.getLong("amount") / 100
            val currency = UICurrency(
                currencyId,
                currencyJson.getString("name"),
                getCurrencyShort(currencyJson.getString("name"))
            )
            var photoURL = productInfo.getString("thumb_photo")
            var pixels = THUMBNAIL_SIZE * THUMBNAIL_SIZE
            val photosToCheck = productInfo.getJSONArray("photos")
                .optJSONObject(0)?.getJSONArray("sizes")
            if (photosToCheck != null) {
                for (i in 0 until photosToCheck.length()) {
                    val sizeInfo = photosToCheck.getJSONObject(i)
                    if (sizeInfo.getString("type") in SIZES_TO_CHECK) {
                        val w = sizeInfo.getInt("width")
                        val h = sizeInfo.getInt("height")
                        val curPx = w * h
                        if (curPx > pixels) {
                            pixels = curPx
                            photoURL = sizeInfo.getString("url")
                        }
                    }
                }
            }
            val productsDao = App.INSTANCE.db.value.productsDao()
            productsDao.save(DbProduct(id = id.toString(), productInfo.toString()))
            return UIProductInfo(
                id, description, photoURL, amount, currency, name,
                FavoriteStorage.checkFavorite(groupId, productId)
            )
        }
    }

    fun stop() {
        loading = false
        loadingThread?.interrupt()
        loadingThread = null
    }

    fun addToFavorite() {
        thread(priority = 1) {
            FavoriteStorage.addFavorite(groupId, productId)
        }
    }

    fun removeFromFavorites() {
        thread(priority = 1) {
            FavoriteStorage.removeFavorite(groupId, productId)
        }
    }
}