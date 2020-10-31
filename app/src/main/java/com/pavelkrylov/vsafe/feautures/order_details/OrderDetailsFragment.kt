package com.pavelkrylov.vsafe.feautures.order_details

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.pavelkrylov.vsafe.App
import com.pavelkrylov.vsafe.feautures.checkout.CheckoutVM
import com.pavelkrylov.vsafe.feautures.stores.CircleTransform
import com.pavelkrylov.vsafe.logic.OrderStatus
import com.pavelkrylov.vsafe.ui.RoundedCornersTransform
import com.pavelkrylov.vsafe.vkmarket.R
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.order_details.*
import kotlinx.android.synthetic.main.order_item_cart.view.*
import kotlinx.android.synthetic.main.order_item_cart.view.photo
import kotlinx.android.synthetic.main.order_item_contact.view.*
import kotlinx.android.synthetic.main.order_item_footer.view.*
import kotlinx.android.synthetic.main.order_item_status.view.*

class OrderDetailsFragment : Fragment(R.layout.order_details) {
    companion object {
        private const val IS_CUSTOMER_KEY = "is_customer"
        private const val ORDER_ID_KEY = "order_id"
        const val CUSTOMER_CANCEL_ORDER_BTN_ID = "cancel_order_btn_customer"
        const val OPEN_DISPUTE_CUSTOMER_BTN_ID = "open_dispute_customer"
        const val CONFIRM_ORDER_CUSTOMER_BTN_ID = "confirm_order_customer"

        fun newInstance(isCustomer: Boolean, orderId: Long): OrderDetailsFragment {
            val fragment = OrderDetailsFragment()
            fragment.arguments = Bundle().apply {
                putBoolean(IS_CUSTOMER_KEY, isCustomer)
                putLong(ORDER_ID_KEY, orderId)
            }
            return fragment
        }
    }

    var isCustomer = false
    var orderId = -1L

