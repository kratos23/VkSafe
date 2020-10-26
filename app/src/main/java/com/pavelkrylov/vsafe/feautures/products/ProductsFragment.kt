package com.pavelkrylov.vsafe.feautures.products

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.recyclerview.widget.GridLayoutManager
import com.pavelkrylov.vsafe.App
import com.pavelkrylov.vsafe.vkmarket.R
import com.pavelkrylov.vsafe.base.EndlessRecyclerViewScrollListener
import kotlinx.android.synthetic.main.products.*
import kotlinx.android.synthetic.main.products.closeBtn

class ProductsFragment : Fragment() {
    companion object {
        const val GROUP_ID_KEY = "groupId"
        const val GROUP_NAME_KEY = "groupName"

        fun newInstance(groupId: Long, groupName: String) = ProductsFragment().apply {
            arguments = Bundle().apply {
                putLong(GROUP_ID_KEY, groupId)
                putString(GROUP_NAME_KEY, groupName)
            }
        }
    }

    var groupId = -1L
    var groupName = ""
    lateinit var model: ProductsVM

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        groupId = arguments!!.getLong(GROUP_ID_KEY)
        groupName = arguments!!.getString(GROUP_NAME_KEY)!!
        model = ViewModelProvider(this, ProductsVMFactory(groupId)).get(ProductsVM::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.products, container, false)
    }


    lateinit var adapter: ProductsAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        productsTitle.text = getString(R.string.products_title, groupName)

        adapter = ProductsAdapter(context!!) {
            model.presenter.productSelected(it)
        }
        val lm = GridLayoutManager(context, 2).apply {
            spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    if (position == adapter.products.size) {
                        return 2
                    } else {
                        return 1;
                    }
                }

            }
        }
        productsRecycler.layoutManager = lm
        productsRecycler.adapter = adapter

        model.firstProductsLD.observe(viewLifecycleOwner) {
            adapter.products.clear()
            adapter.products.addAll(it)
            adapter.notifyItemRangeInserted(0, it.count())
            model.firstProductsLD.removeObservers(viewLifecycleOwner)
        }
        model.addProductsLD.value = emptyList()
        model.addProductsLD.observe(viewLifecycleOwner) {
            val pos = adapter.products.size
            adapter.products.addAll(it)
            adapter.notifyItemRangeInserted(pos, it.count())
        }
        model.loadingLD.observe(viewLifecycleOwner) {
            adapter.setLoading(it)
        }
        model.showNoProductsLD.observe(viewLifecycleOwner) {
            if (it) {
                productsRecycler.visibility = View.GONE
                noProducts.visibility = View.VISIBLE
            } else {
                productsRecycler.visibility = View.VISIBLE
                noProducts.visibility = View.GONE
            }
        }

        productsRecycler.addOnScrollListener(object : EndlessRecyclerViewScrollListener(lm) {
            override fun onLoadMore() {
                Handler().post {
                    model.presenter.onLoadMore()
                }
            }
        })

        closeBtn.setOnClickListener {
            App.instance.getRouter().exit()
        }
    }
}