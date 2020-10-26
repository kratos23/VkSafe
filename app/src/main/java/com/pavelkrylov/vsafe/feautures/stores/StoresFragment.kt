package com.pavelkrylov.vsafe.feautures.stores

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import com.pavelkrylov.vsafe.vkmarket.R
import com.pavelkrylov.vsafe.base.EndlessRecyclerViewScrollListener
import kotlinx.android.synthetic.main.stores.*
import kotlinx.android.synthetic.main.stores.toolBar

class StoresFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.stores, container, false)
    }

    val model: StoresVM by viewModels()
    val cityVM: CityVM by activityViewModels()

    override fun onResume() {
        super.onResume()
        model.presenter.onAttach(this)
        val window = activity!!.window
        window.statusBarColor = Color.WHITE
        model.presenter.setSelectedCityVm(cityVM)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val decView = activity?.window?.decorView
            decView?.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR)
        };
    }

    override fun onPause() {
        super.onPause()
        model.presenter.onDetach()
    }

    lateinit var adapter: StoresAdapter

    fun clearList() {
        val sz = adapter.stores.size
        adapter.stores.clear()
        adapter.notifyItemRangeRemoved(0, sz)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = StoresAdapter(context!!) {
            model.presenter.storeClicked(it)
        }
        toolBar.setOnClickListener {
            model.presenter.citySelectClicked()
        }
        cityVM.selectedCity.observe(viewLifecycleOwner) {
            title.text = getString(R.string.stores_title, it.inName)
            clearList()
        }

        val lm = LinearLayoutManager(context)
        storesRecycler.adapter = adapter
        storesRecycler.layoutManager = lm
        model.firstStoresLD.observe(viewLifecycleOwner) {
            adapter.stores.clear()
            adapter.stores.addAll(it)
            adapter.notifyItemRangeInserted(0, it.count())
            model.firstStoresLD.removeObservers(viewLifecycleOwner)
        }
        model.addStoresLD.value = emptyList()
        model.addStoresLD.observe(viewLifecycleOwner) {
            val pos = adapter.stores.size
            adapter.stores.addAll(it)
            adapter.notifyItemRangeInserted(pos, it.count())
        }
        model.loadingLD.observe(viewLifecycleOwner) {
            adapter.setLoading(it)
        }


        storesRecycler.addOnScrollListener(object : EndlessRecyclerViewScrollListener(lm) {
            override fun onLoadMore() {
                Handler().post {
                    model.presenter.onLoadMore()
                }
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isRemoving) {
            model.presenter.onDestroy()
        }
    }
}