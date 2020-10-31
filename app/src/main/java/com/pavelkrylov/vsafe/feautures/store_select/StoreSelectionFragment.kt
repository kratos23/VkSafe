package com.pavelkrylov.vsafe.feautures.store_select

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.pavelkrylov.vsafe.feautures.stores.CircleTransform
import com.pavelkrylov.vsafe.feautures.stores.UIStore
import com.pavelkrylov.vsafe.vkmarket.R
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.store.view.*
import kotlinx.android.synthetic.main.store_selection.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class StoreSelectionFragment : Fragment(R.layout.store_selection) {

    private val vm: StoreSelectionVM by viewModels()

    private lateinit var adapter: StoresAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        adapter = StoresAdapter(vm)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vm.stores.observe(viewLifecycleOwner, this::onStores)
        viewLifecycleOwner.lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                vm.loadStores()
            }
        }
        selectStoreRv.adapter = adapter
        selectStoreRv.layoutManager = LinearLayoutManager(context)
    }

    private fun onStores(stores: List<UIStore>?) {
        if (stores == null) {
            progress.visibility = View.VISIBLE
            selectStoreRv.visibility = View.INVISIBLE
        } else {
            progress.visibility = View.GONE
            selectStoreRv.visibility = View.VISIBLE
            adapter.submitList(stores)
        }
    }

    class StoresAdapter(val vm: StoreSelectionVM) :
        ListAdapter<UIStore, StoresAdapter.StoreVH>(DIFF_CALLBACK) {
        companion object {
            val DIFF_CALLBACK = object : DiffUtil.ItemCallback<UIStore>() {
                override fun areItemsTheSame(oldItem: UIStore, newItem: UIStore) =
                    oldItem.id == newItem.id

                override fun areContentsTheSame(oldItem: UIStore, newItem: UIStore) =
                    oldItem == newItem
            }
        }


        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoreVH {
            val inflater = LayoutInflater.from(parent.context)
            return StoreVH(inflater.inflate(R.layout.store, parent, false))
        }

        override fun onBindViewHolder(holder: StoreVH, position: Int) {
            val store = getItem(position)
            val v = holder.itemView
            v.groupType.text = store.activity
            v.name.text = store.name
            Picasso.get()
                .load(store.photoUrl)
                .transform(CircleTransform())
                .into(v.logo)
        }

        inner class StoreVH(v: View) : RecyclerView.ViewHolder(v) {
            init {
                v.setOnClickListener {
                    val pos = adapterPosition
                    if (pos != RecyclerView.NO_POSITION) {
                        vm.storeClicked(getItem(pos).id)
                    }
                }
            }
        }
    }
}