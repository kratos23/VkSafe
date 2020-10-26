package com.pavelkrylov.vsafe.feautures.products

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.pavelkrylov.vsafe.vkmarket.R
import com.pavelkrylov.vsafe.logic.formatPrice
import com.pavelkrylov.vsafe.ui.RoundedCornersTransform
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.product.view.*
import java.lang.IllegalArgumentException

class ProductsAdapter(val ctx: Context, val productSelectedListener : (UIProduct)->Unit) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    companion object {
        const val TYPE_ITEM = 0
        const val TYPE_LOADING = 1
    }

    var products = ArrayList<UIProduct>()

    private var loading = false

    public fun setLoading(newLoading: Boolean) {
        if (newLoading == loading) {
            return
        }
        if (!newLoading) {
            notifyItemRemoved(products.size)
        } else {
            notifyItemInserted(products.size)
        }
        loading = newLoading
    }

    val inflater = LayoutInflater.from(ctx)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_ITEM -> StoreHolder(inflater.inflate(R.layout.product, parent, false))
            TYPE_LOADING -> LoadingHolder(inflater.inflate(R.layout.loading, parent, false))
            else -> throw IllegalArgumentException()
        }
    }

    override fun getItemCount(): Int {
        var ans = products.size
        if (loading) {
            ans++
        }
        return ans
    }

    override fun getItemViewType(position: Int): Int {
        if (position < products.size) {
            return TYPE_ITEM
        } else {
            return TYPE_LOADING
        }
    }



    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            TYPE_ITEM -> {
                val product = products[position]
                val v = holder.itemView
                v.productName.text = product.name
                v.price.text = formatPrice(product.price, product.priceCurrency)
                Picasso.get()
                    .load(product.photoUrl)
                    .transform(RoundedCornersTransform())
                    .into(v.productImage)
            }
        }
    }

   inner class StoreHolder(v: View) : RecyclerView.ViewHolder(v) {
       init {
           v.setOnClickListener {
               val position = adapterPosition
               if (position != RecyclerView.NO_POSITION) {
                   productSelectedListener(products[position])
               }
           }
       }
   }

    class LoadingHolder(v: View) : RecyclerView.ViewHolder(v)
}