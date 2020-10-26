package com.pavelkrylov.vsafe.feautures.product_info

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.doOnLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import com.pavelkrylov.vsafe.App
import com.pavelkrylov.vsafe.logic.formatPrice
import com.pavelkrylov.vsafe.vkmarket.R
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.product_info.*

class ProductInfoFragment : Fragment() {
    companion object {
        const val PRODUCT_ID_KEY = "product_id"
        const val GROUP_ID_KEY = "group_id"
        const val PRODUCT_NAME_KEY = "group_name"

        fun newInstance(productId: Long, groupId: Long, name: String): ProductInfoFragment {
            return ProductInfoFragment().apply {
                arguments = Bundle().apply {
                    putLong(PRODUCT_ID_KEY, productId)
                    putLong(GROUP_ID_KEY, groupId)
                    putString(PRODUCT_NAME_KEY, name)
                }
            }
        }
    }

    var productId = -1L
    var groupId = -1L
    var productName = ""
    lateinit var model: ProductInfoVM

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments!!.let {
            productId = it.getLong(PRODUCT_ID_KEY)
            groupId = it.getLong(GROUP_ID_KEY)
            productName = it.getString(PRODUCT_NAME_KEY)!!
        }

        model = ViewModelProvider(
            this, ProductInfoVMFactory(
                groupId,
                productId
            )
        ).get(ProductInfoVM::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.product_info, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        productsTitle.text = productName
        dataGroup.visibility = View.GONE
        model.productInfoLD.observe(viewLifecycleOwner) { product ->
            favoriteContainer.visibility = View.VISIBLE
            dataGroup.visibility = View.VISIBLE
            progress.visibility = View.GONE
            productNameText.text = product.name
            price.text = formatPrice(product.price, product.priceCurrency)
            description.text = product.description

            productImage.doOnLayout {
                Picasso.get()
                    .load(product.photoUrl)
                    .resize(productImage.width, productImage.height)
                    .centerCrop()
                    .into(productImage)
            }
        }
        closeBtn.setOnClickListener {
            App.INSTANCE.getRouter().exit()
        }

        favoriteBtn.setOnClickListener {
            model.presenter.favoriteBtnClicked()
        }

        model.addedToFavorite.observe(viewLifecycleOwner) {
            if (it) {
                favoriteBtn.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#EDEEF0"))
                favoriteBtn.text = getString(R.string.remove_from_favorites)
                favoriteBtn.setTextColor(Color.parseColor("#3F8AE0"))
            } else {
                favoriteBtn.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#4986CC"))
                favoriteBtn.text = getString(R.string.add_to_favorites)
                favoriteBtn.setTextColor(Color.parseColor("#FFFFFF"))
            }
        }
    }
}