    lateinit var vm: OrderDetailsVM
    lateinit var adapter: Adapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCustomer = requireArguments().getBoolean(IS_CUSTOMER_KEY)
        orderId = requireArguments().getLong(ORDER_ID_KEY)
        vm = OrderDetailsVMFactory(isCustomer, orderId).create(OrderDetailsVM::class.java)
        adapter = Adapter(vm)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vm.state.observe(viewLifecycleOwner, this::onUIState)
        toolBar.setNavigationOnClickListener { App.INSTANCE.outerCicerone.router.exit() }
        orderDetailsRv.layoutManager = LinearLayoutManager(context)
        orderDetailsRv.adapter = adapter
    }

    private fun onUIState(state: UIState) {
        toolBar.title = getString(R.string.order_id_string, state.orderId)
        if (state.orderInfo == null) {
            buttonsContainer.visibility = View.INVISIBLE
            progress.visibility = View.VISIBLE
            orderDetailsRv.visibility = View.INVISIBLE
        } else {
            buttonsContainer.visibility = View.VISIBLE
            progress.visibility = View.INVISIBLE
            orderDetailsRv.visibility = View.VISIBLE
            adapter.submitList(state.orderInfo.orderList)
        }

        buttonsContainer.removeAllViews()
        val buttonsView: View? = if (isCustomer) {
            when (state.orderInfo?.orderStatus) {
                OrderStatus.CREATED -> createdCustomerButtons()
                OrderStatus.DISPUTE -> disputeCustomerButtons()
                OrderStatus.PAID -> paidCustomerButtons()
                OrderStatus.CONFIRMED -> confirmedCustomerButtons()
                null -> null
                else -> null
            }
        } else {
            null
        }
        buttonsContainer.visibility = if (buttonsView == null) {
            View.INVISIBLE
        } else {
            View.VISIBLE
        }
        if (buttonsView != null) {
            buttonsContainer.addView(
                buttonsView, FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                ).apply {
                    gravity = Gravity.CENTER
                }
            )
        }
    }

    private fun createdCustomerButtons(): View? {
        return layoutInflater.inflate(
            R.layout.order_created_customer_buttons,
            buttonsContainer,
            false
        ).apply {
            findViewById<View>(R.id.payBtn).setOnClickListener {
                openVkBot()
            }
        }
    }

    private fun disputeCustomerButtons(): View? {
        return layoutInflater.inflate(
            R.layout.order_dispute_customer_buttons,
            buttonsContainer,
            false
        ).apply {
            findViewById<View>(R.id.writeUsBtn).setOnClickListener {
                openVkBot()
            }
        }
    }

    private fun paidCustomerButtons(): View? {
        return layoutInflater.inflate(
            R.layout.order_payed_customer_buttons,
            buttonsContainer,
            false
        ).apply {
            findViewById<View>(R.id.cancelBtn).setOnClickListener {
                vm.btnPressed(CUSTOMER_CANCEL_ORDER_BTN_ID)
            }
        }
    }

    private fun confirmedCustomerButtons(): View? {
        return layoutInflater.inflate(
            R.layout.order_confirmed_customer_buttons,
            buttonsContainer,
            false
        ).apply {
            findViewById<View>(R.id.openDisputeBtn).setOnClickListener {
                vm.btnPressed(OPEN_DISPUTE_CUSTOMER_BTN_ID)
            }
            findViewById<View>(R.id.confirmOrderBtn).setOnClickListener {
                vm.btnPressed(CONFIRM_ORDER_CUSTOMER_BTN_ID)
            }
        }
    }

    private fun openVkBot() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(CheckoutVM.GROUP_MESSAGE_URL))
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        App.INSTANCE.startActivity(intent)
    }

    class Adapter(val vm: OrderDetailsVM) :
        ListAdapter<IOrderDetailsItem, RecyclerView.ViewHolder>(DIFF_CALLBACK) {
        companion object {
            val DIFF_CALLBACK = object : DiffUtil.ItemCallback<IOrderDetailsItem>() {
                override fun areItemsTheSame(
                    oldItem: IOrderDetailsItem,
                    newItem: IOrderDetailsItem
                ) = oldItem.id == newItem.id

                @SuppressLint("DiffUtilEquals")
                override fun areContentsTheSame(
                    oldItem: IOrderDetailsItem,
                    newItem: IOrderDetailsItem
                ) = oldItem == newItem
            }

            const val STATUS_ITEM_TYPE = 0
            const val CONTACT_ITEM_TYPE = 1
            const val CART_ITEM_TYPE = 2
            const val FOOTER_ITEM_TYPE = 3
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            fun inflate(id: Int) = inflater.inflate(id, parent, false)
            return when (viewType) {
                STATUS_ITEM_TYPE -> StatusViewHolder(inflate(R.layout.order_item_status))
                CONTACT_ITEM_TYPE -> ContactViewHolder(inflate(R.layout.order_item_contact))
                CART_ITEM_TYPE -> CartViewHolder(inflate(R.layout.order_item_cart))
                FOOTER_ITEM_TYPE -> FooterViewHolder(inflate(R.layout.order_item_footer))
                else -> throw IllegalArgumentException()
            }
        }


        private fun bindStatus(status: UIStatusItem, holder: StatusViewHolder) {
            val v = holder.itemView
            v.container.setBackgroundColor(status.bgColor)
            v.statusNameTv.text = v.context.getString(R.string.status_string, status.statusName)
            v.statusDescriptionTv.text = status.description
        }

        private fun bindCart(cart: UICartItem, holder: CartViewHolder) {
            val v = holder.itemView
            v.cntTv.text = v.context.getString(R.string.cart_cnt, cart.amount)
            v.productName.text = cart.productName
            v.productPrice.text = cart.price

            Picasso.get()
                .load(cart.photoUrl)
                .transform(RoundedCornersTransform())
                .into(v.photo)
        }

        private fun bindOrderFooter(footer: UIOrderFooter, holder: FooterViewHolder) {
            val v = holder.itemView
            v.totalPrice.text = footer.totalPrice
            v.addressTv.text = v.context.getString(R.string.address_string, footer.address)
            v.commentTv.text = v.context.getString(R.string.comment_string, footer.comment)
            v.addressTv.visibility = if (footer.address.isBlank()) {
                View.GONE
            } else {
                View.VISIBLE
            }
            v.commentTv.visibility = if (footer.comment.isBlank()) {
                View.GONE
            } else {
                View.VISIBLE
            }
        }

        private fun bindContact(contact: UIContactItem, holder: ContactViewHolder) {
            val v = holder.itemView
            v.contactName.text = contact.displayName
            v.sectionLabel.text = contact.contactActionText
            v.sectionLabel2.text = if (vm.isCustomer) {
                v.context.getString(R.string.your_order)
            } else {
                v.context.getString(R.string.client_order)
            }

            Picasso.get()
                .load(contact.photoUrl)
                .transform(CircleTransform())
                .into(v.photo)
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val item = getItem(position)
            when (getItemViewType(position)) {
                STATUS_ITEM_TYPE -> bindStatus(item as UIStatusItem, holder as StatusViewHolder)
                CONTACT_ITEM_TYPE -> bindContact(item as UIContactItem, holder as ContactViewHolder)
                CART_ITEM_TYPE -> bindCart(item as UICartItem, holder as CartViewHolder)
                FOOTER_ITEM_TYPE -> bindOrderFooter(
                    item as UIOrderFooter,
                    holder as FooterViewHolder
                )
            }
        }

        override fun getItemViewType(position: Int): Int {
            return when (getItem(position)) {
                is UIStatusItem -> STATUS_ITEM_TYPE
                is UIContactItem -> CONTACT_ITEM_TYPE
                is UICartItem -> CART_ITEM_TYPE
                is UIOrderFooter -> FOOTER_ITEM_TYPE
                else -> throw IllegalArgumentException()
            }
        }

        class StatusViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        }

        inner class ContactViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            init {
                view.contactBlock.setOnClickListener {
                    val pos = adapterPosition
                    if (pos != RecyclerView.NO_POSITION) {
                        val contact = getItem(pos) as? UIContactItem
                        if (contact != null) {
                            val intent = Intent(
                                Intent.ACTION_VIEW,
                                "https://vk.com/im?sel=${contact.msgToId}".toUri()
                            )
                            view.context.startActivity(intent)
                        }
                    }
                }
            }
        }

        class CartViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        }

        class FooterViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        }
    }
}