package com.pavelkrylov.vsafe.feautures.stores.city_select

import android.animation.ValueAnimator
import android.graphics.Color
import android.graphics.drawable.TransitionDrawable
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.Animation.AnimationListener
import android.view.animation.AnimationUtils
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import com.pavelkrylov.vsafe.App
import com.pavelkrylov.vsafe.feautures.stores.CityVM
import com.pavelkrylov.vsafe.vkmarket.R
import kotlinx.android.synthetic.main.city_select.*
import kotlin.math.roundToInt


class CitySelectFragment : DialogFragment() {
    companion object {
        const val TOOL_BAR_COLOR_KEY = "toolbar_color"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.city_select, container, false)
    }

    val model: CitySelectVM by viewModels()
    val selectedCityModel: CityVM by activityViewModels()
    lateinit var adapter: CityAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recycler.layoutManager = LinearLayoutManager(context)
        adapter = CityAdapter(context!!) {
            model.citiesPresenter.citySelected(it)
        }
        recycler.adapter = adapter
        recycler.setHasFixedSize(true)

        model.citiesLD.observe(viewLifecycleOwner) {
            adapter.setCities(it)
        }
        selectedCityModel.selectedCity.observe(viewLifecycleOwner) {
            adapter.setSelectedCityId(it.id)
        }
        toolBar.outlineProvider = TopBarOutlineProvider()

        closeBtn.setOnClickListener {
            App.INSTANCE.getRouter().exit()
        }
    }

    var statusBarAnimator: ValueAnimator? = null
    var toolbarColor = Color.WHITE

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(TOOL_BAR_COLOR_KEY, toolbarColor)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        savedInstanceState?.let {
            updateToolbar(it.getInt(TOOL_BAR_COLOR_KEY))
        }
    }

    private fun updateToolbar(color: Int) {
        toolbarColor = color
        val window = activity?.window
        window?.statusBarColor = color
    }

    override fun onCreateAnimation(transit: Int, enter: Boolean, nextAnim: Int): Animation? {
        if (nextAnim == 0) {
            return null
        }
        val anim: Animation = AnimationUtils.loadAnimation(activity, nextAnim)
        anim.setAnimationListener(object : AnimationListener {
            override fun onAnimationStart(animation: Animation) {
                if (enter) {
                    statusBarAnimator = ValueAnimator.ofArgb(
                        Color.WHITE,
                        Color.parseColor("#66000000")
                    )
                    statusBarAnimator?.addUpdateListener {
                        val color = it.animatedValue as Int
                        updateToolbar(color)
                    }
                    statusBarAnimator?.duration = 250
                    statusBarAnimator?.start()
                    return
                }
                statusBarAnimator = ValueAnimator.ofArgb(
                    Color.parseColor("#66000000"),
                    Color.WHITE
                )
                val crossfader = TransitionDrawable(
                    arrayOf(
                        resources.getDrawable(
                            R.drawable.round_white_bg,
                            activity!!.theme
                        ),
                        resources.getDrawable(R.drawable.round_bg, activity!!.theme)
                    )
                )
                crossfader.isCrossFadeEnabled = true
                statusBarAnimator?.duration = 250
                statusBarAnimator?.addUpdateListener {
                    val color = it.animatedValue as Int
                    val window = activity?.window
                    window?.statusBarColor = color
                    crossfader.alpha = (it.animatedFraction * 255).roundToInt()
                }
                statusBarAnimator?.start()

                container?.background =
                    resources.getDrawable(R.drawable.round_white_bg, activity!!.theme)
                container?.let {
                    it.background = crossfader
                }
            }

            override fun onAnimationRepeat(animation: Animation) {
            }

            override fun onAnimationEnd(animation: Animation) {
                statusBarAnimator?.end()
                statusBarAnimator = null
            }
        })
        return anim
    }

    override fun onResume() {
        super.onResume()
        model.citiesPresenter.setSelectedCityVm(selectedCityModel)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val decView = activity?.window?.decorView
            decView?.setSystemUiVisibility(
                decView.getSystemUiVisibility() and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
            )
        };
    }
}