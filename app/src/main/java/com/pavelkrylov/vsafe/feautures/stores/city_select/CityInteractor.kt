package com.pavelkrylov.vsafe.feautures.stores.city_select

import android.content.Context
import android.os.Process
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.pavelkrylov.vsafe.App
import com.pavelkrylov.vsafe.vkmarket.R
import com.pavelkrylov.vsafe.feautures.stores.UICity
import org.json.JSONArray
import java.io.InputStreamReader

class CityInteractor {
    companion object {
        private const val CITY_PREF = "city"
        private const val CITY_NAME_KEY = "city_name"
        private const val CITY_NAME_IN_KEY = "city_in_name"
        private const val CITY_ID_KEY = "city_id"

        fun getSavedCity(): UICity {
            val sp = App.instance.getSharedPreferences(CITY_PREF, Context.MODE_PRIVATE)
            val name = sp.getString(CITY_NAME_KEY, "Санкт-Петербург")!!
            val inName = sp.getString(CITY_NAME_IN_KEY, "Санкт-Петербурге")!!
            val id = sp.getLong(CITY_ID_KEY, 2)
            return UICity(id, name, inName)
        }

        fun saveCity(uiCity: UICity) {
            val sp = App.instance.getSharedPreferences(CITY_PREF, Context.MODE_PRIVATE)
            sp.edit()
                .putLong(CITY_ID_KEY, uiCity.id)
                .putString(CITY_NAME_KEY, uiCity.name)
                .putString(CITY_NAME_IN_KEY, uiCity.inName)
                .apply()
        }
    }

    private class LoadCitiesThread(val ld : MutableLiveData<List<UICity>>) : Thread() {
        init {
            priority = Process.THREAD_PRIORITY_DEFAULT + Process.THREAD_PRIORITY_LESS_FAVORABLE
        }

        override fun run() {
            val inStream = App.instance.resources.openRawResource(R.raw.cities)
            val reader = InputStreamReader(inStream)
            val s = reader.readText()
            val array = JSONArray(s)
            val cities = ArrayList<UICity>()
            for (i in 0 until array.length()) {
                val cityJson = array.getJSONObject(i)
                val uiCity = UICity(cityJson.getLong("id"), cityJson.getString("title"),
                    cityJson.getString("titleIn"))
                cities.add(uiCity)
            }
            ld.postValue(cities)
        }
    }

    fun loadCities(): LiveData<List<UICity>> {
        val ld = MutableLiveData<List<UICity>>()
        LoadCitiesThread(ld).start()
        return ld
    }
}