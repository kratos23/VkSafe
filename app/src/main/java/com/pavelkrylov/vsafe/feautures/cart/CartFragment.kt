package com.pavelkrylov.vsafe.feautures.cart

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.*
import com.pavelkrylov.vsafe.App
import com.pavelkrylov.vsafe.ui.RoundedCornersTransform
import com.pavelkrylov.vsafe.vkmarket.R
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.cart.*
import kotlinx.android.synthetic.main.cart_item.view.*
import kotlin.math.roundToLong

class CartFragment : Fragment(R.layout.cart) {
    companion object {
        const val GROUP_ID_KEY = "group_id"

        fun newInstance(groupId: Long): CartFragment {
            val fragment = CartFragment()
            fragment.arguments = Bundle().apply {
                putLong(GROUP_ID_KEY, groupId)
            }
            return fragment
        }
    }

    lateinit var vm: CartVM
    var groupId = -1L
    lateinit var adapter: Adapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        groupId = requireArguments().getLong(GROUP_ID_KEY)
        vm = ViewModelProvider(this, CartVMFactory(groupId)).get(CartVM::class.java)
        adapter = Adapter(vm)
    }

    private fun getCartPrice(cart: List<UICartItem>?): String? {
        cart ?: return null
        if (cart.isEmpty()) {
            return null
        }
        val total =
            (cart.sumByDouble { it.oneItemPrice * it.count.toDouble() } / 100.0).roundToLong()
        val currency = cart.first().currency
        return "$total ${currency.sign ?: currency.name}"
    }

    fun onUIState(state: CartUIState) {
        emptyCartGroup.visibility = if (state.cart?.isEmpty() == true) {
            View.VISIBLE
        } else {
            View.GONE
        }
        cartRv.visibility = if (state.cart?.isEmpty() == true) {
            View.GONE
        } else {
            View.VISIBLE
        }
        clearMenuItem.isVisible = state.cart?.isNotEmpty() == true

        if (state.cart.isNullOrEmpty()) {
            nextContainer.visibility = View.INVISIBLE
        } else {
            nextContainer.visibility = View.VISIBLE
        }
        nextBtn.text = getString(R.string.next, getCartPrice(state.cart))

        if (state.nextLoading) {
            progress.visibility = View.VISIBLE
            nextBtn.visibility = View.INVISIBLE
        } else {
            progress.visibility = View.INVISIBLE
            nextBtn.visibility = View.VISIBLE
        }
        val newList = state.cart?.plusElement(SpacerItem("spacer"))
        adapter.submitList(newList)
    }

    lateinit var clearMenuItem: MenuItem

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbar.setNavigationOnClickListener { App.INSTANCE.outerCicerone.router.exit() }
        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.clearCart -> {
                    vm.clearCartClicked()
                    true
                }
                else -> false
            }
        }
        clearMenuItem = toolbar.menu.findItem(R.id.clearCart)
        vm.getUiState().observe(viewLifecycleOwner, this::onUIState)
        cartRv.adapter = adapter
        cartRv.layoutManager = LinearLayoutManager(context)
        (cartRv.itemAnimator as? SimpleItemAnimator)?.supportsChangeAnimations = false
        returnToMarketBtn.setOnClickListener { vm.returnToMarketBtnClicked() }
        nextBtn.setOnClickListener { vm.nextBtnClicked() }
    }

    class Adapter(val vm: CartVM) : ListAdapter<CartItem, RecyclerView.ViewHolder>(DIFF_CALLBACK) {
        companion object {
            val DIFF_CALLBACK = object : DiffUtil.ItemCallback<CartItem>() {
                override fun areItemsTheSame(oldItem: CartItem, newItem: CartItem) =
                    oldItem.id == newItem.id

                @SuppressLint("DiffUtilEquals")
                override fun areContentsTheSame(oldItem: CartItem, newItem: CartItem) =
                    oldItem == newItem
            }

            const val CART_ITEM_TYPE = 0
            const val SPACER_TYPE = 1
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val inf = LayoutInflater.from(parent.context)
            return when (viewType) {
                CART_ITEM_TYPE -> CartItemHolder(inf.inflate(R.layout.cart_item, parent, false))
                SPACER_TYPE -> SpacerHolder(inf.inflate(R.layout.cart_space, parent, false))
                else -> throw IllegalArgumentException()
            }
        }

        private fun getPrice(uiCartItem: UICartItem): String {
            val price = (uiCartItem.oneItemPrice * uiCartItem.count.toDouble()) / 100.0
            return "${price.roundToLong()} ${uiCartItem.currency.sign ?: uiCartItem.currency.name}"
        }

        fun bindCartItem(holder: CartItemHolder, uiCartItem: UICartItem) {
            val v = holder.itemView
            v.productName.text = uiCartItem.productName
            v.productPrice.text = getPrice(uiCartItem)
            v.cntTv.text = v.context.getString(R.string.cart_cnt, uiCartItem.count)

            Picasso.get()
                .load(uiCartItem.photoUrl)
                .transform(RoundedCornersTransform())
                .into(v.photo)
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            when (holder) {
                is CartItemHolder -> {
                    bindCartItem(holder, getItem(position) as UICartItem)
                }
            }
        }

        override fun getItemViewType(position: Int): Int {
            return when (getItem(position)) {
                is UICartItem -> CART_ITEM_TYPE
                is SpacerItem -> SPACER_TYPE
                else -> throw IllegalArgumentException()
            }
        }

        inner class SpacerHolder(v: View) : RecyclerView.ViewHolder(v)
        inner class CartItemHolder(v: View) : RecyclerView.ViewHolder(v) {
            init {
                v.cartMinusBtn.setOnClickListener {
                    val pos = adapterPosition
                    if (pos != RecyclerView.NO_POSITION) {
                        vm.productDecClicked(getItem(pos).id)
                    }
                }
                v.cartPlusBtn.setOnClickListener {
                    val pos = adapterPosition
                    if (pos != RecyclerView.NO_POSITION) {
                        vm.productIncClicked(getItem(pos).id)
                    }
                }
            }
        }
    }
}