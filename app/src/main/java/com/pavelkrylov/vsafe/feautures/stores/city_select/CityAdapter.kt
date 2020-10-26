package com.pavelkrylov.vsafe.feautures.stores.city_select

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.pavelkrylov.vsafe.vkmarket.R
import com.pavelkrylov.vsafe.feautures.stores.UICity
import kotlinx.android.synthetic.main.city.view.*

class CityAdapter(val ctx: Context, val cityClickListener: (UICity) -> Unit) :
    RecyclerView.Adapter<CityAdapter.CityHolder>() {
    private var cities = emptyList<UICity>()
    val inflater = LayoutInflater.from(ctx)

    private var selectedCityId = -1L

    fun setCities(cities: List<UICity>) {
        this.cities = cities
        notifyDataSetChanged()
    }

    fun setSelectedCityId(cityId: Long) {
        for ((i, city) in cities.withIndex()) {
            if (city.id == selectedCityId) {
                notifyItemChanged(i)
            }
        }
        selectedCityId = cityId
        notifyDataSetChanged()
        for ((i, city) in cities.withIndex()) {
            if (city.id == cityId) {
                notifyItemChanged(i)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CityHolder {
        return CityHolder(inflater.inflate(R.layout.city, parent, false))
    }

    override fun getItemCount() = cities.size

    override fun onBindViewHolder(holder: CityHolder, position: Int) {
        cities[position].let {
            holder.itemView.cityName.text = it.name
            if (it.id == selectedCityId) {
                holder.itemView.selectedImage.visibility = View.VISIBLE
            } else {
                holder.itemView.selectedImage.visibility = View.INVISIBLE
            }
        }
    }

    inner class CityHolder(v: View) : RecyclerView.ViewHolder(v) {
        init {
            v.setOnClickListener {
                val pos = adapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    cityClickListener(cities[pos])
                }
            }
        }


    }
}