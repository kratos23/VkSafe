package com.pavelkrylov.vsafe.feautures.stores

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.pavelkrylov.vsafe.vkmarket.R
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.store.view.*
import java.lang.IllegalArgumentException

class StoresAdapter(val ctx: Context, val storeClickedListener: (UIStore) -> Unit) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    companion object {
        const val TYPE_ITEM = 0
        const val TYPE_LOADING = 1
    }

    var stores = ArrayList<UIStore>()

    private var loading = false

    public fun setLoading(newLoading: Boolean) {
        if (newLoading == loading) {
            return
        }
        if (!newLoading) {
            notifyItemRemoved(stores.size)
        } else {
            notifyItemInserted(stores.size)
        }
        loading = newLoading
    }

    val inflater = LayoutInflater.from(ctx)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_ITEM -> StoreHolder(inflater.inflate(R.layout.store, parent, false))
            TYPE_LOADING -> LoadingHolder(inflater.inflate(R.layout.loading, parent, false))
            else -> throw IllegalArgumentException()
        }
    }

    override fun getItemCount(): Int {
        var ans = stores.size
        if (loading) {
            ans++
        }
        return ans
    }

    override fun getItemViewType(position: Int): Int {
        if (position < stores.size) {
            return TYPE_ITEM
        } else {
            return TYPE_LOADING
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            TYPE_ITEM -> {
                val store = stores[position]
                val v = holder.itemView
                v.groupType.text = store.activity
                v.name.text = store.name
                Picasso.get()
                    .load(store.photoUrl)
                    .transform(CircleTransform())
                    .into(v.logo)
            }
        }
    }

    inner class StoreHolder(v: View) : RecyclerView.ViewHolder(v) {
        init {
            v.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    storeClickedListener(stores[position])
                }
            }
        }
    }

    class LoadingHolder(v: View) : RecyclerView.ViewHolder(v)
}