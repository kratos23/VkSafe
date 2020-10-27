package com.pavelkrylov.vsafe.feautures.stores

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.*
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import com.pavelkrylov.vsafe.base.EndlessRecyclerViewScrollListener
import com.pavelkrylov.vsafe.feautures.main.MainActivity
import com.pavelkrylov.vsafe.vkmarket.R
import kotlinx.android.synthetic.main.stores.*

class StoresFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.stores, container, false)
    }

    val model: StoresVM by viewModels()

    override fun onResume() {
        super.onResume()
        model.presenter.onAttach(this)
        val window = activity!!.window
        window.statusBarColor = Color.WHITE
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

    private val backCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (searchView?.isIconified == false) {
                searchView?.setQuery("", true)
                searchView?.isIconified = true
            } else {
                isEnabled = false
                activity?.onBackPressed()
                isEnabled = true
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? MainActivity)?.setSupportActionBar(toolBar)
        adapter = StoresAdapter(context!!) {
            model.presenter.storeClicked(it)
        }

        val lm = LinearLayoutManager(context)
        storesRecycler.adapter = adapter
        storesRecycler.layoutManager = lm
        model.firstStoresLD.observe(viewLifecycleOwner) {
            clearList()
            adapter.stores.addAll(it)
            adapter.notifyItemRangeInserted(0, it.count())
            model.firstStoresLD.removeObservers(viewLifecycleOwner)
            model.addStoresLD.value = emptyList()
            model.addStoresLD.observe(viewLifecycleOwner) {
                val pos = adapter.stores.size
                adapter.stores.addAll(it)
                adapter.notifyItemRangeInserted(pos, it.count())
            }
        }
        model.loadingLD.observe(viewLifecycleOwner) {
            adapter.setLoading(it)
        }
        backCallback.isEnabled = true
        requireActivity().onBackPressedDispatcher.addCallback(backCallback)
        storesRecycler.addOnScrollListener(object : EndlessRecyclerViewScrollListener(lm) {
            override fun onLoadMore() {
                Handler().post {
                    model.presenter.onLoadMore()
                }
            }
        })
    }


    var searchView: SearchView? = null
    private val searchListener = object : SearchView.OnQueryTextListener {
        override fun onQueryTextSubmit(query: String): Boolean {
            return true
        }

        override fun onQueryTextChange(newText: String): Boolean {
            clearList()
            model.presenter.setQuery(newText)
            model.query = newText
            return true
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.stores, menu)
        searchView = menu.findItem(R.id.searchBar).actionView as SearchView
        searchView?.maxWidth = Int.MAX_VALUE
        searchView?.setQuery(model.query, true)
        searchView?.isIconified = model.query.isEmpty()
        searchView?.setOnQueryTextListener(searchListener)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        searchView = null
        (activity as? MainActivity)?.setSupportActionBar(null)
        backCallback.remove()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isRemoving) {
            model.presenter.onDestroy()
        }
    }
}