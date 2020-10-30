package com.pavelkrylov.vsafe.feautures.orders

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.pavelkrylov.vsafe.feautures.stores.CircleTransform
import com.pavelkrylov.vsafe.logic.OrderStatus
import com.pavelkrylov.vsafe.logic.getStatusText
import com.pavelkrylov.vsafe.logic.toOrderStatus
import com.pavelkrylov.vsafe.vkmarket.R
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.order.view.*
import kotlinx.android.synthetic.main.orders.*

class OrdersFragment : Fragment(R.layout.orders) {
    companion object {
        const val IS_CUSTOMER_KEY = "customer"

        fun newInstance(customer: Boolean): OrdersFragment {
            val fragment = OrdersFragment()
            fragment.arguments = Bundle().apply {
                putBoolean(IS_CUSTOMER_KEY, customer)
            }
            return fragment
        }
    }

    private lateinit var vm: OrdersVM
    private lateinit var adapter: Adapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val isCustomer = requireArguments().getBoolean(IS_CUSTOMER_KEY)
        vm = ViewModelProvider(this, OrdersVMFactory(isCustomer)).get(OrdersVM::class.java)
        adapter = Adapter(vm)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vm.ordersLD.observe(viewLifecycleOwner, this::onOrders)
        ordersRecycler.layoutManager = LinearLayoutManager(context)
        ordersRecycler.adapter = adapter
        vm.onViewCreated()
    }

    fun onOrders(orders: List<UIOrder>?) {
        if (orders == null) {
            progress.visibility = View.VISIBLE
            ordersEmptyTv.visibility = View.GONE
            ordersRecycler.visibility = View.INVISIBLE
        } else {
            progress.visibility = View.GONE
            if (orders.isEmpty()) {
                ordersRecycler.visibility = View.GONE
                ordersEmptyTv.visibility = View.VISIBLE
            } else {
                ordersRecycler.visibility = View.VISIBLE
                ordersEmptyTv.visibility = View.GONE
            }
            val newOrders = if (orders.isEmpty()) {
                emptyList()
            } else {
                orders + SpaceItem("spacer")
            }
            adapter.submitList(newOrders)
        }
    }

    class Adapter(val vm: OrdersVM) :
        ListAdapter<IOrderItem, RecyclerView.ViewHolder>(DIFF_CALLBACK) {



        private val statusColor = mapOf(
            OrderStatus.PAID to Color.parseColor("#FFA000"),
            OrderStatus.CREATED to Color.parseColor("#6D7885"),
            OrderStatus.CONFIRMED to Color.parseColor("#3F8AE0"),
            OrderStatus.DISPUTE to Color.parseColor("#EB4250"),
            OrderStatus.CLOSED to Color.parseColor("#4BB34B"),
            OrderStatus.CANCELED to Color.parseColor("#857250")
        )


        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            return when (viewType) {
                ORDER_ITEM -> OrderViewHolder(inflater.inflate(R.layout.order, parent, false))
                SPACE_ITEM -> SpaceViewHolder(inflater.inflate(R.layout.order_space, parent, false))
                else -> throw IllegalArgumentException()
            }
        }

        override fun getItemViewType(position: Int): Int {
            return when (getItem(position)) {
                is UIOrder -> ORDER_ITEM
                is SpaceItem -> SPACE_ITEM
                else -> throw IllegalArgumentException()
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            if (getItemViewType(position) == ORDER_ITEM) {
                val v = holder.itemView
                val order = getItem(position) as UIOrder
                v.orderId.text = v.context.getString(R.string.order_id_string, order.id)
                v.groupName.text = order.displayName
                v.priceTv.text = order.price
                v.statusTv.text = getStatusText(order.status.toOrderStatus()!!, vm.isCustomer)
                statusColor[order.status.toOrderStatus()]?.let { v.statusTv.setTextColor(it) }
                Picasso.get()
                    .load(order.photoUrl)
                    .transform(CircleTransform())
                    .into(v.photo)
            }
        }

        inner class OrderViewHolder(v: View) : RecyclerView.ViewHolder(v) {
            init {
                itemView.setOnClickListener {
                    val pos = adapterPosition
                    if (pos != RecyclerView.NO_POSITION) {
                        vm.orderClicked((getItem(pos) as UIOrder).id)
                    }
                }
            }
        }

        inner class SpaceViewHolder(v: View) : RecyclerView.ViewHolder(v)

        companion object {
            const val ORDER_ITEM = 0
            const val SPACE_ITEM = 1
            val DIFF_CALLBACK = object : DiffUtil.ItemCallback<IOrderItem>() {
                override fun areItemsTheSame(oldItem: IOrderItem, newItem: IOrderItem) =
                    oldItem.itemId == newItem.itemId

                @SuppressLint("DiffUtilEquals")
                override fun areContentsTheSame(oldItem: IOrderItem, newItem: IOrderItem) =
                    oldItem == newItem
            }
        }
    }
}